package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

object GlobalFields {
    var isOn : Boolean = false
    var flakeToolProbability : Double = 0.3 // 뗀석기 발견 확률
    var flakeBreakProbability : Double = 0.2 // 뗀석기 파괴 확률
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