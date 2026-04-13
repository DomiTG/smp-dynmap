package com.domitg.smpdynmap.renderer;

import org.bukkit.Material;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

/**
 * Maps Minecraft block materials to representative ARGB colours for top-down
 * tile rendering.  Biome-tinted blocks (grass, leaves, water) are returned
 * with a base colour; the renderer applies the biome tint on top.
 */
public class BlockColorMapper {

    /** Sentinel: block is fully transparent — use the colour below it. */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private static final Map<Material, Color> COLORS = new EnumMap<>(Material.class);

    static {
        // ── AIR / VOID ──────────────────────────────────────────────────────
        COLORS.put(Material.AIR,               TRANSPARENT);
        COLORS.put(Material.CAVE_AIR,          TRANSPARENT);
        COLORS.put(Material.VOID_AIR,          TRANSPARENT);

        // ── FLUIDS ──────────────────────────────────────────────────────────
        COLORS.put(Material.WATER,             new Color(0x3F, 0x76, 0xE4, 0xCC));
        COLORS.put(Material.LAVA,              new Color(0xE2, 0x55, 0x00));

        // ── NATURAL TERRAIN ─────────────────────────────────────────────────
        COLORS.put(Material.GRASS_BLOCK,       new Color(0x79, 0xC0, 0x5A));
        COLORS.put(Material.DIRT,              new Color(0x86, 0x62, 0x43));
        COLORS.put(Material.COARSE_DIRT,       new Color(0x77, 0x55, 0x37));
        COLORS.put(Material.PODZOL,            new Color(0x5E, 0x3E, 0x1A));
        COLORS.put(Material.ROOTED_DIRT,       new Color(0x86, 0x62, 0x43));
        COLORS.put(Material.MUD,               new Color(0x3E, 0x3A, 0x36));
        COLORS.put(Material.STONE,             new Color(0x80, 0x80, 0x80));
        COLORS.put(Material.COBBLESTONE,       new Color(0x6A, 0x6A, 0x6A));
        COLORS.put(Material.BEDROCK,           new Color(0x3A, 0x3A, 0x3A));
        COLORS.put(Material.GRAVEL,            new Color(0x80, 0x75, 0x6F));
        COLORS.put(Material.SAND,              new Color(0xDB, 0xD3, 0xA0));
        COLORS.put(Material.RED_SAND,          new Color(0xBE, 0x71, 0x33));
        COLORS.put(Material.CLAY,              new Color(0xA0, 0xA7, 0xB5));
        COLORS.put(Material.MYCELIUM,          new Color(0x70, 0x5F, 0x70));

        // ── STONE VARIANTS ──────────────────────────────────────────────────
        COLORS.put(Material.GRANITE,           new Color(0x98, 0x65, 0x53));
        COLORS.put(Material.POLISHED_GRANITE,  new Color(0x98, 0x65, 0x53));
        COLORS.put(Material.DIORITE,           new Color(0xBB, 0xBB, 0xBB));
        COLORS.put(Material.POLISHED_DIORITE,  new Color(0xBB, 0xBB, 0xBB));
        COLORS.put(Material.ANDESITE,          new Color(0x87, 0x87, 0x87));
        COLORS.put(Material.POLISHED_ANDESITE, new Color(0x87, 0x87, 0x87));
        COLORS.put(Material.DEEPSLATE,         new Color(0x52, 0x52, 0x5D));
        COLORS.put(Material.COBBLED_DEEPSLATE, new Color(0x50, 0x50, 0x58));
        COLORS.put(Material.CALCITE,           new Color(0xD4, 0xD0, 0xC8));
        COLORS.put(Material.TUFF,              new Color(0x6E, 0x71, 0x62));
        COLORS.put(Material.TUFF_BRICKS,       new Color(0x6E, 0x71, 0x62));
        COLORS.put(Material.DRIPSTONE_BLOCK,   new Color(0x8A, 0x6E, 0x5A));

        // ── ORES ────────────────────────────────────────────────────────────
        COLORS.put(Material.COAL_ORE,          new Color(0x5A, 0x5A, 0x5A));
        COLORS.put(Material.DEEPSLATE_COAL_ORE, new Color(0x45, 0x45, 0x4D));
        COLORS.put(Material.IRON_ORE,          new Color(0xAA, 0x8E, 0x7A));
        COLORS.put(Material.DEEPSLATE_IRON_ORE, new Color(0x80, 0x6A, 0x5C));
        COLORS.put(Material.COPPER_ORE,        new Color(0xC0, 0x7A, 0x55));
        COLORS.put(Material.DEEPSLATE_COPPER_ORE, new Color(0xA0, 0x65, 0x45));
        COLORS.put(Material.GOLD_ORE,          new Color(0xFC, 0xEE, 0x4C));
        COLORS.put(Material.DEEPSLATE_GOLD_ORE, new Color(0xD4, 0xC6, 0x3C));
        COLORS.put(Material.REDSTONE_ORE,      new Color(0xAA, 0x22, 0x22));
        COLORS.put(Material.DEEPSLATE_REDSTONE_ORE, new Color(0x8A, 0x18, 0x18));
        COLORS.put(Material.EMERALD_ORE,       new Color(0x17, 0xDD, 0x62));
        COLORS.put(Material.DEEPSLATE_EMERALD_ORE, new Color(0x10, 0xB8, 0x50));
        COLORS.put(Material.LAPIS_ORE,         new Color(0x1E, 0x55, 0xAA));
        COLORS.put(Material.DEEPSLATE_LAPIS_ORE, new Color(0x18, 0x45, 0x8A));
        COLORS.put(Material.DIAMOND_ORE,       new Color(0x4A, 0xCD, 0xCD));
        COLORS.put(Material.DEEPSLATE_DIAMOND_ORE, new Color(0x3A, 0xAD, 0xAD));
        COLORS.put(Material.NETHER_QUARTZ_ORE, new Color(0xAA, 0x88, 0x88));
        COLORS.put(Material.NETHER_GOLD_ORE,   new Color(0xD4, 0x9C, 0x44));
        COLORS.put(Material.ANCIENT_DEBRIS,    new Color(0x5A, 0x3D, 0x30));

        // ── RAW MINERAL BLOCKS ───────────────────────────────────────────────
        COLORS.put(Material.COAL_BLOCK,        new Color(0x18, 0x18, 0x18));
        COLORS.put(Material.IRON_BLOCK,        new Color(0xD8, 0xD8, 0xD8));
        COLORS.put(Material.COPPER_BLOCK,      new Color(0xC0, 0x80, 0x5A));
        COLORS.put(Material.EXPOSED_COPPER,    new Color(0xA8, 0x7D, 0x6A));
        COLORS.put(Material.WEATHERED_COPPER,  new Color(0x6E, 0xA2, 0x77));
        COLORS.put(Material.OXIDIZED_COPPER,   new Color(0x4B, 0xA4, 0x8B));
        COLORS.put(Material.GOLD_BLOCK,        new Color(0xF9, 0xD4, 0x35));
        COLORS.put(Material.DIAMOND_BLOCK,     new Color(0x64, 0xD8, 0xD8));
        COLORS.put(Material.EMERALD_BLOCK,     new Color(0x1A, 0xD8, 0x5C));
        COLORS.put(Material.LAPIS_BLOCK,       new Color(0x1C, 0x52, 0x9A));
        COLORS.put(Material.REDSTONE_BLOCK,    new Color(0xCC, 0x11, 0x11));
        COLORS.put(Material.NETHERITE_BLOCK,   new Color(0x44, 0x3C, 0x3E));
        COLORS.put(Material.AMETHYST_BLOCK,    new Color(0x7C, 0x4E, 0xBC));
        COLORS.put(Material.BUDDING_AMETHYST,  new Color(0x7C, 0x4E, 0xBC));

        // ── WOOD / LOGS ──────────────────────────────────────────────────────
        COLORS.put(Material.OAK_LOG,           new Color(0x5C, 0x4A, 0x28));
        COLORS.put(Material.OAK_PLANKS,        new Color(0xC2, 0x9D, 0x5B));
        COLORS.put(Material.OAK_LEAVES,        new Color(0x4A, 0x7A, 0x24));
        COLORS.put(Material.SPRUCE_LOG,        new Color(0x3C, 0x2E, 0x18));
        COLORS.put(Material.SPRUCE_PLANKS,     new Color(0x7D, 0x60, 0x3B));
        COLORS.put(Material.SPRUCE_LEAVES,     new Color(0x2E, 0x50, 0x18));
        COLORS.put(Material.BIRCH_LOG,         new Color(0xCB, 0xC4, 0xB5));
        COLORS.put(Material.BIRCH_PLANKS,      new Color(0xD8, 0xCF, 0x93));
        COLORS.put(Material.BIRCH_LEAVES,      new Color(0x5B, 0x7D, 0x2C));
        COLORS.put(Material.JUNGLE_LOG,        new Color(0x5E, 0x47, 0x1C));
        COLORS.put(Material.JUNGLE_PLANKS,     new Color(0xA6, 0x7C, 0x4D));
        COLORS.put(Material.JUNGLE_LEAVES,     new Color(0x44, 0x7A, 0x20));
        COLORS.put(Material.ACACIA_LOG,        new Color(0x65, 0x62, 0x5F));
        COLORS.put(Material.ACACIA_PLANKS,     new Color(0xBA, 0x6F, 0x3C));
        COLORS.put(Material.ACACIA_LEAVES,     new Color(0x5A, 0x7A, 0x24));
        COLORS.put(Material.DARK_OAK_LOG,      new Color(0x36, 0x28, 0x10));
        COLORS.put(Material.DARK_OAK_PLANKS,   new Color(0x4C, 0x33, 0x14));
        COLORS.put(Material.DARK_OAK_LEAVES,   new Color(0x3A, 0x6A, 0x1A));
        COLORS.put(Material.MANGROVE_LOG,      new Color(0x7F, 0x32, 0x2D));
        COLORS.put(Material.MANGROVE_PLANKS,   new Color(0x77, 0x42, 0x3C));
        COLORS.put(Material.MANGROVE_LEAVES,   new Color(0x52, 0x7E, 0x27));
        COLORS.put(Material.CHERRY_LOG,        new Color(0x5D, 0x37, 0x3C));
        COLORS.put(Material.CHERRY_PLANKS,     new Color(0xF0, 0xB5, 0xB8));
        COLORS.put(Material.CHERRY_LEAVES,     new Color(0xF2, 0xA8, 0xC0));
        COLORS.put(Material.BAMBOO_BLOCK,      new Color(0x69, 0x80, 0x30));
        COLORS.put(Material.BAMBOO_PLANKS,     new Color(0xC2, 0xB2, 0x77));

        // ── NETHER / END WOOD ────────────────────────────────────────────────
        COLORS.put(Material.CRIMSON_STEM,      new Color(0x7B, 0x16, 0x2C));
        COLORS.put(Material.CRIMSON_PLANKS,    new Color(0x8E, 0x29, 0x43));
        COLORS.put(Material.WARPED_STEM,       new Color(0x24, 0x71, 0x6B));
        COLORS.put(Material.WARPED_PLANKS,     new Color(0x2D, 0x7F, 0x7A));

        // ── STONE BRICKS ────────────────────────────────────────────────────
        COLORS.put(Material.STONE_BRICKS,      new Color(0x7A, 0x7A, 0x7A));
        COLORS.put(Material.MOSSY_STONE_BRICKS, new Color(0x60, 0x7A, 0x60));
        COLORS.put(Material.CRACKED_STONE_BRICKS, new Color(0x68, 0x68, 0x68));
        COLORS.put(Material.CHISELED_STONE_BRICKS, new Color(0x78, 0x78, 0x78));
        COLORS.put(Material.BRICKS,            new Color(0x9C, 0x55, 0x45));
        COLORS.put(Material.MOSSY_COBBLESTONE, new Color(0x5E, 0x72, 0x5E));
        COLORS.put(Material.COBBLESTONE_WALL,  new Color(0x6A, 0x6A, 0x6A));
        COLORS.put(Material.DEEPSLATE_BRICKS,  new Color(0x4A, 0x4A, 0x52));
        COLORS.put(Material.DEEPSLATE_TILES,   new Color(0x40, 0x40, 0x48));

        // ── SANDSTONE ────────────────────────────────────────────────────────
        COLORS.put(Material.SANDSTONE,         new Color(0xCD, 0xC0, 0x87));
        COLORS.put(Material.CHISELED_SANDSTONE, new Color(0xCD, 0xC0, 0x87));
        COLORS.put(Material.SMOOTH_SANDSTONE,  new Color(0xCE, 0xBF, 0x85));
        COLORS.put(Material.CUT_SANDSTONE,     new Color(0xCE, 0xBF, 0x85));
        COLORS.put(Material.RED_SANDSTONE,     new Color(0xA3, 0x54, 0x1A));
        COLORS.put(Material.SMOOTH_RED_SANDSTONE, new Color(0xA3, 0x54, 0x1A));

        // ── PRISMARINE ───────────────────────────────────────────────────────
        COLORS.put(Material.PRISMARINE,        new Color(0x5E, 0x9E, 0x97));
        COLORS.put(Material.PRISMARINE_BRICKS, new Color(0x6A, 0xAE, 0xA7));
        COLORS.put(Material.DARK_PRISMARINE,   new Color(0x2E, 0x5A, 0x4E));
        COLORS.put(Material.SEA_LANTERN,       new Color(0xAF, 0xD6, 0xCD));

        // ── NETHER BLOCKS ────────────────────────────────────────────────────
        COLORS.put(Material.NETHERRACK,        new Color(0x72, 0x22, 0x22));
        COLORS.put(Material.SOUL_SAND,         new Color(0x4A, 0x3B, 0x28));
        COLORS.put(Material.SOUL_SOIL,         new Color(0x40, 0x32, 0x20));
        COLORS.put(Material.GLOWSTONE,         new Color(0xF0, 0xC0, 0x50));
        COLORS.put(Material.NETHER_BRICKS,     new Color(0x2C, 0x10, 0x10));
        COLORS.put(Material.NETHER_BRICK_FENCE, new Color(0x2A, 0x0E, 0x0E));
        COLORS.put(Material.RED_NETHER_BRICKS, new Color(0x4E, 0x0B, 0x0B));
        COLORS.put(Material.BASALT,            new Color(0x4A, 0x4A, 0x52));
        COLORS.put(Material.BLACKSTONE,        new Color(0x2A, 0x27, 0x2C));
        COLORS.put(Material.GILDED_BLACKSTONE, new Color(0x35, 0x2C, 0x1D));
        COLORS.put(Material.MAGMA_BLOCK,       new Color(0xB0, 0x47, 0x10));
        COLORS.put(Material.SHROOMLIGHT,       new Color(0xF0, 0xAD, 0x5D));
        COLORS.put(Material.NETHER_WART_BLOCK, new Color(0x77, 0x09, 0x09));
        COLORS.put(Material.WARPED_WART_BLOCK, new Color(0x14, 0x69, 0x62));

        // ── END BLOCKS ───────────────────────────────────────────────────────
        COLORS.put(Material.END_STONE,         new Color(0xDB, 0xD8, 0x8E));
        COLORS.put(Material.END_STONE_BRICKS,  new Color(0xD6, 0xD4, 0x88));
        COLORS.put(Material.PURPUR_BLOCK,      new Color(0xA5, 0x76, 0xA5));
        COLORS.put(Material.PURPUR_PILLAR,     new Color(0xA8, 0x79, 0xA8));
        COLORS.put(Material.CHORUS_PLANT,      new Color(0x6A, 0x42, 0x6A));
        COLORS.put(Material.CHORUS_FLOWER,     new Color(0x8A, 0x62, 0x8A));
        COLORS.put(Material.END_ROD,           new Color(0xFF, 0xFF, 0xE5));

        // ── GLASS ────────────────────────────────────────────────────────────
        COLORS.put(Material.GLASS,             new Color(0xAF, 0xD0, 0xD5, 0x80));
        COLORS.put(Material.GLASS_PANE,        new Color(0xAF, 0xD0, 0xD5, 0x80));
        COLORS.put(Material.WHITE_STAINED_GLASS,   new Color(0xFF, 0xFF, 0xFF, 0x90));
        COLORS.put(Material.ORANGE_STAINED_GLASS,  new Color(0xD8, 0x77, 0x20, 0x90));
        COLORS.put(Material.MAGENTA_STAINED_GLASS, new Color(0xAA, 0x2E, 0xAA, 0x90));
        COLORS.put(Material.LIGHT_BLUE_STAINED_GLASS, new Color(0x66, 0x99, 0xFF, 0x90));
        COLORS.put(Material.YELLOW_STAINED_GLASS,  new Color(0xFF, 0xE0, 0x30, 0x90));
        COLORS.put(Material.LIME_STAINED_GLASS,    new Color(0x55, 0xCC, 0x33, 0x90));
        COLORS.put(Material.PINK_STAINED_GLASS,    new Color(0xF7, 0x89, 0xA6, 0x90));
        COLORS.put(Material.GRAY_STAINED_GLASS,    new Color(0x55, 0x55, 0x55, 0x90));
        COLORS.put(Material.LIGHT_GRAY_STAINED_GLASS, new Color(0x9A, 0x9A, 0x9A, 0x90));
        COLORS.put(Material.CYAN_STAINED_GLASS,    new Color(0x33, 0x99, 0xAA, 0x90));
        COLORS.put(Material.PURPLE_STAINED_GLASS,  new Color(0x88, 0x22, 0xCC, 0x90));
        COLORS.put(Material.BLUE_STAINED_GLASS,    new Color(0x33, 0x33, 0xCC, 0x90));
        COLORS.put(Material.BROWN_STAINED_GLASS,   new Color(0x66, 0x44, 0x22, 0x90));
        COLORS.put(Material.GREEN_STAINED_GLASS,   new Color(0x33, 0x88, 0x22, 0x90));
        COLORS.put(Material.RED_STAINED_GLASS,     new Color(0xCC, 0x22, 0x22, 0x90));
        COLORS.put(Material.BLACK_STAINED_GLASS,   new Color(0x11, 0x11, 0x11, 0x90));

        // ── WOOL ─────────────────────────────────────────────────────────────
        COLORS.put(Material.WHITE_WOOL,        new Color(0xE8, 0xE8, 0xE8));
        COLORS.put(Material.ORANGE_WOOL,       new Color(0xF3, 0x7D, 0x20));
        COLORS.put(Material.MAGENTA_WOOL,      new Color(0xB7, 0x40, 0xBC));
        COLORS.put(Material.LIGHT_BLUE_WOOL,   new Color(0x6B, 0xAB, 0xE0));
        COLORS.put(Material.YELLOW_WOOL,       new Color(0xF9, 0xC6, 0x29));
        COLORS.put(Material.LIME_WOOL,         new Color(0x71, 0xBD, 0x2A));
        COLORS.put(Material.PINK_WOOL,         new Color(0xED, 0x8C, 0xAD));
        COLORS.put(Material.GRAY_WOOL,         new Color(0x44, 0x4A, 0x4F));
        COLORS.put(Material.LIGHT_GRAY_WOOL,   new Color(0x9A, 0x9A, 0x9A));
        COLORS.put(Material.CYAN_WOOL,         new Color(0x15, 0x7F, 0x91));
        COLORS.put(Material.PURPLE_WOOL,       new Color(0x80, 0x23, 0xAA));
        COLORS.put(Material.BLUE_WOOL,         new Color(0x2D, 0x33, 0x8A));
        COLORS.put(Material.BROWN_WOOL,        new Color(0x72, 0x4F, 0x2F));
        COLORS.put(Material.GREEN_WOOL,        new Color(0x54, 0x6D, 0x1B));
        COLORS.put(Material.RED_WOOL,          new Color(0xA2, 0x22, 0x22));
        COLORS.put(Material.BLACK_WOOL,        new Color(0x1D, 0x1D, 0x21));

        // ── CONCRETE ─────────────────────────────────────────────────────────
        COLORS.put(Material.WHITE_CONCRETE,    new Color(0xCF, 0xD5, 0xD6));
        COLORS.put(Material.ORANGE_CONCRETE,   new Color(0xE0, 0x68, 0x1E));
        COLORS.put(Material.MAGENTA_CONCRETE,  new Color(0xAB, 0x35, 0xBF));
        COLORS.put(Material.LIGHT_BLUE_CONCRETE, new Color(0x2E, 0xAA, 0xCE));
        COLORS.put(Material.YELLOW_CONCRETE,   new Color(0xF2, 0xB4, 0x2B));
        COLORS.put(Material.LIME_CONCRETE,     new Color(0x5E, 0xA9, 0x18));
        COLORS.put(Material.PINK_CONCRETE,     new Color(0xD3, 0x62, 0x8C));
        COLORS.put(Material.GRAY_CONCRETE,     new Color(0x37, 0x3A, 0x3E));
        COLORS.put(Material.LIGHT_GRAY_CONCRETE, new Color(0x82, 0x84, 0x86));
        COLORS.put(Material.CYAN_CONCRETE,     new Color(0x15, 0x77, 0x88));
        COLORS.put(Material.PURPLE_CONCRETE,   new Color(0x64, 0x1F, 0x9C));
        COLORS.put(Material.BLUE_CONCRETE,     new Color(0x2C, 0x2F, 0x8F));
        COLORS.put(Material.BROWN_CONCRETE,    new Color(0x60, 0x3C, 0x20));
        COLORS.put(Material.GREEN_CONCRETE,    new Color(0x49, 0x5B, 0x24));
        COLORS.put(Material.RED_CONCRETE,      new Color(0x8E, 0x21, 0x22));
        COLORS.put(Material.BLACK_CONCRETE,    new Color(0x08, 0x0A, 0x0F));

        // ── TERRACOTTA ───────────────────────────────────────────────────────
        COLORS.put(Material.TERRACOTTA,        new Color(0x98, 0x5E, 0x4C));
        COLORS.put(Material.WHITE_TERRACOTTA,  new Color(0xD1, 0xB1, 0xA1));
        COLORS.put(Material.ORANGE_TERRACOTTA, new Color(0xA0, 0x4D, 0x16));
        COLORS.put(Material.MAGENTA_TERRACOTTA, new Color(0x8E, 0x3D, 0x5C));
        COLORS.put(Material.LIGHT_BLUE_TERRACOTTA, new Color(0x70, 0x80, 0x97));
        COLORS.put(Material.YELLOW_TERRACOTTA, new Color(0xB9, 0x86, 0x25));
        COLORS.put(Material.LIME_TERRACOTTA,   new Color(0x62, 0x73, 0x24));
        COLORS.put(Material.PINK_TERRACOTTA,   new Color(0xA0, 0x53, 0x51));
        COLORS.put(Material.GRAY_TERRACOTTA,   new Color(0x39, 0x2C, 0x2B));
        COLORS.put(Material.LIGHT_GRAY_TERRACOTTA, new Color(0x87, 0x69, 0x62));
        COLORS.put(Material.CYAN_TERRACOTTA,   new Color(0x53, 0x6A, 0x6B));
        COLORS.put(Material.PURPLE_TERRACOTTA, new Color(0x76, 0x35, 0x52));
        COLORS.put(Material.BLUE_TERRACOTTA,   new Color(0x45, 0x41, 0x70));
        COLORS.put(Material.BROWN_TERRACOTTA,  new Color(0x63, 0x3E, 0x27));
        COLORS.put(Material.GREEN_TERRACOTTA,  new Color(0x4A, 0x53, 0x22));
        COLORS.put(Material.RED_TERRACOTTA,    new Color(0x8E, 0x2D, 0x22));
        COLORS.put(Material.BLACK_TERRACOTTA,  new Color(0x25, 0x16, 0x15));

        // ── SNOW / ICE ───────────────────────────────────────────────────────
        COLORS.put(Material.SNOW_BLOCK,        new Color(0xF0, 0xF0, 0xF0));
        COLORS.put(Material.SNOW,              new Color(0xE8, 0xEC, 0xEC));
        COLORS.put(Material.ICE,               new Color(0x9F, 0xCD, 0xEE, 0xCC));
        COLORS.put(Material.PACKED_ICE,        new Color(0x7D, 0xAF, 0xD0));
        COLORS.put(Material.BLUE_ICE,          new Color(0x74, 0xB4, 0xF4));
        COLORS.put(Material.POWDER_SNOW,       new Color(0xE4, 0xE8, 0xEC));

        // ── PLANTS / VEGETATION ──────────────────────────────────────────────
        COLORS.put(Material.GRASS,             TRANSPARENT);  // short grass → see below
        COLORS.put(Material.TALL_GRASS,        TRANSPARENT);
        COLORS.put(Material.FERN,              TRANSPARENT);
        COLORS.put(Material.LARGE_FERN,        TRANSPARENT);
        COLORS.put(Material.DEAD_BUSH,         new Color(0x8A, 0x6A, 0x2A));
        COLORS.put(Material.SEAGRASS,          new Color(0x3A, 0x8A, 0x5A));
        COLORS.put(Material.TALL_SEAGRASS,     new Color(0x3A, 0x8A, 0x5A));
        COLORS.put(Material.KELP,              new Color(0x3A, 0x7A, 0x3A));
        COLORS.put(Material.KELP_PLANT,        new Color(0x3A, 0x7A, 0x3A));
        COLORS.put(Material.LILY_PAD,          new Color(0x24, 0x7A, 0x24));
        COLORS.put(Material.CACTUS,            new Color(0x4A, 0x8A, 0x2A));
        COLORS.put(Material.SUGAR_CANE,        new Color(0x78, 0xBA, 0x5A));
        COLORS.put(Material.BAMBOO,            new Color(0x6A, 0x88, 0x28));
        COLORS.put(Material.VINE,              new Color(0x38, 0x6A, 0x18));
        COLORS.put(Material.GLOW_LICHEN,       new Color(0x70, 0x8A, 0x80, 0xA0));
        COLORS.put(Material.HANGING_ROOTS,     new Color(0x86, 0x62, 0x43));

        // Flowers (transparent – skip, show block below)
        COLORS.put(Material.DANDELION,         TRANSPARENT);
        COLORS.put(Material.POPPY,             TRANSPARENT);
        COLORS.put(Material.BLUE_ORCHID,       TRANSPARENT);
        COLORS.put(Material.ALLIUM,            TRANSPARENT);
        COLORS.put(Material.AZURE_BLUET,       TRANSPARENT);
        COLORS.put(Material.RED_TULIP,         TRANSPARENT);
        COLORS.put(Material.ORANGE_TULIP,      TRANSPARENT);
        COLORS.put(Material.WHITE_TULIP,       TRANSPARENT);
        COLORS.put(Material.PINK_TULIP,        TRANSPARENT);
        COLORS.put(Material.OXEYE_DAISY,       TRANSPARENT);
        COLORS.put(Material.CORNFLOWER,        TRANSPARENT);
        COLORS.put(Material.LILY_OF_THE_VALLEY, TRANSPARENT);
        COLORS.put(Material.SUNFLOWER,         TRANSPARENT);
        COLORS.put(Material.LILAC,             TRANSPARENT);
        COLORS.put(Material.ROSE_BUSH,         TRANSPARENT);
        COLORS.put(Material.PEONY,             TRANSPARENT);

        // Mushrooms
        COLORS.put(Material.RED_MUSHROOM,      TRANSPARENT);
        COLORS.put(Material.BROWN_MUSHROOM,    TRANSPARENT);
        COLORS.put(Material.RED_MUSHROOM_BLOCK, new Color(0xBB, 0x33, 0x33));
        COLORS.put(Material.BROWN_MUSHROOM_BLOCK, new Color(0x8A, 0x6A, 0x43));
        COLORS.put(Material.MUSHROOM_STEM,     new Color(0xCC, 0xCC, 0xBC));

        // Nether vegetation
        COLORS.put(Material.CRIMSON_FUNGUS,    new Color(0x8E, 0x1B, 0x2A));
        COLORS.put(Material.WARPED_FUNGUS,     new Color(0x24, 0x71, 0x6B));
        COLORS.put(Material.NETHER_WART,       new Color(0x7A, 0x0A, 0x0A));
        COLORS.put(Material.CRIMSON_ROOTS,     new Color(0x7B, 0x16, 0x2C));
        COLORS.put(Material.WARPED_ROOTS,      new Color(0x14, 0x70, 0x6A));
        COLORS.put(Material.WEEPING_VINES,     new Color(0x7B, 0x16, 0x2C));
        COLORS.put(Material.TWISTING_VINES,    new Color(0x14, 0x70, 0x6A));
        COLORS.put(Material.CAVE_VINES,        new Color(0x4A, 0x6A, 0x28));
        COLORS.put(Material.SPORE_BLOSSOM,     new Color(0xC8, 0x6E, 0x9A));
        COLORS.put(Material.AZALEA,            new Color(0x5A, 0x7A, 0x28));
        COLORS.put(Material.FLOWERING_AZALEA,  new Color(0xCA, 0x8A, 0xC8));
        COLORS.put(Material.AZALEA_LEAVES,     new Color(0x4A, 0x7A, 0x24));
        COLORS.put(Material.FLOWERING_AZALEA_LEAVES, new Color(0xCA, 0x8A, 0xC8));
        COLORS.put(Material.MOSS_BLOCK,        new Color(0x5C, 0x74, 0x27));
        COLORS.put(Material.MOSS_CARPET,       new Color(0x5C, 0x74, 0x27));
        COLORS.put(Material.BIG_DRIPLEAF,      new Color(0x5A, 0x8A, 0x24));
        COLORS.put(Material.SMALL_DRIPLEAF,    TRANSPARENT);
        COLORS.put(Material.PITCHER_PLANT,     new Color(0x9C, 0x72, 0xC8));
        COLORS.put(Material.TORCHFLOWER,       new Color(0xF0, 0xA0, 0x30));

        // ── FUNCTIONAL / MECHANICAL ──────────────────────────────────────────
        COLORS.put(Material.CRAFTING_TABLE,    new Color(0xA0, 0x6A, 0x3A));
        COLORS.put(Material.FURNACE,           new Color(0x70, 0x70, 0x70));
        COLORS.put(Material.BLAST_FURNACE,     new Color(0x68, 0x68, 0x68));
        COLORS.put(Material.SMOKER,            new Color(0x68, 0x68, 0x68));
        COLORS.put(Material.CHEST,             new Color(0xA0, 0x7C, 0x40));
        COLORS.put(Material.TRAPPED_CHEST,     new Color(0xA0, 0x6E, 0x3A));
        COLORS.put(Material.ENDER_CHEST,       new Color(0x1A, 0x4A, 0x55));
        COLORS.put(Material.BARREL,            new Color(0x8A, 0x67, 0x42));
        COLORS.put(Material.BOOKSHELF,         new Color(0xB0, 0x8E, 0x56));
        COLORS.put(Material.CHISELED_BOOKSHELF, new Color(0xA0, 0x7A, 0x48));
        COLORS.put(Material.LECTERN,           new Color(0xB0, 0x90, 0x58));
        COLORS.put(Material.ANVIL,             new Color(0x40, 0x40, 0x40));
        COLORS.put(Material.ENCHANTING_TABLE,  new Color(0x8A, 0x1A, 0x1A));
        COLORS.put(Material.BEACON,            new Color(0x5C, 0xE8, 0xE8));
        COLORS.put(Material.CONDUIT,           new Color(0xA8, 0x8C, 0x64));
        COLORS.put(Material.BREWING_STAND,     new Color(0x6A, 0x58, 0x38));
        COLORS.put(Material.CAULDRON,          new Color(0x3A, 0x3A, 0x3A));
        COLORS.put(Material.DISPENSER,         new Color(0x78, 0x78, 0x78));
        COLORS.put(Material.DROPPER,           new Color(0x78, 0x78, 0x78));
        COLORS.put(Material.HOPPER,            new Color(0x40, 0x40, 0x40));
        COLORS.put(Material.OBSERVER,          new Color(0x68, 0x68, 0x68));
        COLORS.put(Material.PISTON,            new Color(0xAA, 0x8A, 0x6A));
        COLORS.put(Material.STICKY_PISTON,     new Color(0x6A, 0x8A, 0x4A));
        COLORS.put(Material.SLIME_BLOCK,       new Color(0x5E, 0xBB, 0x5E, 0xCC));
        COLORS.put(Material.HONEY_BLOCK,       new Color(0xF0, 0xA0, 0x30, 0xCC));
        COLORS.put(Material.HONEYCOMB_BLOCK,   new Color(0xE0, 0x90, 0x28));
        COLORS.put(Material.JUKEBOX,           new Color(0x6A, 0x42, 0x28));
        COLORS.put(Material.NOTE_BLOCK,        new Color(0x78, 0x52, 0x34));
        COLORS.put(Material.TNT,               new Color(0xC0, 0x30, 0x20));
        COLORS.put(Material.SPONGE,            new Color(0xC0, 0xBD, 0x4A));
        COLORS.put(Material.WET_SPONGE,        new Color(0x99, 0x9C, 0x3E));

        // ── REDSTONE ─────────────────────────────────────────────────────────
        COLORS.put(Material.REDSTONE_WIRE,     TRANSPARENT);
        COLORS.put(Material.REDSTONE_TORCH,    TRANSPARENT);
        COLORS.put(Material.REPEATER,          TRANSPARENT);
        COLORS.put(Material.COMPARATOR,        TRANSPARENT);
        COLORS.put(Material.LEVER,             TRANSPARENT);
        COLORS.put(Material.TRIPWIRE,          TRANSPARENT);
        COLORS.put(Material.TRIPWIRE_HOOK,     TRANSPARENT);
        COLORS.put(Material.DAYLIGHT_DETECTOR, new Color(0xCC, 0xBB, 0x99));
        COLORS.put(Material.TARGET,            new Color(0xE3, 0xA9, 0xA0));
        COLORS.put(Material.LIGHTNING_ROD,     new Color(0x88, 0x77, 0x55));
        COLORS.put(Material.SCULK,             new Color(0x0D, 0x2D, 0x2A));
        COLORS.put(Material.SCULK_CATALYST,    new Color(0x0D, 0x2D, 0x2A));
        COLORS.put(Material.SCULK_SHRIEKER,    new Color(0x0D, 0x2D, 0x2A));
        COLORS.put(Material.SCULK_SENSOR,      new Color(0x0D, 0x35, 0x35));
        COLORS.put(Material.CALIBRATED_SCULK_SENSOR, new Color(0x0D, 0x35, 0x35));

        // ── LIGHT / TORCHES ──────────────────────────────────────────────────
        COLORS.put(Material.TORCH,             TRANSPARENT);
        COLORS.put(Material.WALL_TORCH,        TRANSPARENT);
        COLORS.put(Material.SOUL_TORCH,        TRANSPARENT);
        COLORS.put(Material.SOUL_WALL_TORCH,   TRANSPARENT);
        COLORS.put(Material.LANTERN,           TRANSPARENT);
        COLORS.put(Material.SOUL_LANTERN,      TRANSPARENT);
        COLORS.put(Material.CAMPFIRE,          new Color(0x80, 0x50, 0x20));
        COLORS.put(Material.SOUL_CAMPFIRE,     new Color(0x20, 0x50, 0x80));

        // ── MISC ─────────────────────────────────────────────────────────────
        COLORS.put(Material.COBWEB,            new Color(0xE0, 0xE0, 0xE0, 0xA0));
        COLORS.put(Material.LADDER,            TRANSPARENT);
        COLORS.put(Material.IRON_BARS,         new Color(0x88, 0x88, 0x88, 0xA0));
        COLORS.put(Material.CHAIN,             new Color(0x55, 0x55, 0x66));
        COLORS.put(Material.DRAGON_EGG,        new Color(0x1A, 0x08, 0x26));
        COLORS.put(Material.TURTLE_EGG,        new Color(0xE0, 0xDE, 0xC3));
        COLORS.put(Material.SNIFFER_EGG,       new Color(0x4E, 0x2C, 0x22));
        COLORS.put(Material.FROGSPAWN,         new Color(0xA0, 0x90, 0x7A));
        COLORS.put(Material.MUD_BRICKS,        new Color(0x7A, 0x5A, 0x48));

        // Doors / Trapdoors / Fence-gates — let terrain show through
        for (Material m : Material.values()) {
            String n = m.name();
            if (!COLORS.containsKey(m) && (n.endsWith("_DOOR") || n.endsWith("_TRAPDOOR")
                    || n.endsWith("_FENCE_GATE") || n.endsWith("_FENCE")
                    || n.endsWith("_SIGN") || n.endsWith("_WALL_SIGN")
                    || n.endsWith("_HANGING_SIGN") || n.endsWith("_BANNER"))) {
                COLORS.put(m, TRANSPARENT);
            }
        }

        // Slab / stair / wall variants — inherit stone/wood colour via lookupBestColor
    }

    /**
     * Returns the colour for a given material.
     * Falls back to a heuristic colour for unlisted materials (e.g. slabs,
     * stairs, walls) rather than returning null.
     */
    public static Color getColor(Material material) {
        if (material == null) return TRANSPARENT;
        Color c = COLORS.get(material);
        if (c != null) return c;
        return inferColor(material);
    }

    /**
     * Returns {@code true} if the block should be treated as fully transparent
     * (i.e. the renderer should look further down for a solid block).
     */
    public static boolean isTransparent(Material material) {
        if (material == null) return true;
        Color c = getColor(material);
        return c == TRANSPARENT || c.getAlpha() == 0;
    }

    // ─── Heuristic fallback ────────────────────────────────────────────────

    private static Color inferColor(Material m) {
        String n = m.name();
        // Stair / Slab / Wall → strip suffix and re-look-up base material
        for (String suffix : new String[]{"_STAIRS", "_SLAB", "_WALL",
                "_PRESSURE_PLATE", "_BUTTON", "_STEP"}) {
            if (n.endsWith(suffix)) {
                String baseName = n.substring(0, n.length() - suffix.length());
                Material base = Material.matchMaterial(baseName);
                if (base != null && COLORS.containsKey(base)) return COLORS.get(base);
            }
        }
        // Polished → strip POLISHED_ prefix
        if (n.startsWith("POLISHED_")) {
            Material base = Material.matchMaterial(n.substring("POLISHED_".length()));
            if (base != null && COLORS.containsKey(base)) return COLORS.get(base);
        }
        // Default: medium grey
        return new Color(0x80, 0x80, 0x80);
    }
}
