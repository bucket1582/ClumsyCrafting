package com.github.bucket1572.clumsycrafting

import com.github.noonmaru.tap.event.EntityProvider
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

class EventListener : Listener {
    var plugin: JavaPlugin? = null

    @EventHandler
    fun onBlockBreakingEvent(event: BlockBreakEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val player: Player = event.player

        val block: Block = event.block
        val blockGroup: BlockGroup = getBlockGroup(block.type)

        val tool: ItemStack? = player.inventory.itemInMainHand
        val toolGroup: ToolGroup = getToolGroup(tool?.type)

        // 맞는 도구 사용했는지 판단
        if ((blockGroup == BlockGroup.WOOD) and (toolGroup != ToolGroup.AXE)) {
            //나무 블록을 캐는데 도끼가 아닐 경우
            player.damage(1.0)
            if (GlobalObject.isSame(tool, GlobalObject.flake)) {
                // 뗀석기를 사용하지 않았을 경우
                event.isCancelled = true
            } else {
                // 사용한 도구가 뗀석기일 경우
                /*
                뗀석기를 사용하여 나무를 캐면, GlobalFields.flakeBreakProbability의 확률로 파괴 됨.
                 */
                val random: Double = Random.nextDouble()
                if (random < GlobalObject.flakeBreakProbability) {
                    if (tool!!.amount == 1)
                        player.inventory.setItemInMainHand(null)
                    else {
                        tool.amount -= 1
                        player.inventory.setItemInMainHand(tool)
                    }
                }
            }
        } else if (
                ((blockGroup == BlockGroup.ORE)
                        or (blockGroup == BlockGroup.STONE)
                        or (blockGroup == BlockGroup.TERRACOTTA)
                        or (blockGroup == BlockGroup.CONCRETE))
                and (toolGroup != ToolGroup.PICKAXE)
        ) {
            // 돌, 광석, 테라코타를 캐는데 곡괭이가 아닐 경우
            player.damage(2.0)
            event.isCancelled = true
        } else if ((blockGroup == BlockGroup.CROP) and (toolGroup != ToolGroup.HOE)) {
            // 농작물을 수확하는데 괭이가 아닐 경우
            event.isDropItems = false // 캘 수는 있지만, 아이템 드랍이 되지 않음.
        }

        // 특수
        if (block.type == Material.COAL_ORE) {
            // 석탄 자연 채광
            val random = Random.nextDouble()
            if (random < GlobalObject.cokesDropProbability) {
                val coke = GlobalObject.coke.clone()
                player.world.dropItemNaturally(event.block.location, coke)
            }
        }
    }

    @EventHandler
    fun onBlockFallingEvent(event: EntityChangeBlockEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val fallingBlock: Entity = event.entity
        val block: Material = event.to
        if ((fallingBlock.type == EntityType.FALLING_BLOCK) and (block == Material.GRAVEL)) {
            /*
            자갈을 떨어뜨리면 GlobalFields.flakeToolProbability의 확률로 뗀석기가 나온다.
             */
            val random: Double = Random.nextDouble()
            if (random < GlobalObject.flakeToolProbability) {
                val dropItem = GlobalObject.flake.clone()

                fallingBlock.world.dropItemNaturally(
                        fallingBlock.location, dropItem
                )
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onCraftingEvent(event: CraftItemEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        if (event.currentItem?.type == Material.BARRIER) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPrepareCraftingEvent(event: PrepareItemCraftEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val matrix = event.inventory.matrix
        val recipe: Recipe = event.recipe ?: return
        if (matrix.size == 9) { // 작업대
            val ironCount = countIron(matrix)
            if ((ironCount["Iron"]!! > 0)
                    and (recipe.result.type.maxDurability > 0)) {
                val rankDown =
                        ironCount["PoorPigIron"]!! * 2 +
                                ironCount["FinePigIron"]!! +
                                ironCount["PoorCastIron"]!! +
                                ironCount["PoorSteel"]!!
                val maxRank = (ironCount["Iron"] ?: error("3")) * 2
                val poorSteelCount = ironCount["PoorSteel"]!!
                val fineSteelCount = ironCount["FineSteel"]!!
                val bestSteelCount = ironCount["BestSteel"]!!
                val steelCount = poorSteelCount + fineSteelCount + bestSteelCount

                val maxDurability = recipe.result.type.maxDurability

                var durability = maxDurability.toDouble()
                durability /= (2.0).pow(rankDown)

                val result = recipe.result.clone()
                if (steelCount == ironCount["Iron"]!!) {
                    if (bestSteelCount == ironCount["Iron"]!!){
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철++"
                            )
                            val damageableMeta = meta as Damageable
                            damageableMeta.damage = (maxDurability - durability).toInt()
                            itemMeta = damageableMeta as ItemMeta
                        }
                    } else if (bestSteelCount > 0) {
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철+"
                            )
                            val damageableMeta = meta as Damageable
                            damageableMeta.damage = (maxDurability - durability).toInt()
                            itemMeta = damageableMeta as ItemMeta
                        }
                    } else {
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철"
                            )
                            val damageableMeta = meta as Damageable
                            damageableMeta.damage = (maxDurability - durability).toInt()
                            itemMeta = damageableMeta as ItemMeta
                        }
                    }
                } else if (steelCount > 0) {
                    result.apply {
                        val meta = itemMeta
                        meta.lore = listOf(
                                GlobalObject.rank(rankDown, maxRank),
                                "${ChatColor.GOLD}강철"
                        )
                        val damageableMeta = meta as Damageable
                        damageableMeta.damage = (maxDurability - durability).toInt()
                        itemMeta = damageableMeta as ItemMeta
                    }
                } else {
                    result.apply {
                        val meta = itemMeta
                        meta.lore = listOf(
                                GlobalObject.rank(rankDown, maxRank),
                        )
                        val damageableMeta = meta as Damageable
                        damageableMeta.damage = (maxDurability - durability).toInt()
                        itemMeta = damageableMeta as ItemMeta
                    }
                }
                event.inventory.result = result
            } else if (ironCount["Iron"]!! > 0) {
                if (recipe.result.itemMeta.displayName == GlobalObject.steelName) return
                val rankDown =
                        ironCount["PoorPigIron"]!! * 2 +
                                ironCount["FinePigIron"]!! +
                                ironCount["PoorCastIron"]!! +
                                ironCount["PoorSteel"]!!
                val maxRank = (ironCount["Iron"] ?: error("3")) * 2
                val poorSteelCount = ironCount["PoorSteel"]!!
                val fineSteelCount = ironCount["FineSteel"]!!
                val bestSteelCount = ironCount["BestSteel"]!!
                val steelCount = poorSteelCount + fineSteelCount + bestSteelCount

                val result = recipe.result.clone()
                if (steelCount == ironCount["Iron"]!!) {
                    if (bestSteelCount == ironCount["Iron"]!!) {
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철++"
                            )
                            itemMeta = meta
                        }
                    } else if (bestSteelCount > 0) {
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철+"
                            )
                            itemMeta = meta
                        }
                    } else {
                        result.apply {
                            val meta = itemMeta
                            meta.lore = listOf(
                                    GlobalObject.rank(rankDown, maxRank),
                                    "${ChatColor.YELLOW}강철"
                            )
                            itemMeta = meta
                        }
                    }
                } else if (steelCount > 0) {
                    result.apply {
                        val meta = itemMeta
                        meta.lore = listOf(
                                GlobalObject.rank(rankDown, maxRank),
                                "${ChatColor.GOLD}강철"
                        )
                        itemMeta = meta
                    }
                } else {
                    result.apply {
                        val meta = itemMeta
                        meta.lore = listOf(
                                GlobalObject.rank(rankDown, maxRank),
                        )
                        itemMeta = meta
                    }
                }
                event.inventory.result = result
            }
        }
    }

    @EventHandler
    fun onSmelting(event: FurnaceExtractEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        if (event.itemType == Material.IRON_INGOT) {
            val player: Player = event.player
            val itemInMainHand: ItemStack = player.inventory.itemInMainHand
            val itemInOffHand: ItemStack = player.inventory.itemInOffHand
            val mainHandCount =
                    if (GlobalObject.isSame(itemInMainHand, GlobalObject.coke)) {
                        itemInMainHand.amount
                    } else {
                        0
                    } // 많이 사용하는 손에 있는 코크스 개수
            val offHandCount =
                    if (GlobalObject.isSame(itemInOffHand, GlobalObject.coke)) {
                        itemInOffHand.amount
                    } else {
                        0
                    } // 적게 사용하는 손에 있는 코크스 개수
            val cokesCount = mainHandCount + offHandCount // 손에 든 코크스 개수 총합

            if (cokesCount > event.itemAmount) {
                // 손에 든 코크스의 개수가 주철 수보다 많을 경우
                event.expToDrop = GlobalObject.fineIronExp

                val castIron: ItemStack = GlobalObject.finePigIron.clone() // 주철 세팅
                castIron.amount = event.itemAmount

                val poorIron: ItemStack = GlobalObject.poorPigIron.clone() // 예전 주철 세팅
                poorIron.amount = event.itemAmount

                val copiedItemStack: ItemStack? = player.inventory.removeItemAnySlot(poorIron)[0]
                if (copiedItemStack != null) return // 화로에 아이템을 들고 아이템을 가져올 때 생기는 복사 버그 방지
                player.inventory.addItem(castIron)

                val cokes = GlobalObject.coke.clone()
                when {
                    mainHandCount > event.itemAmount -> {
                        // 많이 사용하는 손에 든 코크스의 개수가 주철 수보다 많을 경우
                        cokes.amount = mainHandCount - event.itemAmount
                        player.inventory.setItemInMainHand(cokes)
                    }
                    mainHandCount == event.itemAmount -> {
                        player.inventory.setItemInMainHand(null)
                    }
                    else -> {
                        if (mainHandCount > 0) {
                            player.inventory.setItemInMainHand(null)
                        }
                        cokes.amount = offHandCount - (event.itemAmount - mainHandCount)
                        player.inventory.setItemInOffHand(cokes)
                    }
                }
            }
        }
    }
}