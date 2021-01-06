package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor

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

fun steelTypeToSpecialty(steelType: SteelType) : String =
        when(steelType) {
            SteelType.NONE -> noSteel
            SteelType.CONTAINS_STEEL -> containsSteel
            SteelType.ALL_STEEL -> allSteel
            SteelType.CONTAINS_BEST_STEEL -> containsBestSteel
            SteelType.ALL_BEST_STEEL -> allBestSteel
        }