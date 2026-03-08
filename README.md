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