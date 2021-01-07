package com.github.bucket1572.clumsycrafting

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class ToolGroup {
    AXE,
    PICKAXE,
    HOE,
    SWORD,
    SHOVEL,
    RANGED,
    FISHING_ROD,
    BUCKET,
    ELSE
}

fun getToolGroup(material: Material?): ToolGroup =
        when (material) {
            Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
            Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE -> ToolGroup.AXE

            Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE,
            Material.NETHERITE_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE -> ToolGroup.PICKAXE

            Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.IRON_HOE,
            Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE -> ToolGroup.HOE

            Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.GOLDEN_SWORD,
            Material.NETHERITE_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD -> ToolGroup.SWORD

            Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.GOLDEN_SHOVEL,
            Material.NETHERITE_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL -> ToolGroup.SHOVEL

            Material.BOW, Material.CROSSBOW -> ToolGroup.RANGED

            Material.FISHING_ROD, Material.CARROT_ON_A_STICK, Material.WARPED_FUNGUS_ON_A_STICK -> ToolGroup.FISHING_ROD

            Material.BUCKET, Material.COD_BUCKET, Material.LAVA_BUCKET,
            Material.PUFFERFISH_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET,
            Material.WATER_BUCKET -> ToolGroup.BUCKET

            else -> ToolGroup.ELSE
        } // 도구 그룹을 읽어 오는 함수

fun countIron(matrix: Array<ItemStack?>): Map<String, Int> {
    /*
    조합에 사용된 철의 개수와 철의 종류에 따른 개수를 모두 반환합니다.
    return : Map
    Iron -> 철 개수
    PoorPigIron -> 질 나쁜 선철 개수
    PoorCastIron -> 질 나쁜 주철 개수
    FinePigIron -> 질 좋은 선철 개수
    FineCastIron -> 질 좋은 선철 개수
    Steel -> 강철 개수
    BestSteel -> 강철+ 개수
     */
    var ironCount = 0
    var poorPigIronCount = 0
    var finePigIronCount = 0
    var poorCastIronCount = 0
    var fineCastIronCount = 0
    var steelCount = 0
    var bestSteelCount = 0

    matrix.forEach {
        if (it?.type == Material.IRON_INGOT) {
            ironCount++
        }
        if (GlobalObject.isSame(it, GlobalObject.poorPigIron)) {
            poorPigIronCount++
        }
        if (GlobalObject.isSame(it, GlobalObject.finePigIron)) {
            finePigIronCount++
        }
        if (GlobalObject.isSame(it, GlobalObject.poorCastIron)) {
            poorCastIronCount++
        }
        if (GlobalObject.isSame(it, GlobalObject.fineCastIron)) {
            fineCastIronCount++
        }
        if (GlobalObject.isFundamentallySame(it, GlobalObject.poorSteel)) {
            steelCount++
        }
        if (GlobalObject.isSame(it, GlobalObject.bestSteel)) {
            bestSteelCount++
        }
    }
    return mapOf(
            "Iron" to ironCount,
            "PoorPigIron" to poorPigIronCount,
            "PoorCastIron" to poorCastIronCount,
            "FinePigIron" to finePigIronCount,
            "FineCastIron" to fineCastIronCount,
            "Steel" to steelCount,
            "BestSteel" to bestSteelCount
    )
} // 철, 강철 등의 개수를 세는 함수