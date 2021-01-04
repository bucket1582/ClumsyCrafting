package com.github.bucket1572.clumsycrafting

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
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class EventListener : Listener {
    @EventHandler
    fun onBlockBreakingEvent(event: BlockBreakEvent) {
        val player: Player = event.player

        val block: Block = event.block
        val blockGroup: BlockGroup = getBlockGroup(block.type)

        val tool: ItemStack? = player.inventory.itemInMainHand
        val toolGroup: ToolGroup = getToolGroup(tool?.type)

        if ((blockGroup == BlockGroup.WOOD) and (toolGroup != ToolGroup.AXE)) {
            //나무 블록을 캐는데 도끼가 아닐 경우
            player.damage(1.0)
            if ((tool?.type != Material.FLINT) or (tool?.itemMeta?.displayName != "${ChatColor.WHITE}Flake")) {
                // 뗀석기를 사용하지 않았을 경우
                event.isCancelled = true
            } else {
                // 사용한 도구가 뗀석기일 경우
                /*
                뗀석기를 사용하여 나무를 캐면, GlobalFields.flakeBreakProbability의 확률로 파괴 됨.
                 */
                val random: Double = Random.nextDouble()
                if (random < GlobalFields.flakeBreakProbability) {
                    player.inventory.setItemInMainHand(null)
                }
            }
        } else if (
                listOf(
                        blockGroup == BlockGroup.ORE,
                        blockGroup == BlockGroup.STONE,
                        blockGroup == BlockGroup.TERRACOTTA,
                        blockGroup == BlockGroup.CONCRETE
                ).any() and (toolGroup != ToolGroup.PICKAXE)
        ) {
            // 돌, 광석, 테라코타를 캐는데 곡괭이가 아닐 경우
            player.damage(2.0)
            event.isCancelled = true
        } else if ((blockGroup == BlockGroup.CROP) and (toolGroup != ToolGroup.HOE)) {
            // 농작물을 수확하는데 괭이가 아닐 경우
            event.isDropItems = false // 캘 수는 있지만, 아이템 드랍이 되지 않음.
        }
    }

    @EventHandler
    fun onBlockFallingEvent(event: EntityChangeBlockEvent) {
        val fallingBlock: Entity = event.entity
        val block: Material = event.to
        if ((fallingBlock.type == EntityType.FALLING_BLOCK) and (block == Material.GRAVEL)) {
            /*
            자갈을 떨어뜨리면 GlobalFields.flakeToolProbability의 확률로 뗀석기가 나온다.
             */
            val random: Double = Random.nextDouble()
            if (random < GlobalFields.flakeToolProbability) {
                val dropItem = ItemStack(Material.FLINT, 1)
                dropItem.apply {
                    val meta = itemMeta
                    meta.setDisplayName("${ChatColor.WHITE}Flake")
                    itemMeta = meta
                }
                fallingBlock.world.dropItemNaturally(
                        fallingBlock.location, dropItem
                )
                event.isCancelled = true
            }
        }
    }
}