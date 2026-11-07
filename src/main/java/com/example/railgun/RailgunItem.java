package com.example.railgun;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class RailgunItem extends Item {

    // ---- Tweak these to taste ----
    private static final double RANGE = 192.0;          // blocks
    private static final float DAMAGE = 1000.0f;        // per entity hit (pierces everything in line)
    private static final float EXPLOSION_POWER = 5.0f;  // TNT = 4.0
    private static final boolean DESTROY_BLOCKS = true; // false = explosion only hurts entities
    private static final int COOLDOWN_TICKS = 12;       // 0.6 seconds
    private static final double RECOIL = 0.35;

    public RailgunItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (world instanceof ServerWorld serverWorld) {
            fire(serverWorld, player);
        }

        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(stack, world.isClient());
    }

    private void fire(ServerWorld world, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d dir = player.getRotationVec(1.0f).normalize();
        Vec3d maxEnd = start.add(dir.multiply(RANGE));

        // Where does the beam stop? First solid block, or max range.
        BlockHitResult blockHit = world.raycast(new RaycastContext(
                start, maxEnd,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player));
        boolean hitBlock = blockHit.getType() != HitResult.Type.MISS;
        Vec3d end = hitBlock ? blockHit.getPos() : maxEnd;

        // Damage every living thing the beam passes through.
        Box searchBox = new Box(start, end).expand(2.0);
        List<Entity> candidates = world.getOtherEntities(player, searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());
        for (Entity target : candidates) {
            Box hitbox = target.getBoundingBox().expand(0.35);
            if (hitbox.contains(start) || hitbox.raycast(start, end).isPresent()) {
                target.timeUntilRegen = 0; // ignore invulnerability frames
                target.damage(world.getDamageSources().playerAttack(player), DAMAGE);
            }
        }

        // Beam visuals.
        double length = end.distanceTo(start);
        Vec3d muzzle = start.add(dir.multiply(1.0)).add(0, -0.15, 0);
        for (double d = 1.0; d < length; d += 0.4) {
            Vec3d p = start.add(dir.multiply(d));
            world.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            if (((int) (d / 0.4)) % 3 == 0) {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }
        world.spawnParticles(ParticleTypes.FLASH, muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);

        // Impact.
        if (hitBlock) {
            world.createExplosion(player, end.x, end.y, end.z, EXPLOSION_POWER,
                    DESTROY_BLOCKS ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE);
        }

        // Sound + recoil.
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0f, 2.0f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 2.0f, 0.5f);

        player.addVelocity(-dir.x * RECOIL, -dir.y * RECOIL * 0.5, -dir.z * RECOIL);
        player.velocityModified = true;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click to fire").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Pierces everything. Explodes on impact.").formatted(Formatting.DARK_AQUA));
    }
}
