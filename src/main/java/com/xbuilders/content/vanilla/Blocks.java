package com.xbuilders.content.vanilla;

import com.xbuilders.Main;
import com.xbuilders.content.vanilla.blocks.blocks.*;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.server.builtinMechanics.gravityBlock.GravityBlock;
import com.xbuilders.engine.server.ItemUtils;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.utils.resource.PathHandler;
import com.xbuilders.content.vanilla.blocks.*;
import com.xbuilders.content.vanilla.blocks.blocks.trees.*;

import java.io.IOException;
import java.util.ArrayList;

import static com.xbuilders.content.vanilla.blocks.PlantBlockUtils.GROW_PROBABILITY;
import static com.xbuilders.engine.server.ItemUtils.getJsonBlocksFromResource;
import static com.xbuilders.engine.common.math.RandomUtils.random;

public class Blocks {


    public static short BLOCK_AIR = 0;
    //List of all block IDs
    public static short BLOCK_BEDROCK = 1;
    public static short BLOCK_BIRCH_LOG = 2;
    public static short BLOCK_BIRCH_LEAVES = 3;
    public static short BLOCK_BRICK = 4;
    public static short BLOCK_VINES = 5;
    public static short BLOCK_DIRT = 6;
    public static short BLOCK_PANSIES = 7;
    public static short BLOCK_GLASS = 8;
    public static short BLOCK_GRASS = 9;
    public static short BLOCK_GRAVEL = 10;
    public static short BLOCK_OAK_LOG = 11;
    public static short BLOCK_OAK_LEAVES = 12;
    public static short BLOCK_GRASS_PLANT = 14;
    public static short BLOCK_SUGAR_CANE = 15;
    public static short BLOCK_BAMBOO = 740;
    public static short BLOCK_SAND = 16;
    public static short BLOCK_SANDSTONE = 17;
    public static short BLOCK_ANDESITE = 18;
    public static short BLOCK_STONE_BRICK = 19;
    public static short BLOCK_TORCH = 20;
    public static short BLOCK_WATER = 21;
    public static short BLOCK_LAVA = 25;
    public static short BLOCK_WOOL = 22;
    public static short BLOCK_SNOW_GRASS = 23;
    public static short BLOCK_BOOKSHELF = 24;
    public static short BLOCK_TALL_DRY_GRASS_TOP = 26;
    public static short BLOCK_CRACKED_STONE = 27;
    public static short BLOCK_STONE_WITH_VINES = 28;
    public static short BLOCK_TNT_ACTIVE = 29;
    public static short BLOCK_JUNGLE_WOOD = 30;
    public static short BLOCK_JUNGLE_WOOD_SLAB = 31;
    public static short BLOCK_JUNGLE_WOOD_STAIRS = 32;
    public static short BLOCK_HONEYCOMB_BLOCK = 33;
    public static short BLOCK_MOSAIC_BAMBOO_WOOD = 34;
    public static short BLOCK_MUSIC_BOX = 35;
    public static short BLOCK_CAKE = 36;
    public static short BLOCK_JUNGLE_SAPLING = 37;
    public static short BLOCK_OBSIDIAN = 38;
    public static short BLOCK_BURGUNDY_BRICK = 39;
    public static short BLOCK_SEA_GRASS = 253;
    public static short BLOCK_JUNGLE_FENCE = 40;
    public static short BLOCK_RED_FLOWER = 41;
    public static short BLOCK_TALL_DRY_GRASS = 42;
    public static short BLOCK_RED_CANDLE = 43;
    public static short BLOCK_YELLOW_FLOWER = 44;
    public static short BLOCK_COAL_BLOCK = 46;
    public static short BLOCK_JUNGLE_LEAVES = 47;
    public static short BLOCK_JUNGLE_LOG = 48;
    public static short BLOCK_TALL_GRASS_TOP = 49;
    public static short BLOCK_30_HP_ENGINE = 51;
    public static short BLOCK_BEEHIVE = 52;
    public static short BLOCK_DIORITE = 53;
    public static short BLOCK_POLISHED_DIORITE = 54;
    public static short BLOCK_POLISHED_ANDESITE = 56;
    public static short BLOCK_SPRUCE_WOOD = 57;
    public static short BLOCK_AZURE_BLUET = 58;
    public static short BLOCK_DANDELION = 59;
    public static short BLOCK_BLUE_ORCHID = 60;
    public static short BLOCK_FERN = 61;
    public static short BLOCK_GRANITE_BRICK = 62;
    public static short BLOCK_ACACIA_WOOD = 63;
    public static short BLOCK_AMETHYST_CRYSTAL = 64;
    public static short BLOCK_CLAY = 65;
    public static short BLOCK_YELLOW_CONCRETE = 66;
    public static short BLOCK_YELLOW_GLAZED_TERACOTTA = 67;
    public static short BLOCK_BLACK_CONCRETE = 68;
    public static short BLOCK_BLACK_GLAZED_TERACOTTA = 69;
    public static short BLOCK_BLUE_CONCRETE = 70;
    public static short BLOCK_BLUE_GLAZED_TERACOTTA = 71;
    public static short BLOCK_BROWN_CONCRETE = 72;
    public static short BLOCK_BROWN_GLAZED_TERACOTTA = 73;
    public static short BLOCK_CYAN_CONCRETE = 74;
    public static short BLOCK_CYAN_GLAZED_TERACOTTA = 75;
    public static short BLOCK_GRAY_CONCRETE = 76;
    public static short BLOCK_GRAY_GLAZED_TERACOTTA = 77;
    public static short BLOCK_GREEN_CONCRETE = 78;
    public static short BLOCK_GREEN_GLAZED_TERACOTTA = 79;
    public static short BLOCK_LIGHT_BLUE_CONCRETE = 80;
    public static short BLOCK_LIGHT_BLUE_GLAZED_TERACOTTA = 81;
    public static short BLOCK_LIGHT_GRAY_CONCRETE = 82;
    public static short BLOCK_LIGHT_GRAY_GLAZED_TERACOTTA = 83;
    public static short BLOCK_LIME_CONCRETE = 84;
    public static short BLOCK_LIME_GLAZED_TERACOTTA = 85;
    public static short BLOCK_MAGENTA_CONCRETE = 86;
    public static short BLOCK_MAGENTA_GLAZED_TERACOTTA = 87;
    public static short BLOCK_ORANGE_CONCRETE = 88;
    public static short BLOCK_ORANGE_GLAZED_TERACOTTA = 89;
    public static short BLOCK_PINK_CONCRETE = 90;
    public static short BLOCK_PINK_GLAZED_TERACOTTA = 91;
    public static short BLOCK_PURPLE_CONCRETE = 92;
    public static short BLOCK_PURPLE_GLAZED_TERACOTTA = 93;
    public static short BLOCK_RED_CONCRETE = 94;
    public static short BLOCK_RED_GLAZED_TERACOTTA = 95;
    public static short BLOCK_WHITE_CONCRETE = 96;
    public static short BLOCK_WHITE_GLAZED_TERACOTTA = 97;
    public static short BLOCK_BRAIN_CORAL = 98;
    public static short BLOCK_QUARTZ_PILLAR_BLOCK = 100;
    public static short BLOCK_COBBLESTONE = 101;
    public static short BLOCK_WOOL_TURQUOISE = 102;
    public static short BLOCK_WOOL_ORANGE = 103;
    public static short BLOCK_RED_SAND = 104;
    public static short BLOCK_55_HP_ENGINE = 106;
    public static short BLOCK_PHANTOM_STONE = 107;
    public static short BLOCK_PHANTOM_STONE_BRICK = 108;
    public static short BLOCK_CACTUS = 109;
    public static short BLOCK_PALISADE_STONE = 110;
    public static short BLOCK_RED_SANDSTONE = 111;
    public static short BLOCK_FIRE_CORAL_BLOCK = 112;
    public static short BLOCK_CANDLE = 113;
    public static short BLOCK_PALISADE_STONE_2 = 114;
    public static short BLOCK_FIRE_CORAL = 115;
    public static short BLOCK_TALL_GRASS = 116;
    public static short BLOCK_HORN_CORAL_BLOCK = 117;
    public static short BLOCK_GOLD_BLOCK = 118;
    public static short BLOCK_HORN_CORAL_FAN = 119;
    public static short BLOCK_TNT = 120;
    public static short BLOCK_MINI_CACTUS = 123;
    public static short BLOCK_MUSHROOM = 124;
    public static short BLOCK_MUSHROOM_2 = 125;
    public static short BLOCK_ROSES = 126;
    public static short BLOCK_WOOL_PURPLE = 127;
    public static short BLOCK_BIRCH_WOOD = 128;
    public static short BLOCK_RED_STAINED_WOOD = 129;
    public static short BLOCK_OAK_WOOD = 130;
    public static short BLOCK_WOOL_RED = 131;
    public static short BLOCK_WOOL_PINK = 132;
    public static short BLOCK_WOOL_YELLOW = 133;
    public static short BLOCK_WOOL_BROWN = 134;
    public static short BLOCK_TUBE_CORAL_BLOCK = 135;
    public static short BLOCK_TUBE_CORAL = 136;
    public static short BLOCK_TUBE_CORAL_FAN = 137;
    public static short BLOCK_WOOL_DEEP_BLUE = 138;
    public static short BLOCK_WOOL_SKY_BLUE = 139;
    public static short BLOCK_WOOL_DARK_GREEN = 140;
    public static short BLOCK_WOOL_GREEN = 141;
    public static short BLOCK_WOOL_GRAY = 142;
    public static short BLOCK_BRAIN_CORAL_BLOCK = 143;
    public static short BLOCK_DIAMOND_BLOCK = 144;
    public static short BLOCK_STEEL_BLOCK = 145;
    public static short BLOCK_HONEYCOMB_BLOCK_STAIRS = 147;
    public static short BLOCK_OAK_FENCE = 148;
    public static short BLOCK_BIRCH_FENCE = 149;
    public static short BLOCK_BUBBLE_CORAL_BLOCK = 150;
    public static short BLOCK_BUBBLE_CORAL = 151;
    public static short BLOCK_BUBBLE_CORAL_FAN = 152;
    public static short BLOCK_JUNGLE_GRASS = 153;
    public static short BLOCK_LILY_PAD = 154;
    public static short BLOCK_WOOL_MAGENTA = 156;
    public static short BLOCK_WOOL_BLACK = 157;
    public static short BLOCK_GRANITE_BRICK_STAIRS = 158;
    public static short BLOCK_CEMENT = 159;
    public static short BLOCK_OAK_SAPLING = 160;
    public static short BLOCK_BIRCH_SAPLING = 161;
    public static short BLOCK_METAL_GRATE = 164;
    public static short BLOCK_RED_PALISADE_SANDSTONE = 175;
    public static short BLOCK_PHANTOM_SANDSTONE = 176;
    public static short BLOCK_PALISADE_SANDSTONE = 177;
    public static short BLOCK_GLOW_ROCK = 178;
    public static short BLOCK_BLACK_STAINED_GLASS = 179;
    public static short BLOCK_BLUE_STAINED_GLASS = 180;
    public static short BLOCK_BROWN_STAINED_GLASS = 181;
    public static short BLOCK_CYAN_STAINED_GLASS = 182;
    public static short BLOCK_GRAY_STAINED_GLASS = 183;
    public static short BLOCK_GREEN_STAINED_GLASS = 184;
    public static short BLOCK_LIGHT_BLUE_STAINED_GLASS = 185;
    public static short BLOCK_LIGHT_GRAY_STAINED_GLASS = 186;
    public static short BLOCK_LIME_STAINED_GLASS = 187;
    public static short BLOCK_MAGENTA_STAINED_GLASS = 188;
    public static short BLOCK_ORANGE_STAINED_GLASS = 189;
    public static short BLOCK_PINK_STAINED_GLASS = 190;
    public static short BLOCK_PUMPKIN = 1000;
    public static short BLOCK_PUMPKIN_STEM = 1001;
    public static short BLOCK_PUMPKIN_SEEDS = 1002;
    public static short BLOCK_PURPLE_STAINED_GLASS = 191;
    public static short BLOCK_RED_STAINED_GLASS = 192;
    public static short BLOCK_WHITE_STAINED_GLASS = 193;
    public static short BLOCK_YELLOW_STAINED_GLASS = 194;
    public static short BLOCK_DARK_OAK_FENCE = 195;
    public static short BLOCK_BIRCH_WOOD_STAIRS = 196;
    public static short BLOCK_OAK_WOOD_STAIRS = 197;
    public static short BLOCK_DARK_OAK_WOOD_STAIRS = 198;
    public static short BLOCK_HONEYCOMB_BLOCK_SLAB = 199;
    public static short BLOCK_JUNGLE_GRASS_PLANT = 200;
    public static short BLOCK_PRISMARINE_BRICK_STAIRS = 201;
    public static short BLOCK_SANDSTONE_STAIRS = 202;
    public static short BLOCK_CAVE_VINES_FLAT = 203;
    public static short BLOCK_POLISHED_DIORITE_STAIRS = 204;
    public static short BLOCK_BIRCH_WOOD_SLAB = 205;
    public static short BLOCK_OAK_WOOD_SLAB = 206;
    public static short BLOCK_DARK_OAK_WOOD_SLAB = 207;
    public static short BLOCK_BAMBOO_WOOD_STAIRS = 208;
    public static short BLOCK_STONE_BRICK_SLAB = 209;
    public static short BLOCK_RED_SANDSTONE_SLAB = 210;
    public static short BLOCK_SANDSTONE_SLAB = 211;
    public static short BLOCK_DRY_GRASS = 212;
    public static short BLOCK_POLISHED_ANDESITE_SLAB = 213;
    public static short BLOCK_IRON_LADDER = 214;
    public static short BLOCK_RED_VINES_FLAT = 215;
    public static short BLOCK_YELLOW_CANDLE = 216;
    public static short BLOCK_CAVE_VINES = 217;
    public static short BLOCK_GREEN_CANDLE = 218;
    public static short BLOCK_GRANITE = 219;
    public static short BLOCK_BLUE_CANDLE = 220;
    public static short BLOCK_RED_VINES = 221;
    public static short BLOCK_FLAT_VINES = 222;
    public static short BLOCK_DARK_OAK_LADDER = 223;
    public static short BLOCK_DRY_GRASS_PLANT = 224;
    public static short BLOCK_ACACIA_LEAVES = 225;
    public static short BLOCK_ACACIA_LOG = 226;
    public static short BLOCK_FIRE_CORAL_FAN = 227;
    public static short BLOCK_LAPIS_LAZUL_BLOCK = 229;
    public static short BLOCK_ACACIA_SAPLING = 230;
    public static short BLOCK_BRAIN_CORAL_FAN = 232;
    public static short BLOCK_WHITE_ROSE = 233;
    public static short BLOCK_HORN_CORAL = 235;
    public static short BLOCK_RED_ROSE = 236;
    public static short BLOCK_EMERALD_BLOCK = 238;
    public static short BLOCK_BLACK_EYE_SUSAN = 239;
    public static short BLOCK_ORANGE_TULIP = 240;
    public static short BLOCK_DEAD_BUSH = 241;
    public static short BLOCK_HAY_BAIL = 242;
    public static short BLOCK_POLISHED_ANDESITE_STAIRS = 243;
    public static short BLOCK_POLISHED_DIORITE_SLAB = 244;
    public static short BLOCK_CURVED_TRACK = 245;
    public static short BLOCK_BEETS = 246;
    public static short BLOCK_BAMBOO_LADDER = 248;
    public static short BLOCK_ACACIA_FENCE = 249;
    public static short BLOCK_ACACIA_WOOD_STAIRS = 250;
    public static short BLOCK_ACACIA_WOOD_SLAB = 251;
    public static short BLOCK_RAISED_TRACK = 252;
    public static short BLOCK_BLUE_STAINED_WOOD = 254;
    public static short BLOCK_RUBY_CRYSTAL = 255;
    public static short BLOCK_JADE_CRYSTAL = 256;
    public static short BLOCK_AQUAMARINE_CRYSTAL = 257;
    public static short BLOCK_BAMBOO_WOOD_SLAB = 258;
    public static short BLOCK_RED_SANDSTONE_STAIRS = 259;
    public static short BLOCK_STONE_BRICK_STAIRS = 260;
    public static short BLOCK_DRIVERS_SEAT = 261;
    public static short BLOCK_STONE_BRICK_FENCE = 262;
    public static short BLOCK_BRICK_STAIRS = 263;
    public static short BLOCK_BRICK_SLAB = 264;
    public static short BLOCK_SNOW_BLOCK = 265;
    public static short BLOCK_COBBLESTONE_STAIRS = 266;
    public static short BLOCK_COBBLESTONE_SLAB = 267;
    public static short BLOCK_PALISADE_STONE_STAIRS = 268;
    public static short BLOCK_PALISADE_STONE_SLAB = 269;
    public static short BLOCK_PALISADE_STONE_FENCE = 270;
    public static short BLOCK_PALISADE_STONE_2_STAIRS = 271;
    public static short BLOCK_PALISADE_STONE_2_SLAB = 272;
    public static short BLOCK_PALISADE_STONE_2_FENCE = 273;
    public static short BLOCK_POLISHED_DIORITE_FENCE = 274;
    public static short BLOCK_POLISHED_ANDESITE_FENCE = 275;
    public static short BLOCK_CRACKED_STONE_STAIRS = 276;
    public static short BLOCK_CRACKED_STONE_SLAB = 277;
    public static short BLOCK_CRACKED_STONE_FENCE = 278;
    public static short BLOCK_STONE_WITH_VINES_STAIRS = 279;
    public static short BLOCK_STONE_WITH_VINES_SLAB = 280;
    public static short BLOCK_STONE_WITH_VINES_FENCE = 281;
    public static short BLOCK_BURGUNDY_BRICK_STAIRS = 282;
    public static short BLOCK_BURGUNDY_BRICK_SLAB = 283;
    public static short BLOCK_BURGUNDY_BRICK_FENCE = 284;
    public static short BLOCK_SWITCH_JUNCTION = 285;
    public static short BLOCK_TRACK_STOP = 286;
    public static short BLOCK_RED_PALISADE_SANDSTONE_STAIRS = 287;
    public static short BLOCK_RED_PALISADE_SANDSTONE_SLAB = 288;
    public static short BLOCK_RED_PALISADE_SANDSTONE_FENCE = 289;
    public static short BLOCK_PALISADE_SANDSTONE_STAIRS = 290;
    public static short BLOCK_PALISADE_SANDSTONE_SLAB = 291;
    public static short BLOCK_PALISADE_SANDSTONE_FENCE = 292;
    public static short BLOCK_WOOL_STAIRS = 293;
    public static short BLOCK_WOOL_SLAB = 294;
    public static short BLOCK_WOOL_GRAY_STAIRS = 295;
    public static short BLOCK_WOOL_GRAY_SLAB = 296;
    public static short BLOCK_WOOL_RED_STAIRS = 297;
    public static short BLOCK_WOOL_RED_SLAB = 298;
    public static short BLOCK_WOOL_PINK_STAIRS = 299;
    public static short BLOCK_WOOL_PINK_SLAB = 300;
    public static short BLOCK_WOOL_ORANGE_STAIRS = 301;
    public static short BLOCK_WOOL_ORANGE_SLAB = 302;
    public static short BLOCK_WOOL_YELLOW_STAIRS = 303;
    public static short BLOCK_WOOL_YELLOW_SLAB = 304;
    public static short BLOCK_WOOL_GREEN_STAIRS = 305;
    public static short BLOCK_WOOL_GREEN_SLAB = 306;
    public static short BLOCK_WOOL_DARK_GREEN_STAIRS = 307;
    public static short BLOCK_WOOL_DARK_GREEN_SLAB = 308;
    public static short BLOCK_WOOL_TURQUOISE_STAIRS = 309;
    public static short BLOCK_WOOL_TURQUOISE_SLAB = 310;
    public static short BLOCK_WOOL_DEEP_BLUE_STAIRS = 311;
    public static short BLOCK_WOOL_DEEP_BLUE_SLAB = 312;
    public static short BLOCK_WOOL_SKY_BLUE_STAIRS = 313;
    public static short BLOCK_WOOL_SKY_BLUE_SLAB = 314;
    public static short BLOCK_WOOL_BROWN_STAIRS = 315;
    public static short BLOCK_WOOL_BROWN_SLAB = 316;
    public static short BLOCK_WOOL_PURPLE_STAIRS = 317;
    public static short BLOCK_WOOL_PURPLE_SLAB = 318;
    public static short BLOCK_WOOL_MAGENTA_STAIRS = 319;
    public static short BLOCK_WOOL_MAGENTA_SLAB = 320;
    public static short BLOCK_WOOL_BLACK_STAIRS = 321;
    public static short BLOCK_WOOL_BLACK_SLAB = 322;
    public static short BLOCK_YELLOW_CONCRETE_STAIRS = 323;
    public static short BLOCK_YELLOW_CONCRETE_SLAB = 324;
    public static short BLOCK_YELLOW_CONCRETE_FENCE = 325;
    public static short BLOCK_BLACK_CONCRETE_STAIRS = 326;
    public static short BLOCK_BLACK_CONCRETE_SLAB = 327;
    public static short BLOCK_BLACK_CONCRETE_FENCE = 328;
    public static short BLOCK_BLUE_CONCRETE_STAIRS = 329;
    public static short BLOCK_BLUE_CONCRETE_SLAB = 330;
    public static short BLOCK_BLUE_CONCRETE_FENCE = 331;
    public static short BLOCK_BROWN_CONCRETE_STAIRS = 332;
    public static short BLOCK_BROWN_CONCRETE_SLAB = 333;
    public static short BLOCK_BROWN_CONCRETE_FENCE = 334;
    public static short BLOCK_CYAN_CONCRETE_STAIRS = 335;
    public static short BLOCK_CYAN_CONCRETE_SLAB = 336;
    public static short BLOCK_CYAN_CONCRETE_FENCE = 337;
    public static short BLOCK_GRAY_CONCRETE_STAIRS = 338;
    public static short BLOCK_GRAY_CONCRETE_SLAB = 339;
    public static short BLOCK_GRAY_CONCRETE_FENCE = 340;
    public static short BLOCK_GREEN_CONCRETE_STAIRS = 341;
    public static short BLOCK_GREEN_CONCRETE_SLAB = 342;
    public static short BLOCK_GREEN_CONCRETE_FENCE = 343;
    public static short BLOCK_LIGHT_BLUE_CONCRETE_STAIRS = 344;
    public static short BLOCK_LIGHT_BLUE_CONCRETE_SLAB = 345;
    public static short BLOCK_LIGHT_BLUE_CONCRETE_FENCE = 346;
    public static short BLOCK_LIGHT_GRAY_CONCRETE_STAIRS = 347;
    public static short BLOCK_LIGHT_GRAY_CONCRETE_SLAB = 348;
    public static short BLOCK_LIGHT_GRAY_CONCRETE_FENCE = 349;
    public static short BLOCK_LIME_CONCRETE_STAIRS = 350;
    public static short BLOCK_LIME_CONCRETE_SLAB = 351;
    public static short BLOCK_LIME_CONCRETE_FENCE = 352;
    public static short BLOCK_MAGENTA_CONCRETE_STAIRS = 353;
    public static short BLOCK_MAGENTA_CONCRETE_SLAB = 354;
    public static short BLOCK_MAGENTA_CONCRETE_FENCE = 355;
    public static short BLOCK_ORANGE_CONCRETE_STAIRS = 356;
    public static short BLOCK_ORANGE_CONCRETE_SLAB = 357;
    public static short BLOCK_ORANGE_CONCRETE_FENCE = 358;
    public static short BLOCK_PINK_CONCRETE_STAIRS = 359;
    public static short BLOCK_PINK_CONCRETE_SLAB = 360;
    public static short BLOCK_PINK_CONCRETE_FENCE = 361;
    public static short BLOCK_PURPLE_CONCRETE_STAIRS = 362;
    public static short BLOCK_PURPLE_CONCRETE_SLAB = 363;
    public static short BLOCK_PURPLE_CONCRETE_FENCE = 364;
    public static short BLOCK_RED_CONCRETE_STAIRS = 365;
    public static short BLOCK_RED_CONCRETE_SLAB = 366;
    public static short BLOCK_RED_CONCRETE_FENCE = 367;
    public static short BLOCK_WHITE_CONCRETE_STAIRS = 368;
    public static short BLOCK_WHITE_CONCRETE_SLAB = 369;
    public static short BLOCK_WHITE_CONCRETE_FENCE = 370;
    public static short BLOCK_YELLOW_GLAZED_TERACOTTA_STAIRS = 371;
    public static short BLOCK_YELLOW_GLAZED_TERACOTTA_SLAB = 372;
    public static short BLOCK_BLACK_GLAZED_TERACOTTA_STAIRS = 373;
    public static short BLOCK_BLACK_GLAZED_TERACOTTA_SLAB = 374;
    public static short BLOCK_BLUE_GLAZED_TERACOTTA_STAIRS = 375;
    public static short BLOCK_BLUE_GLAZED_TERACOTTA_SLAB = 376;
    public static short BLOCK_BROWN_GLAZED_TERACOTTA_STAIRS = 377;
    public static short BLOCK_BROWN_GLAZED_TERACOTTA_SLAB = 378;
    public static short BLOCK_CYAN_GLAZED_TERACOTTA_STAIRS = 379;
    public static short BLOCK_CYAN_GLAZED_TERACOTTA_SLAB = 380;
    public static short BLOCK_GRAY_GLAZED_TERACOTTA_STAIRS = 381;
    public static short BLOCK_GRAY_GLAZED_TERACOTTA_SLAB = 382;
    public static short BLOCK_GREEN_GLAZED_TERACOTTA_STAIRS = 383;
    public static short BLOCK_GREEN_GLAZED_TERACOTTA_SLAB = 384;
    public static short BLOCK_LIGHT_BLUE_GLAZED_TERACOTTA_STAIRS = 385;
    public static short BLOCK_LIGHT_BLUE_GLAZED_TERACOTTA_SLAB = 386;
    public static short BLOCK_LIGHT_GRAY_GLAZED_TERACOTTA_STAIRS = 387;
    public static short BLOCK_LIGHT_GRAY_GLAZED_TERACOTTA_SLAB = 388;
    public static short BLOCK_LIME_GLAZED_TERACOTTA_STAIRS = 389;
    public static short BLOCK_LIME_GLAZED_TERACOTTA_SLAB = 390;
    public static short BLOCK_MAGENTA_GLAZED_TERACOTTA_STAIRS = 391;
    public static short BLOCK_MAGENTA_GLAZED_TERACOTTA_SLAB = 392;
    public static short BLOCK_ORANGE_GLAZED_TERACOTTA_STAIRS = 393;
    public static short BLOCK_ORANGE_GLAZED_TERACOTTA_SLAB = 394;
    public static short BLOCK_PINK_GLAZED_TERACOTTA_STAIRS = 395;
    public static short BLOCK_PINK_GLAZED_TERACOTTA_SLAB = 396;
    public static short BLOCK_PURPLE_GLAZED_TERACOTTA_STAIRS = 397;
    public static short BLOCK_PURPLE_GLAZED_TERACOTTA_SLAB = 398;
    public static short BLOCK_RED_GLAZED_TERACOTTA_STAIRS = 399;
    public static short BLOCK_RED_GLAZED_TERACOTTA_SLAB = 400;
    public static short BLOCK_WHITE_GLAZED_TERACOTTA_STAIRS = 401;
    public static short BLOCK_WHITE_GLAZED_TERACOTTA_SLAB = 402;
    public static short BLOCK_OAK_LADDER = 403;
    public static short BLOCK_MINECART_ROAD_BLOCK = 404;
    public static short BLOCK_STONE = 405;
    public static short BLOCK_BAMBOO_WOOD_FENCE = 406;
    public static short BLOCK_CROISSANT = 407;
    public static short BLOCK_MINECART_ROAD_SLAB = 408;
    public static short BLOCK_PRISMARINE_BRICKS = 409;
    public static short BLOCK_DARK_PRISMARINE_BRICKS = 410;
    public static short BLOCK_GLASS_STAIRS = 411;
    public static short BLOCK_GLASS_SLAB = 412;
    public static short BLOCK_BLUE_TORCH = 413;
    public static short BLOCK_CEMENT_STAIRS = 414;
    public static short BLOCK_CEMENT_FENCE = 415;
    public static short BLOCK_CEMENT_SLAB = 416;
    public static short BLOCK_OBSIDIAN_SLAB = 417;
    public static short BLOCK_OBSIDIAN_FENCE = 418;
    public static short BLOCK_LAPIS_LAZUL_STAIRS = 419;
    public static short BLOCK_LAPIS_LAZUL_BLOCK_SLAB = 420;
    public static short BLOCK_LAPIS_LAZUL_FENCE = 421;
    public static short BLOCK_STEEL_STAIRS = 422;
    public static short BLOCK_STEEL_BLOCK_SLAB = 423;
    public static short BLOCK_STEEL_FENCE = 424;
    public static short BLOCK_GOLD_STAIRS = 425;
    public static short BLOCK_GOLD_BLOCK_SLAB = 426;
    public static short BLOCK_GOLD_FENCE = 427;
    public static short BLOCK_EMERALD_STAIRS = 428;
    public static short BLOCK_EMERALD_BLOCK_SLAB = 429;
    public static short BLOCK_EMERALD_FENCE = 430;
    public static short BLOCK_DIAMOND_STAIRS = 431;
    public static short BLOCK_DIAMOND_BLOCK_SLAB = 432;
    public static short BLOCK_DIAMOND_FENCE = 433;
    public static short BLOCK_CROSSWALK_PAINT = 434;
    public static short BLOCK_DARK_PRISMARINE_BRICK_STAIRS = 435;
    public static short BLOCK_PRISMARINE_BRICK_SLAB = 436;
    public static short BLOCK_OBSIDIAN_STAIRS = 437;
    public static short BLOCK_PRISMARINE_BRICK_FENCE = 438;
    public static short BLOCK_RED_SANDSTONE_PILLAR = 439;
    public static short BLOCK_STONE_BRICK_PILLAR = 440;
    public static short BLOCK_PALISADE_SANDSTONE_PILLAR = 441;
    public static short BLOCK_PALISADE_STONE_PILLAR = 442;
    public static short BLOCK_PALISADE_STONE_2_PILLAR = 443;
    public static short BLOCK_CRACKED_STONE_PILLAR = 444;
    public static short BLOCK_STONE_WITH_VINES_PILLAR = 445;
    public static short BLOCK_FLAT_DRAGON_VINES = 446;
    public static short BLOCK_DRAGON_VINES = 447;
    public static short BLOCK_RED_PALISADE_SANDSTONE_PILLAR = 448;
    public static short BLOCK_MARBLE_PILLAR_BLOCK = 449;
    public static short BLOCK_QUARTZ_PILLAR = 450;
    public static short BLOCK_MARBLE_PILLAR = 451;
    public static short BLOCK_FARMLAND = 452;
    public static short BLOCK_WET_FARMLAND = 743;
    public static short BLOCK_ROAD_MARKINGS = 453;
    public static short BLOCK_GRANITE_BRICK_PILLAR = 454;
    public static short BLOCK_GRANITE_BRICK_SLAB = 455;
    public static short BLOCK_GRANITE_BRICK_FENCE = 456;
    public static short BLOCK_FIRE = 457;
    public static short BLOCK_DARK_PRISMARINE_BRICK_SLAB = 458;
    public static short BLOCK_DARK_PRISMARINE_BRICK_FENCE = 459;
    public static short BLOCK_ENGRAVED_SANDSTONE = 460;
    public static short BLOCK_ENGRAVED_RED_SANDSTONE = 461;
    public static short BLOCK_ORANGE_MARBLE_TILE = 462;
    public static short BLOCK_CHECKERBOARD_CHISELED_MARBLE = 463;
    public static short BLOCK_CHISELED_MARBLE = 464;
    public static short BLOCK_CHISELED_QUARTZ = 465;
    public static short BLOCK_MARBLE_TILE = 466;
    public static short BLOCK_BLUE_MARBLE_TILE = 467;
    public static short BLOCK_GREEN_MARBLE_TILE = 468;
    public static short BLOCK_ORANGE_MARBLE_TILE_SLAB = 469;
    public static short BLOCK_GRAY_MARBLE_TILE = 470;
    public static short BLOCK_ORANGE_MARBLE_TILE_STAIRS = 471;
    public static short BLOCK_GREEN_MARBLE_TILE_SLAB = 472;
    public static short BLOCK_YELLOW_STAINED_GLASS_STAIRS = 473;
    public static short BLOCK_YELLOW_STAINED_GLASS_SLAB = 474;
    public static short BLOCK_BLACK_STAINED_GLASS_STAIRS = 475;
    public static short BLOCK_BLACK_STAINED_GLASS_SLAB = 476;
    public static short BLOCK_BLUE_STAINED_GLASS_STAIRS = 477;
    public static short BLOCK_BLUE_STAINED_GLASS_SLAB = 478;
    public static short BLOCK_BROWN_STAINED_GLASS_STAIRS = 479;
    public static short BLOCK_BROWN_STAINED_GLASS_SLAB = 480;
    public static short BLOCK_CYAN_STAINED_GLASS_STAIRS = 481;
    public static short BLOCK_CYAN_STAINED_GLASS_SLAB = 482;
    public static short BLOCK_GRAY_STAINED_GLASS_STAIRS = 483;
    public static short BLOCK_GRAY_STAINED_GLASS_SLAB = 484;
    public static short BLOCK_GREEN_STAINED_GLASS_STAIRS = 485;
    public static short BLOCK_GREEN_STAINED_GLASS_SLAB = 486;
    public static short BLOCK_LIGHT_BLUE_STAINED_GLASS_STAIRS = 487;
    public static short BLOCK_LIGHT_BLUE_STAINED_GLASS_SLAB = 488;
    public static short BLOCK_LIGHT_GRAY_STAINED_GLASS_STAIRS = 489;
    public static short BLOCK_LIGHT_GRAY_STAINED_GLASS_SLAB = 490;
    public static short BLOCK_LIME_STAINED_GLASS_STAIRS = 491;
    public static short BLOCK_LIME_STAINED_GLASS_SLAB = 492;
    public static short BLOCK_MAGENTA_STAINED_GLASS_STAIRS = 493;
    public static short BLOCK_MAGENTA_STAINED_GLASS_SLAB = 494;
    public static short BLOCK_ORANGE_STAINED_GLASS_STAIRS = 495;
    public static short BLOCK_ORANGE_STAINED_GLASS_SLAB = 496;
    public static short BLOCK_PINK_STAINED_GLASS_STAIRS = 497;
    public static short BLOCK_PINK_STAINED_GLASS_SLAB = 498;
    public static short BLOCK_PURPLE_STAINED_GLASS_STAIRS = 499;
    public static short BLOCK_PURPLE_STAINED_GLASS_SLAB = 500;
    public static short BLOCK_RED_STAINED_GLASS_STAIRS = 501;
    public static short BLOCK_RED_STAINED_GLASS_SLAB = 502;
    public static short BLOCK_WHITE_STAINED_GLASS_STAIRS = 503;
    public static short BLOCK_WHITE_STAINED_GLASS_SLAB = 504;
    public static short BLOCK_CHECKERBOARD_CHISELED_MARBLE_STAIRS = 505;
    public static short BLOCK_CHECKERBOARD_CHISELED_MARBLE_SLAB = 506;
    public static short BLOCK_CHISELED_MARBLE_STAIRS = 507;
    public static short BLOCK_CHISELED_MARBLE_SLAB = 508;
    public static short BLOCK_MARBLE_TILE_STAIRS = 509;
    public static short BLOCK_MARBLE_TILE_SLAB = 510;
    public static short BLOCK_GRAY_MARBLE_TILE_STAIRS = 511;
    public static short BLOCK_GRAY_MARBLE_TILE_SLAB = 512;
    public static short BLOCK_BLUE_MARBLE_TILE_STAIRS = 513;
    public static short BLOCK_BLUE_MARBLE_TILE_SLAB = 514;
    public static short BLOCK_GREEN_MARBLE_TILE_STAIRS = 515;
    public static short BLOCK_CHISELED_QUARTZ_STAIRS = 516;
    public static short BLOCK_CHISELED_QUARTZ_SLAB = 517;
    public static short BLOCK_CHISELED_QUARTZ_PILLAR = 518;
    public static short BLOCK_GRAY_MARBLE_TILE_PILLAR = 519;
    public static short BLOCK_BLUE_MARBLE_TILE_PILLAR = 520;
    public static short BLOCK_GREEN_MARBLE_TILE_PILLAR = 521;
    public static short BLOCK_ORANGE_MARBLE_TILE_PILLAR = 522;
    public static short BLOCK_MARBLE_TILE_PILLAR = 523;
    public static short BLOCK_LAMP = 524;
    public static short BLOCK_BLUE_LAMP = 525;
    public static short BLOCK_GLASS_PANE = 526;
    public static short BLOCK_YELLOW_STAINED_GLASS_PANE = 527;
    public static short BLOCK_BLACK_STAINED_GLASS_PANE = 528;
    public static short BLOCK_BLUE_STAINED_GLASS_PANE = 529;
    public static short BLOCK_BROWN_STAINED_GLASS_PANE = 530;
    public static short BLOCK_CYAN_STAINED_GLASS_PANE = 531;
    public static short BLOCK_GRAY_STAINED_GLASS_PANE = 532;
    public static short BLOCK_GREEN_STAINED_GLASS_PANE = 533;
    public static short BLOCK_LIGHT_BLUE_STAINED_GLASS_PANE = 534;
    public static short BLOCK_LIGHT_GRAY_STAINED_GLASS_PANE = 535;
    public static short BLOCK_LIME_STAINED_GLASS_PANE = 536;
    public static short BLOCK_MAGENTA_STAINED_GLASS_PANE = 537;
    public static short BLOCK_ORANGE_STAINED_GLASS_PANE = 538;
    public static short BLOCK_PINK_STAINED_GLASS_PANE = 539;
    public static short BLOCK_PURPLE_STAINED_GLASS_PANE = 540;
    public static short BLOCK_RED_STAINED_GLASS_PANE = 541;
    public static short BLOCK_WHITE_STAINED_GLASS_PANE = 542;
    public static short BLOCK_START_BOUNDARY = 543;
    public static short BLOCK_YELLOW_CHISELED_MARBLE_TILE = 545;
    public static short BLOCK_SUNFLOWER = 546;
    public static short BLOCK_SUNFLOWER_STALK = 547;
    public static short BLOCK_MEGA_TNT = 548;
    public static short BLOCK_CROSSTRACK = 550;
    public static short BLOCK_BLACK_CHISELED_MARBLE_TILE = 553;
    public static short BLOCK_BLUE_CHISELED_MARBLE_TILE = 554;
    public static short BLOCK_BROWN_CHISELED_MARBLE_TILE = 555;
    public static short BLOCK_CYAN_CHISELED_MARBLE_TILE = 556;
    public static short BLOCK_GRAY_CHISELED_MARBLE_TILE = 557;
    public static short BLOCK_GREEN_CHISELED_MARBLE_TILE = 558;
    public static short BLOCK_PASTEL_BLUE_CHISELED_MARBLE_TILE = 559;
    public static short BLOCK_PASTEL_GREEN_CHISELED_MARBLE_TILE = 560;
    public static short BLOCK_MAGENTA_CHISELED_MARBLE_TILE = 561;
    public static short BLOCK_ORANGE_CHISELED_MARBLE_TILE = 562;
    public static short BLOCK_PINK_CHISELED_MARBLE_TILE = 563;
    public static short BLOCK_PURPLE_CHISELED_MARBLE_TILE = 564;
    public static short BLOCK_BURGUNDY_CHISELED_MARBLE_TILE = 565;
    public static short BLOCK_BAMBOO_BLOCK = 566;
    public static short BLOCK_PASTEL_RED_CHISELED_MARBLE_TILE = 567;
    public static short BLOCK_YELLOW_MARBLE_TILE = 568;
    public static short BLOCK_BLACK_MARBLE_TILE = 569;
    public static short BLOCK_ICE_BLOCK = 570;
    public static short BLOCK_BROWN_MARBLE_TILE = 571;
    public static short BLOCK_CYAN_MARBLE_TILE = 572;
    public static short BLOCK_BAMBOO_WOOD = 573;
    public static short BLOCK_BOTTLE = 574;
    public static short BLOCK_PASTEL_BLUE_MARBLE_TILE = 575;
    public static short BLOCK_PASTEL_GREEN_MARBLE_TILE = 576;
    public static short BLOCK_MAGENTA_MARBLE_TILE = 577;
    public static short BLOCK_CUP = 578;
    public static short BLOCK_PINK_MARBLE_TILE = 579;
    public static short BLOCK_PURPLE_MARBLE_TILE = 580;
    public static short BLOCK_BURGUNDY_MARBLE_TILE = 581;
    public static short BLOCK_PASTEL_RED_MARBLE_TILE = 582;
    public static short BLOCK_WINE_GLASS = 583;
    public static short BLOCK_YELLOW_STAINED_WOOD = 584;
    public static short BLOCK_BLACK_STAINED_WOOD = 585;
    public static short BLOCK_YELLOW_MARBLE_TILE_STAIRS = 586;
    public static short BLOCK_YELLOW_MARBLE_TILE_SLAB = 587;
    public static short BLOCK_YELLOW_MARBLE_TILE_PILLAR = 588;
    public static short BLOCK_BLACK_MARBLE_TILE_STAIRS = 589;
    public static short BLOCK_BLACK_MARBLE_TILE_SLAB = 590;
    public static short BLOCK_BLACK_MARBLE_TILE_PILLAR = 591;
    public static short BLOCK_BREAD = 592;
    public static short BLOCK_CYAN_STAINED_WOOD = 593;
    public static short BLOCK_PINK_CHISELED_MARBLE_TILE_PILLAR = 594;
    public static short BLOCK_BROWN_MARBLE_TILE_STAIRS = 595;
    public static short BLOCK_BROWN_MARBLE_TILE_SLAB = 596;
    public static short BLOCK_BROWN_MARBLE_TILE_PILLAR = 597;
    public static short BLOCK_CYAN_MARBLE_TILE_STAIRS = 598;
    public static short BLOCK_CYAN_MARBLE_TILE_SLAB = 599;
    public static short BLOCK_CYAN_MARBLE_TILE_PILLAR = 600;
    public static short BLOCK_PURPLE_CHISELED_MARBLE_TILE_STAIRS = 601;
    public static short BLOCK_PURPLE_CHISELED_MARBLE_TILE_SLAB = 602;
    public static short BLOCK_PURPLE_CHISELED_MARBLE_TILE_PILLAR = 603;
    public static short BLOCK_BURGUNDY_CHISELED_MARBLE_TILE_STAIRS = 604;
    public static short BLOCK_BURGUNDY_CHISELED_MARBLE_TILE_SLAB = 605;
    public static short BLOCK_BURGUNDY_CHISELED_MARBLE_TILE_PILLAR = 606;
    public static short BLOCK_PASTEL_BLUE_MARBLE_TILE_STAIRS = 607;
    public static short BLOCK_PASTEL_BLUE_MARBLE_TILE_SLAB = 608;
    public static short BLOCK_PASTEL_BLUE_MARBLE_TILE_PILLAR = 609;
    public static short BLOCK_PASTEL_GREEN_MARBLE_TILE_STAIRS = 610;
    public static short BLOCK_PASTEL_GREEN_MARBLE_TILE_SLAB = 611;
    public static short BLOCK_PASTEL_GREEN_MARBLE_TILE_PILLAR = 612;
    public static short BLOCK_MAGENTA_MARBLE_TILE_STAIRS = 613;
    public static short BLOCK_MAGENTA_MARBLE_TILE_SLAB = 614;
    public static short BLOCK_MAGENTA_MARBLE_TILE_PILLAR = 615;
    public static short BLOCK_PASTEL_RED_CHISELED_MARBLE_TILE_STAIRS = 616;
    public static short BLOCK_PASTEL_RED_CHISELED_MARBLE_TILE_SLAB = 617;
    public static short BLOCK_PASTEL_RED_CHISELED_MARBLE_TILE_PILLAR = 618;
    public static short BLOCK_PINK_MARBLE_TILE_STAIRS = 619;
    public static short BLOCK_PINK_MARBLE_TILE_SLAB = 620;
    public static short BLOCK_PINK_MARBLE_TILE_PILLAR = 621;
    public static short BLOCK_PURPLE_MARBLE_TILE_STAIRS = 622;
    public static short BLOCK_PURPLE_MARBLE_TILE_SLAB = 623;
    public static short BLOCK_PURPLE_MARBLE_TILE_PILLAR = 624;
    public static short BLOCK_BURGUNDY_MARBLE_TILE_STAIRS = 625;
    public static short BLOCK_BURGUNDY_MARBLE_TILE_SLAB = 626;
    public static short BLOCK_BURGUNDY_MARBLE_TILE_PILLAR = 627;
    public static short BLOCK_PASTEL_RED_MARBLE_TILE_STAIRS = 628;
    public static short BLOCK_PASTEL_RED_MARBLE_TILE_SLAB = 629;
    public static short BLOCK_PASTEL_RED_MARBLE_TILE_PILLAR = 630;
    public static short BLOCK_YELLOW_CHISELED_MARBLE_TILE_STAIRS = 631;
    public static short BLOCK_YELLOW_CHISELED_MARBLE_TILE_SLAB = 632;
    public static short BLOCK_YELLOW_CHISELED_MARBLE_TILE_PILLAR = 633;
    public static short BLOCK_BLACK_CHISELED_MARBLE_TILE_STAIRS = 634;
    public static short BLOCK_BLACK_CHISELED_MARBLE_TILE_SLAB = 635;
    public static short BLOCK_BLACK_CHISELED_MARBLE_TILE_PILLAR = 636;
    public static short BLOCK_BLUE_CHISELED_MARBLE_TILE_STAIRS = 637;
    public static short BLOCK_BLUE_CHISELED_MARBLE_TILE_SLAB = 638;
    public static short BLOCK_BLUE_CHISELED_MARBLE_TILE_PILLAR = 639;
    public static short BLOCK_BROWN_CHISELED_MARBLE_TILE_STAIRS = 640;
    public static short BLOCK_BROWN_CHISELED_MARBLE_TILE_SLAB = 641;
    public static short BLOCK_BROWN_CHISELED_MARBLE_TILE_PILLAR = 642;
    public static short BLOCK_CYAN_CHISELED_MARBLE_TILE_STAIRS = 643;
    public static short BLOCK_CYAN_CHISELED_MARBLE_TILE_SLAB = 644;
    public static short BLOCK_CYAN_CHISELED_MARBLE_TILE_PILLAR = 645;
    public static short BLOCK_GRAY_CHISELED_MARBLE_TILE_STAIRS = 646;
    public static short BLOCK_GRAY_CHISELED_MARBLE_TILE_SLAB = 647;
    public static short BLOCK_GRAY_CHISELED_MARBLE_TILE_PILLAR = 648;
    public static short BLOCK_GREEN_CHISELED_MARBLE_TILE_STAIRS = 649;
    public static short BLOCK_GREEN_CHISELED_MARBLE_TILE_SLAB = 650;
    public static short BLOCK_GREEN_CHISELED_MARBLE_TILE_PILLAR = 651;
    public static short BLOCK_PASTEL_BLUE_CHISELED_MARBLE_TILE_STAIRS = 652;
    public static short BLOCK_PASTEL_BLUE_CHISELED_MARBLE_TILE_SLAB = 653;
    public static short BLOCK_PASTEL_BLUE_CHISELED_MARBLE_TILE_PILLAR = 654;
    public static short BLOCK_PASTEL_GREEN_CHISELED_MARBLE_TILE_STAIRS = 655;
    public static short BLOCK_PASTEL_GREEN_CHISELED_MARBLE_TILE_SLAB = 656;
    public static short BLOCK_PASTEL_GREEN_CHISELED_MARBLE_TILE_PILLAR = 657;
    public static short BLOCK_MAGENTA_CHISELED_MARBLE_TILE_STAIRS = 658;
    public static short BLOCK_MAGENTA_CHISELED_MARBLE_TILE_SLAB = 659;
    public static short BLOCK_MAGENTA_CHISELED_MARBLE_TILE_PILLAR = 660;
    public static short BLOCK_ORANGE_CHISELED_MARBLE_TILE_STAIRS = 661;
    public static short BLOCK_ORANGE_CHISELED_MARBLE_TILE_SLAB = 662;
    public static short BLOCK_ORANGE_CHISELED_MARBLE_TILE_PILLAR = 663;
    public static short BLOCK_PINK_CHISELED_MARBLE_TILE_STAIRS = 664;
    public static short BLOCK_PINK_CHISELED_MARBLE_TILE_SLAB = 665;
    public static short BLOCK_GRAY_STAINED_WOOD = 666;
    public static short BLOCK_GREEN_STAINED_WOOD = 667;
    public static short BLOCK_LIGHT_BLUE_STAINED_WOOD = 668;
    public static short BLOCK_LIME_STAINED_WOOD = 669;
    public static short BLOCK_MAGENTA_STAINED_WOOD = 670;
    public static short BLOCK_ORANGE_STAINED_WOOD = 671;
    public static short BLOCK_PINK_STAINED_WOOD = 672;
    public static short BLOCK_PURPLE_STAINED_WOOD = 673;
    public static short BLOCK_WHITE_SPACE_TILE_STAIRS = 674;
    public static short BLOCK_WHITE_STAINED_WOOD = 675;
    public static short BLOCK_ENGRAVED_SANDSTONE_2 = 676;
    public static short BLOCK_ENGRAVED_RED_SANDSTONE_2 = 677;
    public static short BLOCK_PEONY_BUSH = 678;
    public static short BLOCK_ICE_BLOCK_STAIRS = 679;
    public static short BLOCK_ICE_BLOCK_SLAB = 680;
    public static short BLOCK_SOLAR_PANEL = 681;
    public static short BLOCK_MOSAIC_BAMBOO_WOOD_STAIRS = 682;
    public static short BLOCK_MOSAIC_BAMBOO_WOOD_SLAB = 683;
    public static short BLOCK_MOSAIC_BAMBOO_WOOD_FENCE = 684;
    public static short BLOCK_YELLOW_STAINED_WOOD_STAIRS = 685;
    public static short BLOCK_YELLOW_STAINED_WOOD_SLAB = 686;
    public static short BLOCK_YELLOW_STAINED_WOOD_FENCE = 687;
    public static short BLOCK_BLACK_STAINED_WOOD_STAIRS = 688;
    public static short BLOCK_BLACK_STAINED_WOOD_SLAB = 689;
    public static short BLOCK_BLACK_STAINED_WOOD_FENCE = 690;
    public static short BLOCK_BLUE_STAINED_WOOD_STAIRS = 691;
    public static short BLOCK_BLUE_STAINED_WOOD_SLAB = 692;
    public static short BLOCK_BLUE_STAINED_WOOD_FENCE = 693;
    public static short BLOCK_CYAN_STAINED_WOOD_STAIRS = 694;
    public static short BLOCK_CYAN_STAINED_WOOD_SLAB = 695;
    public static short BLOCK_CYAN_STAINED_WOOD_FENCE = 696;
    public static short BLOCK_GRAY_STAINED_WOOD_STAIRS = 697;
    public static short BLOCK_GRAY_STAINED_WOOD_SLAB = 698;
    public static short BLOCK_GRAY_STAINED_WOOD_FENCE = 699;
    public static short BLOCK_GREEN_STAINED_WOOD_STAIRS = 700;
    public static short BLOCK_GREEN_STAINED_WOOD_SLAB = 701;
    public static short BLOCK_GREEN_STAINED_WOOD_FENCE = 702;
    public static short BLOCK_LIGHT_BLUE_STAINED_WOOD_STAIRS = 703;
    public static short BLOCK_LIGHT_BLUE_STAINED_WOOD_SLAB = 704;
    public static short BLOCK_LIGHT_BLUE_STAINED_WOOD_FENCE = 705;
    public static short BLOCK_LIME_STAINED_WOOD_STAIRS = 706;
    public static short BLOCK_LIME_STAINED_WOOD_SLAB = 707;
    public static short BLOCK_LIME_STAINED_WOOD_FENCE = 708;
    public static short BLOCK_MAGENTA_STAINED_WOOD_STAIRS = 709;
    public static short BLOCK_MAGENTA_STAINED_WOOD_SLAB = 710;
    public static short BLOCK_MAGENTA_STAINED_WOOD_FENCE = 711;
    public static short BLOCK_ORANGE_STAINED_WOOD_STAIRS = 712;
    public static short BLOCK_ORANGE_STAINED_WOOD_SLAB = 713;
    public static short BLOCK_ORANGE_STAINED_WOOD_FENCE = 714;
    public static short BLOCK_PINK_STAINED_WOOD_STAIRS = 715;
    public static short BLOCK_PINK_STAINED_WOOD_SLAB = 716;
    public static short BLOCK_PINK_STAINED_WOOD_FENCE = 717;
    public static short BLOCK_PURPLE_STAINED_WOOD_STAIRS = 718;
    public static short BLOCK_PURPLE_STAINED_WOOD_SLAB = 719;
    public static short BLOCK_PURPLE_STAINED_WOOD_FENCE = 720;
    public static short BLOCK_RED_STAINED_WOOD_STAIRS = 721;
    public static short BLOCK_RED_STAINED_WOOD_SLAB = 722;
    public static short BLOCK_RED_STAINED_WOOD_FENCE = 723;
    public static short BLOCK_WHITE_STAINED_WOOD_STAIRS = 724;
    public static short BLOCK_WHITE_STAINED_WOOD_SLAB = 725;
    public static short BLOCK_WHITE_STAINED_WOOD_FENCE = 726;
    public static short BLOCK_WHITE_SPACE_TILE = 727;
    public static short BLOCK_GRAY_SPACE_TILE = 728;
    public static short BLOCK_WHITE_SPACE_TILE_SLAB = 729;
    public static short BLOCK_GRAY_SPACE_TILE_STAIRS = 730;
    public static short BLOCK_GRAY_SPACE_TILE_SLAB = 731;
    public static short BLOCK_SPRUCE_LOG = 732;
    public static short BLOCK_SPRUCE_LEAVES = 733;
    public static short BLOCK_SPRUCE_SAPLING = 734;
    public static short BLOCK_MERGE_TRACK = 736;
    public static short BLOCK_20_HP_ENGINE = 741;
    public static short BLOCK_70_HP_ENGINE = 744;
    public static short BLOCK_SILVER_BRICK = 747;
    public static short BLOCK_LARGE_HELICOPTER_BLADE = 749;
    public static short BLOCK_SMALL_HELICOPTER_BLADE = 750;
    public static short BLOCK_YELLOW_CARPET = 752;
    public static short BLOCK_BLACK_CARPET = 753;
    public static short BLOCK_BLUE_CARPET = 754;
    public static short BLOCK_BROWN_CARPET = 755;
    public static short BLOCK_CYAN_CARPET = 756;
    public static short BLOCK_GRAY_CARPET = 757;
    public static short BLOCK_GREEN_CARPET = 758;
    public static short BLOCK_LIGHT_BLUE_CARPET = 759;
    public static short BLOCK_LIGHT_GRAY_CARPET = 760;
    public static short BLOCK_LIME_CARPET = 761;
    public static short BLOCK_MAGENTA_CARPET = 762;
    public static short BLOCK_ORANGE_CARPET = 763;
    public static short BLOCK_PINK_CARPET = 764;
    public static short BLOCK_PURPLE_CARPET = 765;
    public static short BLOCK_RED_CARPET = 766;
    public static short BLOCK_WHITE_CARPET = 767;
    public static short BLOCK_GOLD_SEAT = 768;
    public static short BLOCK_GOLD_SIDING = 769;
    public static short BLOCK_TAN_SEAT = 770;
    public static short BLOCK_TAN_SIDING = 771;
    public static short BLOCK_BROWN_SEAT = 772;
    public static short BLOCK_BROWN_SIDING = 773;
    public static short BLOCK_CYAN_SEAT = 774;
    public static short BLOCK_CYAN_SIDING = 775;
    public static short BLOCK_GRAY_SEAT = 776;
    public static short BLOCK_GRAY_SIDING = 777;
    public static short BLOCK_GREEN_SEAT = 778;
    public static short BLOCK_GREEN_SIDING = 779;
    public static short BLOCK_BLUE_SEAT = 780;
    public static short BLOCK_BLUE_SIDING = 781;
    public static short BLOCK_LIGHT_GRAY_SEAT = 782;
    public static short BLOCK_LIGHT_GRAY_SIDING = 783;
    public static short BLOCK_LIGHT_GREEN_SEAT = 784;
    public static short BLOCK_LIGHT_GREEN_SIDING = 785;
    public static short BLOCK_MAGENTA_SEAT = 786;
    public static short BLOCK_MAGENTA_SIDING = 787;
    public static short BLOCK_ORANGE_SEAT = 788;
    public static short BLOCK_ORANGE_SIDING = 789;
    public static short BLOCK_PINK_SEAT = 790;
    public static short BLOCK_PINK_SIDING = 791;
    public static short BLOCK_PURPLE_SEAT = 792;
    public static short BLOCK_PURPLE_SIDING = 793;
    public static short BLOCK_RED_SEAT = 794;
    public static short BLOCK_RED_SIDING = 795;
    public static short BLOCK_WHITE_SEAT = 796;
    public static short BLOCK_WHITE_SIDING = 797;
    public static short BLOCK_YELLOW_SEAT = 798;
    public static short BLOCK_YELLOW_SIDING = 799;
    public static short BLOCK_WIRE = 737;
    public static short BLOCK_TEST_BLOCK = -1;
    public static short BLOCK_TEST_ORIENTABLE = -2;
    public static short BLOCK_TEST_SLAB = -3;
    public static short BLOCK_TEST_TRAPDOOR = -4;
    public static short BLOCK_TEST_FENCE_GATE = -5;
    public static short BLOCK_TEST_DOOR_HALF = -6;
    public static short BLOCK_ACACIA2_DOOR_TOP = 878;
    public static short BLOCK_ACACIA_DOOR_TOP = 879;
    public static short BLOCK_BAMBOO_2_DOOR_TOP = 880;
    public static short BLOCK_BAMBOO_DOOR_TOP = 881;
    public static short BLOCK_BIRCH_DOOR_TOP = 882;
    public static short BLOCK_CLADDED_BLUE_DOOR_TOP = 883;
    public static short BLOCK_CLADDED_GREEN_DOOR_TOP = 884;
    public static short BLOCK_CLADDED_MAGENTA_DOOR_TOP = 885;
    public static short BLOCK_CLADDED_DOOR_TOP = 886;
    public static short BLOCK_FRENCH_BAMBOO_WINDOWS_DOOR_TOP = 887;
    public static short BLOCK_FRENCH_BAMBOO_DOOR_TOP = 888;
    public static short BLOCK_FRENCH_BLUE_WINDOWS_DOOR_TOP = 889;
    public static short BLOCK_FRENCH_BLUE_DOOR_TOP = 890;
    public static short BLOCK_FRENCH_GREEN_WINDOWS_DOOR_TOP = 891;
    public static short BLOCK_FRENCH_GREEN_DOOR_TOP = 892;
    public static short BLOCK_FRENCH_MAGENTA_WINDOWS_DOOR_TOP = 893;
    public static short BLOCK_FRENCH_MAGENTA_DOOR_TOP = 894;
    public static short BLOCK_FRENCH_WINDOWS_DOOR_TOP = 895;
    public static short BLOCK_FRENCH_DOOR_TOP = 896;
    public static short BLOCK_GLASS_DOOR_TOP = 897;
    public static short BLOCK_GRAY_SPACE_DOOR_TOP = 898;
    public static short BLOCK_GRAY_SPACE_CONTROL_PANEL_DOOR_TOP = 899;
    public static short BLOCK_GRAY_2_DOOR_TOP = 900;
    public static short BLOCK_ITALIAN_BAMBOO_WINDOWS_DOOR_TOP = 901;
    public static short BLOCK_ITALIAN_BAMBOO_DOOR_TOP = 902;
    public static short BLOCK_ITALIAN_BLUE_WINDOWS_DOOR_TOP = 903;
    public static short BLOCK_ITALIAN_BLUE_DOOR_TOP = 904;
    public static short BLOCK_ITALIAN_GREEN_WINDOWS_DOOR_TOP = 905;
    public static short BLOCK_ITALIAN_GREEN_DOOR_TOP = 906;
    public static short BLOCK_ITALIAN_MAGENTA_WINDOWS_DOOR_TOP = 907;
    public static short BLOCK_ITALIAN_MAGENTA_DOOR_TOP = 908;
    public static short BLOCK_ITALIAN_WINDOWS_DOOR_TOP = 909;
    public static short BLOCK_ITALIAN_DOOR_TOP = 910;
    public static short BLOCK_JUNGLE_2_DOOR_TOP = 911;
    public static short BLOCK_JUNGLE_DOOR_TOP = 912;
    public static short BLOCK_OAK_2_DOOR_TOP = 913;
    public static short BLOCK_OAK_RENAISSANCE_DOOR_TOP = 914;
    public static short BLOCK_OAK_DOOR_TOP = 915;
    public static short BLOCK_OVAL_BAMBOO_DOOR_TOP = 916;
    public static short BLOCK_OVAL_BLUE_DOOR_TOP = 917;
    public static short BLOCK_OVAL_GREEN_DOOR_TOP = 918;
    public static short BLOCK_OVAL_MAGENTA_DOOR_TOP = 919;
    public static short BLOCK_OVAL_DOOR_TOP = 920;
    public static short BLOCK_PARTY_BLUE_DOOR_TOP = 921;
    public static short BLOCK_PARTY_GREEN_DOOR_TOP = 922;
    public static short BLOCK_PARTY_MAGENTA_DOOR_TOP = 923;
    public static short BLOCK_PARTY_PURPLE_DOOR_TOP = 924;
    public static short BLOCK_PARTY_RED_DOOR_TOP = 925;
    public static short BLOCK_PARTY_YELLOW_DOOR_TOP = 926;
    public static short BLOCK_RED_DOOR_TOP = 927;
    public static short BLOCK_SPRUCE_2_DOOR_TOP = 928;
    public static short BLOCK_SPRUCE_DOOR_TOP = 929;
    public static short BLOCK_STEEL_DOOR_TOP = 930;
    public static short BLOCK_WARPED_DOOR_TOP = 931;
    public static short BLOCK_WEB_DOOR_TOP = 932;
    public static short BLOCK_WHITE_1_DOOR_TOP = 933;
    public static short BLOCK_WHITE_2_CONTROL_PANEL_DOOR_TOP = 934;
    public static short BLOCK_WHITE_2_DOOR_TOP = 935;
    public static short BLOCK_WHITE_DOOR_TOP = 936;
    public static short BLOCK_ACACIA_DOOR_2 = 937;
    public static short BLOCK_ACACIA_DOOR = 938;
    public static short BLOCK_BAMBOO_2_DOOR = 939;
    public static short BLOCK_BAMBOO_DOOR = 940;
    public static short BLOCK_BIRCH_DOOR = 941;
    public static short BLOCK_CLADDED_BLUE_DOOR = 942;
    public static short BLOCK_CLADDED_GREEN_DOOR = 943;
    public static short BLOCK_CLADDED_MAGENTA_DOOR = 944;
    public static short BLOCK_CLADDED_DOOR = 945;
    public static short BLOCK_FRENCH_BAMBOO_WINDOWS_DOOR = 946;
    public static short BLOCK_FRENCH_BAMBOO_DOOR = 947;
    public static short BLOCK_FRENCH_BLUE_WINDOWS_DOOR = 948;
    public static short BLOCK_FRENCH_BLUE_DOOR = 949;
    public static short BLOCK_FRENCH_GREEN_WINDOWS_DOOR = 950;
    public static short BLOCK_FRENCH_GREEN_DOOR = 951;
    public static short BLOCK_FRENCH_MAGENTA_WINDOWS_DOOR = 952;
    public static short BLOCK_FRENCH_MAGENTA_DOOR = 953;
    public static short BLOCK_FRENCH_WINDOWS_DOOR = 954;
    public static short BLOCK_FRENCH_DOOR = 955;
    public static short BLOCK_GLASS_DOOR = 956;
    public static short BLOCK_GRAY_SPACE_DOOR = 957;
    public static short BLOCK_GRAY_SPACE_CONTROL_PANEL_DOOR = 958;
    public static short BLOCK_GRAY_SPACE_DOOR_2 = 959;
    public static short BLOCK_ITALIAN_BAMBOO_WINDOWS_DOOR = 960;
    public static short BLOCK_ITALIAN_BAMBOO_DOOR = 961;
    public static short BLOCK_ITALIAN_BLUE_WINDOWS_DOOR = 962;
    public static short BLOCK_ITALIAN_BLUE_DOOR = 963;
    public static short BLOCK_ITALIAN_GREEN_WINDOWS_DOOR = 964;
    public static short BLOCK_ITALIAN_GREEN_DOOR = 965;
    public static short BLOCK_ITALIAN_MAGENTA_WINDOWS_DOOR = 966;
    public static short BLOCK_ITALIAN_MAGENTA_DOOR = 967;
    public static short BLOCK_ITALIAN_WINDOWS_DOOR = 968;
    public static short BLOCK_ITALIAN_DOOR = 969;
    public static short BLOCK_JUNGLE_2_DOOR = 970;
    public static short BLOCK_JUNGLE_DOOR = 971;
    public static short BLOCK_OAK_2_DOOR = 972;
    public static short BLOCK_OAK_RENAISSANCE_DOOR = 973;
    public static short BLOCK_OAK_DOOR = 974;
    public static short BLOCK_OVAL_BAMBOO_DOOR = 975;
    public static short BLOCK_OVAL_BLUE_DOOR = 976;
    public static short BLOCK_OVAL_GREEN_DOOR = 977;
    public static short BLOCK_OVAL_MAGENTA_DOOR = 978;
    public static short BLOCK_OVAL_DOOR = 979;
    public static short BLOCK_PARTY_BLUE_DOOR = 980;
    public static short BLOCK_PARTY_GREEN_DOOR = 981;
    public static short BLOCK_PARTY_MAGENTA_DOOR = 982;
    public static short BLOCK_PARTY_PURPLE_DOOR = 983;
    public static short BLOCK_PARTY_RED_DOOR = 984;
    public static short BLOCK_PARTY_YELLOW_DOOR = 985;
    public static short BLOCK_RED_DOOR = 986;
    public static short BLOCK_SPRUCE_2_DOOR = 987;
    public static short BLOCK_SPRUCE_DOOR = 988;
    public static short BLOCK_STEEL_DOOR = 989;
    public static short BLOCK_WARPED_DOOR = 990;
    public static short BLOCK_WEB_DOOR = 991;
    public static short BLOCK_WHITE_SPACE_DOOR = 992;
    public static short BLOCK_WHITE_SPACE_CONTROL_PANEL_DOOR = 993;
    public static short BLOCK_WHITE_SPACE_DOOR_2 = 994;
    public static short BLOCK_WHITE_DOOR = 995;
    public static short BLOCK_JUNGLE_FENCE_GATE = 800;
    public static short BLOCK_OAK_FENCE_GATE = 801;
    public static short BLOCK_BIRCH_FENCE_GATE = 802;
    public static short BLOCK_DARK_OAK_FENCE_GATE = 803;
    public static short BLOCK_ACACIA_FENCE_GATE = 804;
    public static short BLOCK_STONE_BRICK_FENCE_GATE = 805;
    public static short BLOCK_PALISADE_STONE_FENCE_GATE = 806;
    public static short BLOCK_PALISADE_STONE_2_FENCE_GATE = 807;
    public static short BLOCK_POLISHED_DIORITE_FENCE_GATE = 808;
    public static short BLOCK_POLISHED_ANDESITE_FENCE_GATE = 809;
    public static short BLOCK_CRACKED_STONE_FENCE_GATE = 810;
    public static short BLOCK_STONE_WITH_VINES_FENCE_GATE = 811;
    public static short BLOCK_BURGUNDY_BRICK_FENCE_GATE = 812;
    public static short BLOCK_RED_PALISADE_SANDSTONE_FENCE_GATE = 813;
    public static short BLOCK_PALISADE_SANDSTONE_FENCE_GATE = 814;
    public static short BLOCK_YELLOW_CONCRETE_FENCE_GATE = 815;
    public static short BLOCK_BLACK_CONCRETE_FENCE_GATE = 816;
    public static short BLOCK_BLUE_CONCRETE_FENCE_GATE = 817;
    public static short BLOCK_BROWN_CONCRETE_FENCE_GATE = 818;
    public static short BLOCK_CYAN_CONCRETE_FENCE_GATE = 819;
    public static short BLOCK_GRAY_CONCRETE_FENCE_GATE = 820;
    public static short BLOCK_GREEN_CONCRETE_FENCE_GATE = 821;
    public static short BLOCK_LIGHT_BLUE_CONCRETE_FENCE_GATE = 822;
    public static short BLOCK_LIGHT_GRAY_CONCRETE_FENCE_GATE = 823;
    public static short BLOCK_LIME_CONCRETE_FENCE_GATE = 824;
    public static short BLOCK_MAGENTA_CONCRETE_FENCE_GATE = 825;
    public static short BLOCK_ORANGE_CONCRETE_FENCE_GATE = 826;
    public static short BLOCK_PINK_CONCRETE_FENCE_GATE = 827;
    public static short BLOCK_PURPLE_CONCRETE_FENCE_GATE = 828;
    public static short BLOCK_RED_CONCRETE_FENCE_GATE = 829;
    public static short BLOCK_WHITE_CONCRETE_FENCE_GATE = 830;
    public static short BLOCK_BAMBOO_WOOD_FENCE_GATE = 831;
    public static short BLOCK_CEMENT_FENCE_GATE = 832;
    public static short BLOCK_OBSIDIAN_FENCE_GATE = 833;
    public static short BLOCK_LAPIS_LAZUL_FENCE_GATE = 834;
    public static short BLOCK_STEEL_FENCE_GATE = 835;
    public static short BLOCK_GOLD_FENCE_GATE = 836;
    public static short BLOCK_EMERALD_FENCE_GATE = 837;
    public static short BLOCK_DIAMOND_FENCE_GATE = 838;
    public static short BLOCK_PRISMARINE_BRICK_FENCE_GATE = 839;
    public static short BLOCK_GRANITE_BRICK_FENCE_GATE = 840;
    public static short BLOCK_DARK_PRISMARINE_BRICK_FENCE_GATE = 841;
    public static short BLOCK_MOSAIC_BAMBOO_WOOD_FENCE_GATE = 842;
    public static short BLOCK_YELLOW_STAINED_WOOD_FENCE_GATE = 843;
    public static short BLOCK_BLACK_STAINED_WOOD_FENCE_GATE = 844;
    public static short BLOCK_BLUE_STAINED_WOOD_FENCE_GATE = 845;
    public static short BLOCK_CYAN_STAINED_WOOD_FENCE_GATE = 846;
    public static short BLOCK_GRAY_STAINED_WOOD_FENCE_GATE = 847;
    public static short BLOCK_GREEN_STAINED_WOOD_FENCE_GATE = 848;
    public static short BLOCK_LIGHT_BLUE_STAINED_WOOD_FENCE_GATE = 849;
    public static short BLOCK_LIME_STAINED_WOOD_FENCE_GATE = 850;
    public static short BLOCK_MAGENTA_STAINED_WOOD_FENCE_GATE = 851;
    public static short BLOCK_ORANGE_STAINED_WOOD_FENCE_GATE = 852;
    public static short BLOCK_PINK_STAINED_WOOD_FENCE_GATE = 853;
    public static short BLOCK_PURPLE_STAINED_WOOD_FENCE_GATE = 854;
    public static short BLOCK_RED_STAINED_WOOD_FENCE_GATE = 855;
    public static short BLOCK_WHITE_STAINED_WOOD_FENCE_GATE = 856;
    public static short BLOCK_EDISON_LIGHT = 55;
    public static short BLOCK_ELECTRIC_LIGHT = 174;
    public static short BLOCK_SEA_LIGHT = 13;
    public static short BLOCK_DIAMOND_ORE = 105;
    public static short BLOCK_COAL_ORE = 228;
    public static short BLOCK_IRON_ORE = 234;
    public static short BLOCK_GOLD_ORE = 237;
    public static short BLOCK_LAPIS_ORE = 544;
    public static short BLOCK_EMERALD_ORE = 551;
    public static short BLOCK_WHEAT_SEEDS = 162;
    public static short BLOCK_CARROT_SEEDS = 163;
    public static short BLOCK_POTATO_SEEDS = 165;
    public static short BLOCK_POTATO_GROWTH_1 = 166;
    public static short BLOCK_POTATO_GROWTH_2 = 167;
    public static short BLOCK_CARROT_GROWTH_1 = 549;
    public static short BLOCK_CARROT_GROWTH_2 = 735;
    public static short BLOCK_BEETROOT_GROWTH_1 = 738;
    public static short BLOCK_BEETROOT_GROWTH_2 = 739;
    public static short BLOCK_WHEAT_GROWTH_1 = 168;
    public static short BLOCK_WHEAT_GROWTH_2 = 169;
    public static short BLOCK_WHEAT_GROWTH_3 = 170;
    public static short BLOCK_WHEAT_GROWTH_4 = 171;
    public static short BLOCK_WHEAT_GROWTH_5 = 172;
    public static short BLOCK_WHEAT_GROWTH_6 = 173;
    public static short BLOCK_WHEAT = 121;
    public static short BLOCK_CARROTS = 122;
    public static short BLOCK_POTATOES = 146;
    public static short BLOCK_BEETROOT_SEEDS = 247;
    public static short BLOCK_ACACIA_TRAPDOOR = 857;
    public static short BLOCK_ACACIA2_TRAPDOOR = 858;
    public static short BLOCK_BAMBOO_2_TRAPDOOR = 859;
    public static short BLOCK_BAMBOO_TRAPDOOR = 860;
    public static short BLOCK_BIRCH_TRAPDOOR = 861;
    public static short BLOCK_CLADDED_BIRCH_TRAPDOOR = 862;
    public static short BLOCK_CLADDED_REDWOOD_TRAPDOOR = 863;
    public static short BLOCK_CLADDED_WHITE_TRAPDOOR = 864;
    public static short BLOCK_GLASS_TRAPDOOR = 865;
    public static short BLOCK_GRAY_SPACE_TRAPDOOR_TRAPDOOR = 866;
    public static short BLOCK_JUNGLE_2_TRAPDOOR = 867;
    public static short BLOCK_JUNGLE_TRAPDOOR = 868;
    public static short BLOCK_OAK_2_TRAPDOOR = 869;
    public static short BLOCK_OAK_TRAPDOOR = 870;
    public static short BLOCK_RED_TRAPDOOR = 871;
    public static short BLOCK_RENAISSANCE_TRAPDOOR = 872;
    public static short BLOCK_SPRUCE_TRAPDOOR = 873;
    public static short BLOCK_STEEL_TRAPDOOR = 874;
    public static short BLOCK_WARPED_TRAPDOOR = 875;
    public static short BLOCK_WHITE_SPACE_TRAPDOOR_TRAPDOOR = 876;
    public static short BLOCK_WHITE_TRAPDOOR = 877;
    public static short BLOCK_BARREL = 45;
    public static short BLOCK_CRAFTING_TABLE = 50;
    public static short BLOCK_FURNACE = 99;
    public static short BLOCK_STRAIGHT_TRACK = 155;
    public static short BLOCK_SPAWN_BLOCK = 552;
    public static short BLOCK_FLAG_BLOCK = 231;


    /**
     * Blocks must be initialized first, otherwise it will not work
     * Utils
     */
    public static PlantBlockUtils plantUtils = new PlantBlockUtils();

    /**
     * Returns a entities of all blocks for initialization
     *
     * @return
     */
    public static ArrayList<Block> starup_getBlocks() {

        //Load blocks from our json files
        ArrayList<Block> blockList = getJsonBlocksFromResource("/data/xbuilders/blocks/json");
        //Add blocks
        blockList.add(new BlockBarrel(Blocks.BLOCK_BARREL, "barrel"));
        blockList.add(new CraftingTable(Blocks.BLOCK_CRAFTING_TABLE));
        blockList.add(new BlockFurnace(Blocks.BLOCK_FURNACE));
        blockList.add(new BlockStraightTrack(Blocks.BLOCK_STRAIGHT_TRACK));
        blockList.add(new BlockSpawn(Blocks.BLOCK_SPAWN_BLOCK));
        blockList.add(new BlockFlag(Blocks.BLOCK_FLAG_BLOCK));
        blockList.add(new BlockFarmland(Blocks.BLOCK_FARMLAND));
        blockList.add(new BlockWetFarmland(Blocks.BLOCK_WET_FARMLAND));

        if (Client.DEV_MODE) {//Make ids for dev mode
            try {
                ItemUtils.block_makeClassJavaFiles(blockList, PathHandler.resourcePath("\\items\\blocks\\java"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return blockList;
    }


    public static void editBlocks(ClientWindow window) {


        //Block UIs
        Main.game.barrelUI.assignToBlock(Registrys.getBlock(Blocks.BLOCK_BARREL));
        Main.game.smeltingUI.assignToBlock(Registrys.getBlock(Blocks.BLOCK_FURNACE));


        short[] dontExplodeList = {Blocks.BLOCK_TNT, Blocks.BLOCK_MEGA_TNT};
        BlockEventUtils.setTNTEvents(Registrys.getBlock(Blocks.BLOCK_TNT), 5, 2000, 50, dontExplodeList);
        BlockEventUtils.setTNTEvents(Registrys.getBlock(Blocks.BLOCK_MEGA_TNT), 10, 5000, 100, dontExplodeList);


        BlockEventUtils.makeVerticalPairedBlock(Blocks.BLOCK_TALL_GRASS_TOP, Blocks.BLOCK_TALL_GRASS);
        BlockEventUtils.makeVerticalPairedBlock(Blocks.BLOCK_TALL_DRY_GRASS_TOP, Blocks.BLOCK_TALL_DRY_GRASS);


        //Logs
//        Block log = Registrys.getBlock(Blocks.BLOCK_OAK_LOG);
//        log.removeBlockEvent(false,
//                PlantBlockUtils.logRemovalEvent(log, Registrys.getBlock(Blocks.BLOCK_OAK_LEAVES)));

        Block lava = Registrys.getBlock(Blocks.BLOCK_LAVA);
        lava.liquidMaxFlow = 6;
        lava.enterDamage = 0.5f;

        Block fire = Registrys.getBlock(Blocks.BLOCK_FIRE);
        fire.enterDamage = 0.2f;

        randomTickEvents();

        changeMaterialCoasting();

        GravityBlock gravity = new GravityBlock(window);
        gravity.convert(Registrys.getBlock(Blocks.BLOCK_SAND));
        gravity.convert(Registrys.getBlock(Blocks.BLOCK_RED_SAND));
        gravity.convert(Registrys.getBlock(Blocks.BLOCK_GRAVEL));
        gravity.convert(Registrys.getBlock(Blocks.BLOCK_SNOW_BLOCK));

        CrystalBlockUtils.init();

        //Some blocks can only be mined with certain tools
        Block hard = Registrys.getBlock(Blocks.BLOCK_OBSIDIAN);
        hard.toolsThatCanMine_tags = new String[]{"diamond"}; //tags
        hard.easierMiningTool_tag = "pickaxe";

        hard = Registrys.getBlock(Blocks.BLOCK_DIAMOND_BLOCK);
        hard.toolsThatCanMine_tags = new String[]{"diamond"};
        hard.easierMiningTool_tag = "pickaxe";

        for (Block b : Registrys.blocks.getList()) {
            //Add flammable tag to various blocks
            if (isWood(b)) {
                b.properties.put("flammable", "true");
            } else if (!b.solid && b.type == RenderType.SPRITE
                    && (b.alias.contains("dead") || b.alias.contains("dry") || b.alias.contains("grass"))) {
                b.properties.put("flammable", "true");
            }

            if (b.alias.toLowerCase().contains("glass")) {
                b.toughness = 0.2f;
            }


            if (isWood(b)) {
                b.easierMiningTool_tag = "axe";
            } else if (plantUtils.blockIsGrassSnowOrDirt(b)
                    || b.alias.contains("sand")
                    || b.alias.contains("gravel")
                    || b.alias.contains("clay")
                    || b.alias.contains("leaves")) {
                b.easierMiningTool_tag = "shovel";
            }
        }

    }

    private static boolean isWood(Block b) {
        return (b.alias.contains("wood") || b.alias.contains("log") || b.alias.contains("planks")
                || b.alias.contains("birch") || b.alias.contains("jungle") || b.alias.contains("oak")
                || b.alias.contains("spruce") || b.alias.contains("acacia"))
                && b.solid;
    }


    private static void randomTickEvents() {
        Block.RandomTickEvent dirtTickEvent = (x, y, z) -> {
            if (!Main.getClient().world.getBlock(x, y - 1, z).solid) {
                Main.getServer().setBlock(plantUtils.getGrassBlockOfBiome(x, y, z), x, y, z);
                return true;
            }
            return false;
        };

        Block.RandomTickEvent grassTickEvent = (x, y, z) -> {
            Block aboveBlock = Main.getClient().world.getBlock(x, y - 1, z);
            if (aboveBlock.solid) {
                Main.getServer().setBlock(Blocks.BLOCK_DIRT, x, y, z);
                return true;
            } else if (random.nextFloat() < GROW_PROBABILITY) {
                short grassToPlant = plantUtils.growGrass(x, y, z, aboveBlock);
                if (grassToPlant != -1) {
                    Main.getServer().setBlock(grassToPlant, x, y - 1, z);
                    return true;
                }
            }
            return false;
        };

        Block dirt = Registrys.getBlock(Blocks.BLOCK_DIRT);
        dirt.randomTickEvent = dirtTickEvent;

        Block grass = Registrys.getBlock(Blocks.BLOCK_GRASS);
        grass.randomTickEvent = grassTickEvent;
        grass = Registrys.getBlock(Blocks.BLOCK_DRY_GRASS);
        grass.randomTickEvent = grassTickEvent;
        grass = Registrys.getBlock(Blocks.BLOCK_JUNGLE_GRASS);
        grass.randomTickEvent = grassTickEvent;
        grass = Registrys.getBlock(Blocks.BLOCK_SNOW_GRASS);
        grass.randomTickEvent = grassTickEvent;

        /**
         * Decay events
         */
        //All snowy/default growth has a 75% chance to disappear
        for (short plant : plantUtils.snowyDefaultGrowth) {
            plantUtils.addDecayTickEvent(Registrys.getBlock(plant));
        }
        //Grass
        plantUtils.addDecayTickEvent(Registrys.getBlock(Blocks.BLOCK_GRASS_PLANT));
        plantUtils.addDecayTickEvent(Registrys.getBlock(Blocks.BLOCK_DRY_GRASS_PLANT));
        plantUtils.addDecayTickEvent(Registrys.getBlock(Blocks.BLOCK_JUNGLE_GRASS_PLANT));
        //Tall grass
        plantUtils.addDecayTickEvent(Registrys.getBlock(Blocks.BLOCK_TALL_DRY_GRASS));
        plantUtils.addDecayTickEvent(Registrys.getBlock(Blocks.BLOCK_TALL_GRASS));


        /**
         * Crops
         */
        plantUtils.addPlantGrowthEvents(
                Registrys.getBlock(Blocks.BLOCK_BEETROOT_SEEDS),
                Registrys.getBlock(Blocks.BLOCK_BEETROOT_GROWTH_1),
                Registrys.getBlock(Blocks.BLOCK_BEETROOT_GROWTH_2),
                Registrys.getBlock(Blocks.BLOCK_BEETS));

        plantUtils.addPlantGrowthEvents(
                Registrys.getBlock(Blocks.BLOCK_CARROT_SEEDS),
                Registrys.getBlock(Blocks.BLOCK_CARROT_GROWTH_1),
                Registrys.getBlock(Blocks.BLOCK_CARROT_GROWTH_2),
                Registrys.getBlock(Blocks.BLOCK_CARROTS));

        plantUtils.addPlantGrowthEvents(
                Registrys.getBlock(Blocks.BLOCK_POTATO_SEEDS),
                Registrys.getBlock(Blocks.BLOCK_POTATO_GROWTH_1),
                Registrys.getBlock(Blocks.BLOCK_POTATO_GROWTH_2),
                Registrys.getBlock(Blocks.BLOCK_POTATOES));

        plantUtils.addPlantGrowthEvents(
                Registrys.getBlock(Blocks.BLOCK_WHEAT_SEEDS),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_1),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_2),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_3),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_4),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_5),
                Registrys.getBlock(Blocks.BLOCK_WHEAT_GROWTH_6),
                Registrys.getBlock(Blocks.BLOCK_WHEAT));

        plantUtils.addPlantGrowthEvents(
                Registrys.getBlock(Blocks.BLOCK_PUMPKIN_SEEDS),
                Registrys.getBlock(Blocks.BLOCK_PUMPKIN_STEM),
                Registrys.getBlock(Blocks.BLOCK_PUMPKIN));

        plantUtils.makeStalk(
                Registrys.getBlock("xbuilders:bamboo"),
                Registrys.getBlock("xbuilders:bamboo_sapling"),
                15,
                (block) -> {//Growable
                    return plantUtils.blockIsSandOrGravel(block)
                            || plantUtils.blockIsGrassSnowOrDirt(block);
                });

        plantUtils.makeStalk(
                Registrys.getBlock("xbuilders:sugar_cane"), null,
                8,
                (block) -> {//growable
                    return plantUtils.blockIsSandOrGravel(block);
                });


        Registrys.getBlock(Blocks.BLOCK_OAK_SAPLING).randomTickEvent = OakTreeUtils.randomTickEvent;
        Registrys.getBlock(Blocks.BLOCK_SPRUCE_SAPLING).randomTickEvent = SpruceTreeUtils.randomTickEvent;
        Registrys.getBlock(Blocks.BLOCK_BIRCH_SAPLING).randomTickEvent = BirchTreeUtils.randomTickEvent;
        Registrys.getBlock(Blocks.BLOCK_JUNGLE_SAPLING).randomTickEvent = JungleTreeUtils.randomTickEvent;
        Registrys.getBlock(Blocks.BLOCK_ACACIA_SAPLING).randomTickEvent = AcaciaTreeUtils.randomTickEvent;

        Registrys.getBlock(Blocks.BLOCK_OAK_LOG).removeBlockEvent(false,
                TreeUtils.logRemovalEvent(
                        Registrys.getBlock(Blocks.BLOCK_OAK_LOG),
                        Registrys.getBlock(Blocks.BLOCK_OAK_LEAVES)));

        Registrys.getBlock(Blocks.BLOCK_SPRUCE_LOG).removeBlockEvent(false,
                TreeUtils.logRemovalEvent(
                        Registrys.getBlock(Blocks.BLOCK_SPRUCE_LOG),
                        Registrys.getBlock(Blocks.BLOCK_SPRUCE_LEAVES)));

        Registrys.getBlock(Blocks.BLOCK_BIRCH_LOG).removeBlockEvent(false,
                TreeUtils.logRemovalEvent(
                        Registrys.getBlock(Blocks.BLOCK_BIRCH_LOG),
                        Registrys.getBlock(Blocks.BLOCK_BIRCH_LEAVES)));

        Registrys.getBlock(Blocks.BLOCK_JUNGLE_LOG).removeBlockEvent(false,
                TreeUtils.logRemovalEvent(
                        Registrys.getBlock(Blocks.BLOCK_JUNGLE_LOG),
                        Registrys.getBlock(Blocks.BLOCK_JUNGLE_LEAVES)));

        Registrys.getBlock(Blocks.BLOCK_ACACIA_LOG).removeBlockEvent(false,
                TreeUtils.logRemovalEvent(
                        Registrys.getBlock(Blocks.BLOCK_ACACIA_LOG),
                        Registrys.getBlock(Blocks.BLOCK_ACACIA_LEAVES)));

        //Leaves
        Registrys.getBlock(Blocks.BLOCK_OAK_LEAVES).randomTickEvent = TreeUtils.leafTickEvent(Registrys.getBlock(Blocks.BLOCK_OAK_LEAVES));
        Registrys.getBlock(Blocks.BLOCK_SPRUCE_LEAVES).randomTickEvent = TreeUtils.leafTickEvent(Registrys.getBlock(Blocks.BLOCK_SPRUCE_LEAVES));
        Registrys.getBlock(Blocks.BLOCK_BIRCH_LEAVES).randomTickEvent = TreeUtils.leafTickEvent(Registrys.getBlock(Blocks.BLOCK_BIRCH_LEAVES));
        Registrys.getBlock(Blocks.BLOCK_JUNGLE_LEAVES).randomTickEvent = TreeUtils.leafTickEvent(Registrys.getBlock(Blocks.BLOCK_JUNGLE_LEAVES));
        Registrys.getBlock(Blocks.BLOCK_ACACIA_LEAVES).randomTickEvent = TreeUtils.leafTickEvent(Registrys.getBlock(Blocks.BLOCK_ACACIA_LEAVES));
        //Vines
        TreeUtils.vineEvents(Registrys.getBlock(Blocks.BLOCK_VINES), Blocks.BLOCK_JUNGLE_LEAVES);
        TreeUtils.vineEvents(Registrys.getBlock(Blocks.BLOCK_CAVE_VINES), Blocks.BLOCK_JUNGLE_LEAVES);
        TreeUtils.vineEvents(Registrys.getBlock(Blocks.BLOCK_DRAGON_VINES), Blocks.BLOCK_JUNGLE_LEAVES);
        TreeUtils.vineEvents(Registrys.getBlock(Blocks.BLOCK_RED_VINES), Blocks.BLOCK_JUNGLE_LEAVES);

        Registrys.getBlock(Blocks.BLOCK_FIRE).randomTickEvent = (x, y, z) -> {
            if (!Main.getClient().world.getBlock(x, y + 1, z).solid || Math.random() < 0.1) {
                //Decay other blocks
                if (!Main.getClient().world.getBlock(x, y + 1, z).solid ||
                        Main.getClient().world.getBlock(x, y + 1, z).properties.containsKey("flammable")) {
                    Main.getServer().setBlock(Blocks.BLOCK_AIR, x, y + 1, z);
                }
                //Decay this block
                Main.getServer().setBlock(Blocks.BLOCK_AIR, x, y, z);
                return true;
            } else {
                boolean foundFlammable = false;
                if (spreadIfFlammable(x + 1, y, z)) foundFlammable = true;
                if (spreadIfFlammable(x - 1, y, z)) foundFlammable = true;
                if (spreadIfFlammable(x, y, z + 1)) foundFlammable = true;
                if (spreadIfFlammable(x, y, z - 1)) foundFlammable = true;

                if (spreadIfFlammable(x + 1, y - 1, z)) foundFlammable = true;
                if (spreadIfFlammable(x - 1, y - 1, z)) foundFlammable = true;
                if (spreadIfFlammable(x, y - 1, z + 1)) foundFlammable = true;
                if (spreadIfFlammable(x, y - 1, z - 1)) foundFlammable = true;

                if (spreadIfFlammable(x + 1, y + 1, z)) foundFlammable = true;
                if (spreadIfFlammable(x - 1, y + 1, z)) foundFlammable = true;
                if (spreadIfFlammable(x, y + 1, z + 1)) foundFlammable = true;
                if (spreadIfFlammable(x, y + 1, z - 1)) foundFlammable = true;

                if (spreadIfFlammable(x + 1, y + 2, z)) foundFlammable = true;
                if (spreadIfFlammable(x - 1, y + 2, z)) foundFlammable = true;
                if (spreadIfFlammable(x, y + 2, z + 1)) foundFlammable = true;
                if (spreadIfFlammable(x, y + 2, z - 1)) foundFlammable = true;

                if (spreadIfFlammable(x, y - 1, z)) foundFlammable = true;
                if (spreadIfFlammable(x, y + 2, z)) foundFlammable = true;

                return foundFlammable;
            }
        };

    }

    private static boolean spreadIfFlammable(int x, int y, int z) {
        if (Main.getClient().world.getBlock(x, y, z).properties.containsKey("flammable")) {
            Main.getServer().setBlock(Blocks.BLOCK_FIRE, x, y - 1, z);
            return true;
        }
        return false;
    }

    private static void changeMaterialCoasting() {
        for (Block b : Registrys.blocks.getList()) {
            //Add coasting to all glass
            if (b.alias.toLowerCase().contains("glass")) {
                b.surfaceCoast = 0.95f;
            }

            //Add flammable tag to various blocks
            String lowercaseName = b.alias.toLowerCase();
            if (lowercaseName.contains("leave") || lowercaseName.contains("log") || lowercaseName.contains("plank") || lowercaseName.contains("oak") || lowercaseName.contains("birch") || lowercaseName.contains("wood") || lowercaseName.contains("acacia") || lowercaseName.contains("jungle") || lowercaseName.contains("spruce") || lowercaseName.contains("dark_oak") || lowercaseName.contains("crimson") || lowercaseName.contains("warped") || lowercaseName.contains("dry")) {
                b.properties.put("flammable", "true");
            }
        }

        Registrys.getBlock(Blocks.BLOCK_ICE_BLOCK).surfaceCoast = 0.995f;
        Registrys.getBlock(Blocks.BLOCK_CACTUS).surfaceFriction = 0.5f;
        Registrys.getBlock(Blocks.BLOCK_OAK_LOG).properties.put("flammable", "true");
        Registrys.getBlock(Blocks.BLOCK_HONEYCOMB_BLOCK).bounciness = 0.7f;
        Registrys.getBlock(Blocks.BLOCK_HONEYCOMB_BLOCK_SLAB).bounciness = 0.6f;
    }


}
