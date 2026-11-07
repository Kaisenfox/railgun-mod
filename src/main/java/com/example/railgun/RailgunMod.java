package com.example.railgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class RailgunMod implements ModInitializer {
    public static final String MOD_ID = "railgun";

    public static Item RAILGUN;

    @Override
    public void onInitialize() {
        RAILGUN = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "railgun"),
                new RailgunItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC))
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(RAILGUN));
    }
}
