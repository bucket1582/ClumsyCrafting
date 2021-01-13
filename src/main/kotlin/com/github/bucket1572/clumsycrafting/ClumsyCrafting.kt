package com.github.bucket1572.clumsycrafting

import com.github.noonmaru.tap.util.updateFromGitHubMagically
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object GlobalObject {
    var isOn: Boolean = false

    // 확률
    const val flakeToolProbability: Double = 0.3 // 뗀석기 발견 확률
    const val flakeBreakProbability: Double = 0.15 // 뗀석기 파괴 확률
    const val cokesDropProbability: Double = 0.05 // 코크스 자연 채광 확률
    const val jewelBreakProbability: Double = 0.2 // 다이아몬드 원석 / 에메랄드 원석 파괴 확률
    const val reinForceBaseFailProbability: Double = 0.4 // 기본 강화 실패 확률
    const val reinForceBaseBreakProbability: Double = 0.22 // 기본 파괴 확률
    const val reinForceFailProbabilityDecrease: Double = 0.03 // 강화 실패 확률 감률
    const val reinForceBreakProbabilityDecrease: Double = 0.02 // 파괴 확률 감률
    const val handworkFailProbabilityDecrease: Double = 0.015 // 세공 실패 확률 감률
    const val handworkBreakProbabilityDecrease: Double = 0.01 // 세공 파괴 확률 감률
    const val itemBreakBaseProbability: Double = 0.9 // 아이템이 기본적으로 파괴되지 않을 확률
    const val sophisticatedCraftingProbability: Double = 0.08 // 정교함 태그가 붙을 확률
    const val bestSteelPickaxeJewelProbability: Double = 0.3 // 강철++ 곡괭이로 보석을 캤을 때 원석이 1개 추가로 나올 확률
    const val qualityDecreaseProbability: Double = 0.15 // 품질 하락 확률

    // 이름
    private val flakeName: String = "${ChatColor.WHITE}뗀석기"
    private val cokesName: String = "${ChatColor.WHITE}코크스"
    private val pigIronName: String = "${ChatColor.WHITE}선철"
    private val castIronName: String = "${ChatColor.WHITE}주철"
    private val steelName: String = "${ChatColor.WHITE}강철"
    val converterName: String = "${ChatColor.WHITE}전로"
    private val banItemName: String = "${ChatColor.RED}이 조합법은 금지되었습니다."

    // 추가 된 재료
    val flake = ItemStack(Material.FLINT) // 뗀석기
    val coke = ItemStack(Material.COAL) // 코크스
    val poorPigIron = ItemStack(Material.IRON_INGOT) // 질 나쁜 선철
    val finePigIron = ItemStack(Material.IRON_INGOT) // 질 좋은 선철
    val poorCastIron = ItemStack(Material.IRON_INGOT) // 질 나쁜 주철
    val fineCastIron = ItemStack(Material.IRON_INGOT) // 질 좋은 주철
    val poorSteel = ItemStack(Material.IRON_INGOT) // 질 나쁜 강철
    val fineSteel = ItemStack(Material.IRON_INGOT) // 질 좋은 강철
    val bestSteel = ItemStack(Material.IRON_INGOT) // 최고 품질 강철
    val standardConverter = ItemStack(Material.CAULDRON)
    val banItem = ItemStack(Material.BARRIER) // 금지

    // 도구와 관련 된 데이터
    const val cokesDryingExperience: Double = 0.5 // 코크스 건류 작업 후 얻는 경험치량
    const val cokesDryingTickTime: Int = 1000 // 코크스 건류 작업 시간
    const val fineIronExp: Int = 6 // 불순물 함량이 낮은 선철을 만들었을 때 얻는 경험치량
    const val defaultMaxQuality: Int = 1 // 무작위 재료의 기본 최고 품질
    const val durabilityCoefficient : Double = 0.8 // 품질에 따른 내구도의 하락비
    const val forgingTicks: Int = 600 // 단조 작업에 걸리는 시간
    const val reinforcingTicks: Int = 300 // 강화 작업에 걸리는 시간
    const val torchBurningTicks: Int = 9600 // 기본 횃불 유지 시간

    // 기타
    const val defaultAttackSpeed: Double = 4.0 // 플레이어 기본 공격 속도
    const val defaultAttackDamage: Double = 2.0 // 플레이어 기본 공격력

    init {
        // 선철
        applyDescription(poorPigIron, pigIronName, 0.0, 1)
        applyDescription(finePigIron, pigIronName, 1.0, 1)

        // 주철
        applyDescription(poorCastIron, castIronName, 0.0, 1)
        applyDescription(fineCastIron, castIronName, 1.0, 1)

        // 강철
        applyDescription(poorSteel, steelName, 0.0, 1)
        applyDescription(fineSteel, steelName, 0.5, 1)
        applyDescription(bestSteel, steelName, 1.0, 1)

        // 전로
        applyDescription(standardConverter, converterName, 0.0, 12)

        // 뗀석기
        applyDescription(flake, flakeName, 0.0, 0)

        // 코크스
        applyDescription(coke, cokesName, 0.0, 0)

        // 밴
        applyDescription(banItem, banItemName, 0.0, 0, specialty("제작 불가"))
    }

    fun isSame(itemA: ItemStack?, itemB: ItemStack?): Boolean =
            /*
            두 아이템의 성분과 설명이 모두 같은지 판단하는 함수, 즉 완벽히 동일한가
             */
            (itemA?.type == itemB?.type) and (itemA?.itemMeta?.lore == itemB?.itemMeta?.lore)

    fun isFundamentallySame(itemA: ItemStack?, itemB: ItemStack?): Boolean =
            /*
            두 아이템의 성분과 이름이 같은지 판단하는 함수
             */
            (itemA?.type == itemB?.type) and (itemA?.itemMeta?.lore?.get(0) == itemB?.itemMeta?.lore?.get(0))
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
    1. 인수 0개 : ClumsyCraft 의 상태가 On 인지 Off 인지 알려줌.
    2. 인수 1개 :
        a. start : ClumsyCraft 의 상태를 On 으로 바꿈.
        b. stop : ClumsyCraft 의 상태를 Off 로 바꿈.
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