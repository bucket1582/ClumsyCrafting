package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object GlobalObject {
    var isOn : Boolean = false
    
    const val flakeToolProbability : Double = 0.3 // 뗀석기 발견 확률
    const val flakeBreakProbability : Double = 0.2 // 뗀석기 파괴 확률
    private val flakeName : String = "${ChatColor.WHITE}뗀석기"
    private val flakeLocalName : String = "Flake"
    
    const val cokesDryingExperience : Double = 0.5 // 코크스 건류 작업 후 얻는 경험치량
    const val cokesDryingTickTime : Int = 1000 // 코크스 건류 작업 시간
    const val cokesDropProbability : Double = 0.05 // 코크스 자연 채광 확률
    private val cokesName : String = "${ChatColor.WHITE}코크스"
    private val cokeLocalName : String = "Coke"

    val fineIronExp : Int = 6 // 불순물 함량이 낮은 선철을 만들었을 때 얻는 경험치량
    private val pigIronName : String = "${ChatColor.WHITE}선철"
    private val castIronName : String = "${ChatColor.WHITE}주철"
    val steelName : String = "${ChatColor.WHITE}강철"
    private val pigIronLocalName : String = "PigIron"
    private val castIronLocalName : String = "CastIron"
    private val steelLocalName : String = "Steel"

    val converterName : String = "${ChatColor.WHITE}전로"
    private val converterLocalName : String = "SteelConverter"

    private val banItemName : String = "${ChatColor.RED}이 조합법은 금지되었습니다."
    private val banItemLocalName : String = "Ban"

    val flake = ItemStack(Material.FLINT) // 뗀석기
    val coke = ItemStack(Material.COAL) // 코크스

    val poorPigIron = ItemStack(Material.IRON_INGOT) // 질 나쁜 선철
    val finePigIron = ItemStack(Material.IRON_INGOT) // 질 좋은 선철
    val poorCastIron = ItemStack(Material.IRON_INGOT) // 질 나쁜 주철
    val fineCastIron = ItemStack(Material.IRON_INGOT) // 질 좋은 주철

    val banItem = ItemStack(Material.BARRIER)

    init {
        // 선철
        poorPigIron.apply {
            val meta = itemMeta
            meta.setDisplayName(pigIronName)
            meta.lore = listOf(
                    pigIronName,
                    "${ChatColor.WHITE}불순물 함량 : ${ChatColor.RED}높음"
            )
            meta.setLocalizedName(pigIronLocalName)
            itemMeta = meta
        }
        
        finePigIron.apply {
            val meta = itemMeta
            meta.setDisplayName(pigIronName)
            meta.lore = listOf(
                    pigIronName,
                    "${ChatColor.WHITE}불순물 함량 : ${ChatColor.GREEN}낮음"
            )
            meta.setLocalizedName(pigIronLocalName)
            itemMeta = meta
        }

        // 주철
        poorCastIron.apply {
            val meta = itemMeta
            meta.setDisplayName(castIronName)
            meta.lore = listOf(
                    castIronName,
                    "${ChatColor.WHITE}불순물 함량 : ${ChatColor.RED}높음"
            )
            meta.setLocalizedName(castIronLocalName)
            itemMeta = meta
        }

        fineCastIron.apply {
            val meta = itemMeta
            meta.setDisplayName(castIronName)
            meta.lore = listOf(
                    castIronName,
                    "${ChatColor.WHITE}불순물 함량 : ${ChatColor.GREEN}낮음"
            )
            meta.setLocalizedName(castIronLocalName)
            itemMeta = meta
        }

        // 뗀석기
        flake.apply {
            val meta = itemMeta
            meta.setDisplayName(flakeName)
            meta.lore = listOf(
                    flakeName
            )
            meta.setLocalizedName(flakeLocalName)
            itemMeta = meta
        }

        // 코크스
        coke.apply {
            val meta = itemMeta
            meta.setDisplayName(cokesName)
            meta.lore = listOf(
                    cokesName
            )
            meta.setLocalizedName(cokeLocalName)
            itemMeta = meta
        }

        // 밴
        banItem.apply {
            val meta = itemMeta
            meta.setDisplayName(banItemName)
            meta.lore = listOf(
                    banItemName
            )
            meta.setLocalizedName(banItemLocalName)
            itemMeta = meta
        }

    }

    fun isSame(itemA : ItemStack?, itemB : ItemStack?) : Boolean {
        return (itemA?.type == itemB?.type) and (itemA?.itemMeta?.lore == itemB?.itemMeta?.lore)
    }

    fun rank(rankDown: Int, maxRank: Int) : String =
        "${ChatColor.WHITE}품질 : [${maxRank - rankDown}/${maxRank}] (${rankDown}랭크↓)"
}

class ClumsyCrafting : JavaPlugin() {
    override fun onEnable() {
        val commandHandler = Commands()
        commandHandler.plugin = this
        this.getCommand("clumsy")?.setExecutor(commandHandler)

        val eventListener = EventListener()
        eventListener.plugin = this
        this.server.pluginManager.registerEvents(eventListener, this)

        ClumsyRecipes.plugin = this
        ClumsyRecipes.loadAll()
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
                if (GlobalObject.isOn) {
                    sender.sendMessage("${ChatColor.GREEN}ClumsyCraft is on.")
                } else {
                    sender.sendMessage("${ChatColor.RED}ClumsyCraft is off.")
                }
            } else if (args.size == 1) {
                if (args[0] == "start") {
                    if (!GlobalObject.isOn) {
                        sender.server.broadcastMessage(
                            "${ChatColor.GREEN}${ChatColor.BOLD}ClumsyCraft is now on."
                        )
                        GlobalObject.isOn = true
                    } else {
                        sender.sendMessage(
                            "${ChatColor.RED}ClumsyCraft is already on."
                        )
                    }
                } else if (args[0] == "stop") {
                    if (GlobalObject.isOn) {
                        sender.server.broadcastMessage(
                            "${ChatColor.RED}${ChatColor.BOLD}ClumsyCraft will no longer bothers you."
                        )
                        GlobalObject.isOn = false
                    } else {
                        sender.sendMessage(
                            "${ChatColor.RED}ClumsyCraft is already off."
                        )
                    }
                } else if (args[0] == "update") {
                    /*
                    plugin?.updateFromGitHubMagically(
                            "bucket1572", "ClumsyCrafting", "ClumsyCrafting.jar",
                            sender::sendMessage)
                     */
                    TODO("Not Yet Implemented")
                } else {
                    sender.sendMessage("${ChatColor.RED}Invalid argument.")
                }
            }
        }
        return true
    }
}