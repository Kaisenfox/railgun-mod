Railgun - Fabric mod for Minecraft Java 1.21.1

BUILD
1. Generate a Gradle wrapper (this zip doesn't include the wrapper jar):
   - Easiest: download the official template from https://fabricmc.net/develop/template/
     then copy its gradle/ folder, gradlew and gradlew.bat into this project, OR
   - If you have Gradle 8.8+ installed: run `gradle wrapper --gradle-version 8.8`
2. Needs JDK 21.
3. Run `./gradlew build` (Windows: gradlew.bat build)
4. Jar is at build/libs/railgun-1.0.0.jar -> put it in your mods folder
   (requires Fabric Loader 0.16+ and Fabric API for 1.21.1).

DEV: `./gradlew runClient` launches a test client. Get the gun with
`/give @s railgun:railgun`.

TUNING: constants at the top of RailgunItem.java (damage, range, explosion size,
cooldown, block destruction on/off).
