package com.github.bucket1572.clumsycrafting

import org.bukkit.Material

enum class BlockGroup {
    /*
    모든 가공품은 제외 됨. (단, 윤나는 - 은 가공품으로 취급하지 않음.)
     */
    WOOD,
    STONE,
    TERRACOTTA,
    CONCRETE,
    ORE,
    CROP, // 식료품으로 가공될 수 있는 식물 중 사탕수수, 해조류를 제외한 모든 식물과 fungus
    ELSE
}

fun getBlockGroup(material: Material?) : BlockGroup =
        /*
        Material에 해당하는 블록 그룹을 반환
         */
        when(material) {
            Material.ACACIA_WOOD, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD,
            Material.JUNGLE_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD,
            Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_BIRCH_WOOD,
            Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_JUNGLE_WOOD,
            Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD,
            Material.CRIMSON_STEM, Material.WARPED_STEM, Material.STRIPPED_CRIMSON_STEM,
            Material.STRIPPED_WARPED_STEM, Material.ACACIA_LOG, Material.BIRCH_LOG,
            Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG,
            Material.SPRUCE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_OAK_LOG,
            Material.STRIPPED_SPRUCE_LOG, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE,
            Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE, Material.BAMBOO -> BlockGroup.WOOD

            Material.STONE, Material.GRANITE, Material.POLISHED_GRANITE, Material.DIORITE, Material.POLISHED_DIORITE,
            Material.ANDESITE, Material.POLISHED_ANDESITE, Material.BASALT, Material.POLISHED_BASALT,
            Material.END_STONE, Material.BLACKSTONE, Material.POLISHED_BLACKSTONE,
            Material.GILDED_BLACKSTONE, Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
            Material.PRISMARINE -> BlockGroup.STONE

            Material.EMERALD_ORE, Material.REDSTONE_ORE, Material.DIAMOND_ORE, Material.LAPIS_ORE,
            Material.NETHER_GOLD_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
            Material.NETHER_QUARTZ_ORE -> BlockGroup.ORE

            Material.TERRACOTTA, Material.MAGENTA_GLAZED_TERRACOTTA, Material.BLACK_GLAZED_TERRACOTTA,
            Material.BLACK_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA, Material.BLUE_TERRACOTTA,
            Material.BROWN_GLAZED_TERRACOTTA, Material.BROWN_TERRACOTTA, Material.CYAN_GLAZED_TERRACOTTA,
            Material.CYAN_TERRACOTTA, Material.GRAY_GLAZED_TERRACOTTA, Material.GRAY_TERRACOTTA,
            Material.GREEN_TERRACOTTA, Material.GREEN_GLAZED_TERRACOTTA, Material.PINK_GLAZED_TERRACOTTA,
            Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA, Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA, Material.LIME_GLAZED_TERRACOTTA, Material.MAGENTA_TERRACOTTA,
            Material.LIME_TERRACOTTA, Material.PINK_TERRACOTTA, Material.ORANGE_GLAZED_TERRACOTTA,
            Material.ORANGE_TERRACOTTA, Material.PURPLE_GLAZED_TERRACOTTA, Material.PURPLE_TERRACOTTA,
            Material.RED_GLAZED_TERRACOTTA, Material.RED_TERRACOTTA, Material.WHITE_GLAZED_TERRACOTTA,
            Material.WHITE_TERRACOTTA -> BlockGroup.TERRACOTTA

            Material.BLACK_CONCRETE, Material.BLUE_CONCRETE, Material.BROWN_CONCRETE,
            Material.CYAN_CONCRETE, Material.GRAY_CONCRETE, Material.GREEN_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.LIME_CONCRETE,
            Material.MAGENTA_CONCRETE, Material.ORANGE_CONCRETE, Material.PINK_CONCRETE,
            Material.PURPLE_CONCRETE, Material.RED_CONCRETE, Material.WHITE_CONCRETE,
            Material.YELLOW_CONCRETE -> BlockGroup.CONCRETE

            Material.WHEAT, Material.MELON, Material.PUMPKIN, Material.CARVED_PUMPKIN,
            Material.COCOA, Material.CHORUS_PLANT, Material.CHORUS_FLOWER,
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS, Material.SWEET_BERRY_BUSH, Material.POTATOES,
            Material.CARROTS, Material.BEETROOTS -> BlockGroup.CROP

            else -> BlockGroup.ELSE
        }