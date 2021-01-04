package com.github.bucket1572.clumsycrafting

import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.util.updateFromGitHubMagically
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.random.Random

object GlobalFields {
    var isOn : Boolean = false
    var flakeToolProbability : Double = 0.3
}

class ClumsyCrafting : JavaPlugin() {
    override fun onEnable() {
        val commandHandler = Commands()
        commandHandler.plugin = this
        this.getCommand("clumsy")?.setExecutor(commandHandler)
        this.server.pluginManager.registerEvents(EventListener(), this)
    }
}

private class Commands : CommandExecutor {
    var plugin: JavaPlugin? = null
    /*
    ClumsyCraft 커맨드
    1. 인수 0개 : ClumsyCraft의 상태가 On인지 Off인지 알려줌.
    2. 인수 1개 :
        a. start : ClumsyCraft의 상태를 On으로 바꿈.
        b. stop : ClumsyCraft의 상태를 Off로 바꿈.
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "clumsy") {
            if (args.isEmpty()) {
                if (GlobalFields.isOn) {
                    sender.sendMessage("${ChatColor.GREEN}ClumsyCraft is on.")
                } else {
                    sender.sendMessage("${ChatColor.RED}ClumsyCraft is off.")
                }
            } else if (args.size == 1) {
                if (args[0] == "start") {
                    if (!GlobalFields.isOn) {
                        sender.server.broadcastMessage(
                            "${ChatColor.GREEN}${ChatColor.BOLD}ClumsyCraft is now on."
                        )
                        GlobalFields.isOn = true
                    } else {
                        sender.sendMessage(
                            "${ChatColor.RED}ClumsyCraft is already on."
                        )
                    }
                } else if (args[0] == "stop") {
                    if (GlobalFields.isOn) {
                        sender.server.broadcastMessage(
                            "${ChatColor.RED}${ChatColor.BOLD}ClumsyCraft will no longer bothers you."
                        )
                        GlobalFields.isOn = false
                    } else {
                        sender.sendMessage(
                            "${ChatColor.RED}ClumsyCraft is already off."
                        )
                    }
                } else if (args[0] == "update") {
                    plugin?.updateFromGitHubMagically(
                            "bucket1572", "ClumsyCrafting", "ClumsyCrafting.jar",
                            sender::sendMessage)
                } else {
                    sender.sendMessage("${ChatColor.RED}Invalid argument.")
                }
            }
        }
        return true
    }
}

enum class BlockGroup {
    WOOD, ELSE
}

enum class ToolGroup {
    AXE, ELSE
}

fun getBlockGroup(material: Material) : BlockGroup =
    when(material) {
        Material.ACACIA_WOOD, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD,
        Material.JUNGLE_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD,
        Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_BIRCH_WOOD,
        Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_JUNGLE_WOOD,
        Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD,
        Material.CRIMSON_STEM, Material.WARPED_STEM, Material.STRIPPED_CRIMSON_STEM,
        Material.STRIPPED_WARPED_STEM, Material.ACACIA_LOG, Material.BIRCH_LOG,
        Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG,
        Material.SPRUCE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_OAK_LOG,
        Material.STRIPPED_SPRUCE_LOG, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE,
        Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE -> BlockGroup.WOOD
        else -> BlockGroup.ELSE
    }

fun getToolGroup(material: Material) : ToolGroup =
    when(material) {
        Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
        Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE -> ToolGroup.AXE
        else -> ToolGroup.ELSE
    }

private class EventListener : Listener {
    @EventHandler
    fun onBlockBreakingEvent(event: BlockBreakEvent) {
        val player: Player = event.player

        val block: Block = event.block
        val blockGroup: BlockGroup = getBlockGroup(block.type)

        val tool: ItemStack = player.inventory.itemInMainHand
        val toolGroup: ToolGroup = getToolGroup(tool.type)

        if ((blockGroup == BlockGroup.WOOD) and (toolGroup != ToolGroup.AXE)) {
            //나무 블록을 캐는데 도끼가 아닐 경우
            player.damage(1.0)
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockFallingEvent(event: EntityChangeBlockEvent) {
        val fallingBlock: Entity = event.entity
        val block: Material = event.to
        if ((fallingBlock.type == EntityType.FALLING_BLOCK) and (block == Material.GRAVEL)) {
            val random: Double = Random.nextDouble()
            if (random < GlobalFields.flakeToolProbability) {
                val dropItem = ItemStack(Material.FLINT, 1)
                dropItem.apply {
                    val meta = itemMeta
                    meta.setDisplayName("${ChatColor.WHITE}Flake")
                }
                fallingBlock.world.dropItemNaturally(
                        fallingBlock.location, dropItem
                )
                event.isCancelled = true
            }
        }
    }
}