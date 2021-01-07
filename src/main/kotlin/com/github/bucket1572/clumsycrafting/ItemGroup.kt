package com.github.bucket1572.clumsycrafting

import org.bukkit.Material

enum class ItemGroup {
    TOOL,
    INTERACTIVE_BLOCK,
    UPGRADE_BLOCK,
    SOLID_BLOCK,
    ELSE_BLOCK,
    FOOD,
    TORCH,
    SKIP,
    ELSE
}

fun getItemGroup(material: Material?): ItemGroup =
        /*
        아이템 그룹을 가져오는 함수
         */
        if (material?.isItem != true) {
            ItemGroup.ELSE
        }
        else if (getToolGroup(material) != ToolGroup.ELSE) {
            ItemGroup.TOOL
        } // 도구인지 판단
        else if (material.isBlock) { // 블록인지 판단
            when {
                (material == Material.TNT) or
                        (material.name.contains("BED")) or
                        (material == Material.RESPAWN_ANCHOR) or
                        (material.name.contains("SIGN")) -> {
                    ItemGroup.SKIP
                }
                material.isInteractable -> { // 상호작용 가능한 블록
                    ItemGroup.INTERACTIVE_BLOCK
                }
                material == Material.BOOKSHELF -> { // 책장
                    ItemGroup.UPGRADE_BLOCK
                }
                (material == Material.TORCH) or (material == Material.REDSTONE_TORCH) -> { // 횃불
                    ItemGroup.TORCH
                }
                material.isSolid -> { // 위의 모든 것을 제외하고 고체
                    ItemGroup.SOLID_BLOCK
                }
                else -> { // 위의 모든 것을 제외한 블록 (예 : 물 / 용암)
                    ItemGroup.ELSE_BLOCK
                }
            }
        }
        else {
            if (material.isEdible) {
                ItemGroup.FOOD
            } else {
                ItemGroup.ELSE
            }
        }