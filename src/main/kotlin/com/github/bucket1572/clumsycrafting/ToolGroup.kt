package com.github.bucket1572.clumsycrafting

import org.bukkit.Material

enum class ToolGroup {
    AXE,
    PICKAXE,
    HOE,
    ELSE
}

fun getToolGroup(material: Material?) : ToolGroup =
        when(material) {
            Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
            Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE -> ToolGroup.AXE

            Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE,
            Material.NETHERITE_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE -> ToolGroup.PICKAXE

            Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.IRON_HOE,
            Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE -> ToolGroup.HOE

            else -> ToolGroup.ELSE
        }