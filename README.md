![tessera landscape](assets/images/b.jpg)

# Tessera
**A voxel game written in Java + LWJGL.**


If nothing happens, it could be that you dont have JRE 17 installed on your machine.
1. Test if you have java installed with `java -version`
2. Install JDK 17: https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-17)

## Important notes
* The JVM version must be 17. If it is higher, the following message will show in output:
  * `[LWJGL] [ThreadLocalUtil] Unsupported JNI version detected, this may result in a crash. Please inform LWJGL developers.`
* I use LWJGL's Nuklear library that is builtin to LWJGL to do all of the UI rendering.
* Each chunk is 32x32x32 in size. Chunks coordinates are 3D
* The up direction is -Y, and the down direction is +Y

### Textures
Textures taken mostly from Pixel Perfection, with some handcrafted ones, and textures taken from open source minetest texture packs as well:
* https://github.com/Athemis/PixelPerfectionCE/tree/master
* https://github.com/Wallbraker/PixelPerfection?tab=readme-ov-file

### Blender profiles
All entities and block types are made using blender.
There are 2 blender profiles, one for blocks and one for entities
* The block profile has +Y as up direction
* The entity profile has -Y as up direction

## Test run modes (block breaking/placing)
These launch arguments skip the menus and drop you straight into a world so
block breaking/placing can be tested over each transport. All modes auto-open
the block-interaction test panel (F8 toggles it):
* `testSingleplayer` — loads the first world in the saves list in
  singleplayer (FakeChannel transport). Exits if no worlds exist.
* `testMultiplayer` — hosts the first world on port `25565` and automatically
  opens a second window (separate JVM) joined to it, so edits can be tested
  across a real Netty connection in both directions. Exits if no worlds exist.
* `testMultiplayerJoin` — joins a test host at `127.0.0.1:25565`
  (used by the spawned second window, but you can also run it yourself).
* Optional extras (only used when a test mode is active):
  * `port=NNNN` — override the test port (1024-65535).
  * `playerName=Name` — override this window's player name without saving it.
    Needed because the server rejects duplicate player names.

Examples (args are passed straight to `com.tessera.Main`):
* `testSingleplayer`
* `testMultiplayer`
* `testMultiplayer port=25566 playerName=Host1`