package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.inventory.ItemStack

private val noSteel = "${ChatColor.WHITE}주철/선철"
private val containsSteel = "${ChatColor.GOLD}강철" // 강철 포함
private val allSteel = "${ChatColor.YELLOW}강철" // All 강철
private val containsBestSteel = "${ChatColor.YELLOW}강철+" // 2랭크 강철 포함
private val allBestSteel = "${ChatColor.YELLOW}강철++" // All 2랭크 강철

enum class SteelType {
    NONE,
    CONTAINS_STEEL,
    ALL_STEEL,
    CONTAINS_BEST_STEEL,
    ALL_BEST_STEEL
}

fun getSteelType(itemStack: ItemStack) : SteelType? =
        getSpecialties(itemStack).let {
            val specialties = it
            when {
                specialties.contains(noSteel) -> {
                    SteelType.NONE
                }
                specialties.contains(containsSteel) -> {
                    SteelType.CONTAINS_STEEL
                }
                specialties.contains(allSteel) -> {
                    SteelType.ALL_STEEL
                }
                specialties.contains(containsBestSteel) -> {
                    SteelType.CONTAINS_BEST_STEEL
                }
                specialties.contains(allBestSteel) -> {
                    SteelType.ALL_BEST_STEEL
                }
                else -> {
                    null
                }
            }
        } // 강철 태그를 읽어 오는 함수