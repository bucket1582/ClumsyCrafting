package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.time.temporal.TemporalAmount
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

fun rank(rankDown: Double, maxRank: Int): String = // 사용 된 철의 품질
        "${ChatColor.WHITE}품질 : [%.1f/${maxRank}] (%.1f ↓)".format(maxRank - rankDown, rankDown) // 품질 생성

fun specialty(tag: SpecialtyTags): String =
        specialtyToTag(tag) // 특수 태그

fun specialty(anythingElse: String): String =
        "${ChatColor.WHITE}$anythingElse" // 기타 특기 사항

fun description(name: String, quality: Double, maxQuality: Int, vararg specialties: String): List<String> =
        listOf(
                "${ChatColor.WHITE}이름 : $name",
                rank(maxQuality - quality, maxQuality),
                "${ChatColor.WHITE}특수 : ",
                *specialties
        ) // 설명을 만드는 함수

fun applyDescription(itemStack: ItemStack, quality: Double, maxQuality: Int, vararg specialties: String) =
        if (itemStack.hasItemMeta()) {
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
        } // 아이템 메타가 있을 경우
        else {
            itemStack.apply {
                val meta = itemMeta
                val name =
                        itemStack.type.name.substringAfter(":").replace('_', ' ').capitalize()
                meta.lore = description(
                        name, quality, maxQuality, *specialties
                )
                itemMeta = meta
            }
        } // 아이템 메타가 없을 경우

fun applyDescription(itemStack: ItemStack, name: String, quality: Double, maxQuality: Int, vararg specialties: String) =
        itemStack.apply {
            val meta = itemMeta
            meta.setDisplayName(name)
            meta.lore = description(
                    name, quality, maxQuality, *specialties
            )
            itemMeta = meta
        } // 설명 적용

fun applyDescription(itemStack: ItemStack, quality: Double, maxQuality: Int, specialties: List<String>) {
    val specials = specialties.toTypedArray()
    applyDescription(itemStack, quality, maxQuality, *specials)
} // 설명 적용

fun addSpecialty(itemStack: ItemStack, update: Boolean, vararg specialties: String) {
    if (update)
        getQuality(itemStack) // description 업데이트
    val updateLore = itemStack.lore!!.toMutableList() // 기존 특수 태그
    updateLore.addAll(specialties)
    itemStack.apply {
        val meta = itemMeta
        meta.lore = updateLore
        itemMeta = meta
    }
} // 특수 태그 추가

fun setDurabilityForTools(itemStack: ItemStack, maxDurability: Number, durability: Number) =
        itemStack.apply {
            val meta = itemMeta as Damageable
            meta.damage = (maxDurability.toDouble() - durability.toDouble()).toInt()
            itemMeta = meta as ItemMeta
        } // 내구도 설정

fun setQuality(itemStack: ItemStack): Double {
    var random = Random.nextDouble()
    random = (random * 10).roundToInt() / 10.0
    val quality = random * GlobalObject.defaultMaxQuality
    applyDescription(itemStack, quality, GlobalObject.defaultMaxQuality)
    return random
} // 품질 설정

fun setMaxQuality(itemStack: ItemStack): Int {
    var random = Random.nextDouble()
    random = (random * 10).roundToInt() / 10.0
    val quality = random * GlobalObject.defaultMaxQuality
    applyDescription(itemStack, quality, GlobalObject.defaultMaxQuality)
    return GlobalObject.defaultMaxQuality
} // 최대 품질 설정

fun getQuality(itemStack: ItemStack): Double =
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
        }
        else {
            setQuality(itemStack)
        } // 품질을 읽어 오는 함수

fun getQuality(qualityString: String): Double {
    val tmp = qualityString.substringAfter('[').substringBefore(']')
    val quality = tmp.substringBefore('/').toDouble()
    val maxQuality = tmp.substringAfter('/').toInt()

    return if (maxQuality != 0) {
        quality / maxQuality
    } else {
        0.5
    }
} // 품질을 읽어 오는 함수


fun getMaxQuality(itemStack: ItemStack): Int =
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
        }
        else {
            setMaxQuality(itemStack)
        } // 최대 품질을 읽어오는 함수, default : 1

fun getMaxQuality(qualityString: String): Int {
    val tmp = qualityString.substringAfter('[').substringBefore(']')
    val maxQuality = tmp.substringAfter('/').toInt()

    return if (maxQuality != 0) {
        maxQuality
    } else {
        1
    }
} // 최대 품질을 읽어 오는 함수

fun addAttribute(itemStack: ItemStack, attribute: Attribute, name: String, amount: Double, equipmentSlot: EquipmentSlot) {
    itemStack.apply {
        val meta = itemMeta
        meta.addAttributeModifier(
                attribute,
                AttributeModifier(
                        UUID.randomUUID(), name,
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER, equipmentSlot
                )
        )
        itemMeta = meta
    }
}