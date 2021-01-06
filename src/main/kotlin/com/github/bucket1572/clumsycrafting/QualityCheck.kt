package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import kotlin.random.Random

fun rank(rankDown: Double, maxRank: Int): String = // 사용 된 철의 품질
        "${ChatColor.WHITE}품질 : [%.1f/${maxRank}] (%.1f ↓)".format(maxRank - rankDown, rankDown)

fun specialty(steelType: SteelType): String =
        steelTypeToSpecialty(steelType)

fun specialty(anythingElse: String): String =
        "${ChatColor.WHITE}$anythingElse"

fun description(name: String, quality: Double, maxQuality: Int, vararg specialties: String): List<String> =
        listOf(
                "${ChatColor.WHITE}이름 : $name",
                rank(maxQuality - quality, maxQuality),
                "${ChatColor.WHITE}특수 : ",
                *specialties
        )

fun applyDescription(itemStack: ItemStack, quality: Double, maxQuality: Int, vararg specialties: String) =
        when {
            itemStack.itemMeta.hasDisplayName() ->
                itemStack.apply {
                    val meta = itemMeta
                    val name = itemMeta.displayName
                    meta.lore = description(
                            name, quality, maxQuality, *specialties
                    )
                    itemMeta = meta
                }
            else ->
                itemStack.apply {
                    val meta = itemMeta
                    val name =
                            itemStack.type.name.substringAfter(":").replace('_', ' ').capitalize()
                    meta.lore = description(
                            name, quality, maxQuality, *specialties
                    )
                    itemMeta = meta
                }
        }

fun applyDescription(itemStack: ItemStack, name: String, quality: Double, maxQuality: Int, vararg specialties: String) =
        itemStack.apply {
            val meta = itemMeta
            meta.setDisplayName(name)
            meta.lore = description(
                    name, quality, maxQuality, *specialties
            )
            itemMeta = meta
        }

fun applyDescription(itemStack: ItemStack, quality: Double, maxQuality: Int, specialties: List<String>) {
        val specials = specialties.toTypedArray()
        applyDescription(itemStack, quality, maxQuality, *specials)
}

fun setDurabilityForTools(itemStack: ItemStack, maxDurability: Number, durability: Number) =
        itemStack.apply {
            val meta = itemMeta as Damageable
            meta.damage = (maxDurability.toDouble() - durability.toDouble()).toInt()
            itemMeta = meta as ItemMeta
        }

fun setQuality(itemStack: ItemStack) : Double {
    var random = Random.nextDouble()
    random = Math.round(random * 10) / 10.0
    val quality = random * GlobalObject.defaultMaxQuality
    applyDescription(itemStack, quality, GlobalObject.defaultMaxQuality)
    return random
}

fun setMaxQuality(itemStack: ItemStack) : Int {
    var random = Random.nextDouble()
    random = Math.round(random * 10) / 10.0
    val quality = random * GlobalObject.defaultMaxQuality
    applyDescription(itemStack, quality, GlobalObject.defaultMaxQuality)
    return GlobalObject.defaultMaxQuality
}


fun getQuality(itemStack: ItemStack) : Double =
        if (itemStack.hasItemMeta()) {
            val meta = itemStack.itemMeta
            if (meta.hasLore()) {
                val lore = meta.lore!!
                if (lore.size < 2) {
                    setQuality(itemStack)
                } else {
                    val qualityString = lore[1]
                    val tmp = qualityString.substringAfter('[').substringBefore(']')
                    val quality = tmp.substringBefore('/').toDouble()
                    val maxQuality = tmp.substringAfter('/').toInt()

                    if (maxQuality != 0) {
                        quality / maxQuality
                    } else {
                        setQuality(itemStack)
                    }
                }
            } else {
                setQuality(itemStack)
            }
        } else {
            setQuality(itemStack)
        }

fun getMaxQuality(itemStack: ItemStack) : Int =
        if (itemStack.hasItemMeta()) {
            val meta = itemStack.itemMeta
            if (meta.hasLore()) {
                val lore = meta.lore!!
                if (lore.size < 2) {
                    setMaxQuality(itemStack)
                } else {
                    val qualityString = lore[1]
                    val tmp = qualityString.substringAfter('[').substringBefore(']')
                    val maxQuality = tmp.substringAfter('/').toInt()

                    if (maxQuality != 0) {
                        maxQuality
                    } else {
                        setMaxQuality(itemStack)
                    }
                }
            } else {
                setMaxQuality(itemStack)
            }
        } else {
            setMaxQuality(itemStack)
        }

fun getSpecialties(itemStack: ItemStack) : List<String> =
        if (itemStack.hasItemMeta()) {
            val meta = itemStack.itemMeta
            if (meta.hasLore()) {
                val lore = meta.lore!!
                if (lore.size < 4) {
                    listOf()
                } else {
                    lore.subList(4, lore.size)
                }
            } else {
                listOf()
            }
        } else {
            listOf()
        }