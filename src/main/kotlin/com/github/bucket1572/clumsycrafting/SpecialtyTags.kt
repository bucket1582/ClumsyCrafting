package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.inventory.ItemStack

enum class SpecialtyTags {
    NO_STEEL, // 강철 미포함 태그
    CONTAINS_STEEL, // 강철 포함 태그
    ALL_STEEL, // 강철 태그
    CONTAINS_STEEL_PLUS, // 강철+ 태그
    BEST_STEEL, // 강철++ 태그
    SOPHISTICATED // 정교함 태그
}

fun specialtyToTag(tag: SpecialtyTags): String =
        when(tag) {
            SpecialtyTags.NO_STEEL -> "${ChatColor.WHITE}주철/선철"
            SpecialtyTags.CONTAINS_STEEL -> "${ChatColor.GOLD}강철"
            SpecialtyTags.ALL_STEEL -> "${ChatColor.YELLOW}강철"
            SpecialtyTags.CONTAINS_STEEL_PLUS -> "${ChatColor.YELLOW}강철+"
            SpecialtyTags.BEST_STEEL -> "${ChatColor.YELLOW}강철++"
            SpecialtyTags.SOPHISTICATED -> "${ChatColor.GREEN}정교함"
        } // 특수 태그

fun getSpecialties(itemStack: ItemStack): List<String> =
        if (itemStack.hasItemMeta()) {
            val meta = itemStack.itemMeta
            if (meta.hasLore()) {
                val lore = meta.lore!!
                if (lore.size < 4) {
                    listOf()
                } else {
                    lore.subList(3, lore.size)
                }
            } else {
                listOf()
            }
        }
        else {
            listOf()
        } // 특수 태그를 읽어 오는 함수