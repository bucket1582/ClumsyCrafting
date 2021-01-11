package com.github.bucket1572.clumsycrafting

import com.github.noonmaru.tap.effect.playFirework
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

class EventListener : Listener {
    var plugin: JavaPlugin? = null
    private val enabledBlocks = ArrayList<Block>()

    private fun writeMarker(location: Location, realQuality: Double, maxQuality: Int) {
        val markerLocation: Location = location.let {
            Location(it.world, it.x + 0.5, it.y + 1.2, it.z + 0.5)
        } // 마커 위치
        val world = location.world
        // 상호작용 가능한 블록이거나 업그레이드에 사용되는 블록일 경우, 품질을 표시(기억)한다.
        world.spawn(markerLocation, ArmorStand::class.java).apply {
            // 이름 (퀄리티)
            customName = rank(maxQuality - realQuality * maxQuality, maxQuality)

            // 기본 세팅
            isVisible = false // 안 보임.
            isMarker = true // 마커임.
            isCollidable = false // 충돌 불가
            isCustomNameVisible = true // 이름은 보임.
            isInvulnerable = true // 때릴 수 없음.
            setGravity(false) // 중력 없음.

            // 슬롯 세팅
            disabledSlots.add(EquipmentSlot.FEET)
            disabledSlots.add(EquipmentSlot.CHEST)
            disabledSlots.add(EquipmentSlot.HAND)
            disabledSlots.add(EquipmentSlot.LEGS)
            disabledSlots.add(EquipmentSlot.OFF_HAND)
            disabledSlots.add(EquipmentSlot.HEAD)
        } // 품질 표시
    }
    /*
    마커 작성
    Input
    -----
    location : 작성 위치
    realQuality : 실제 품질 (0과 1 사이)
    maxQuality : 최대 품질

    설명
    ---
    지정한 위치에 품질을 표시합니다.

    Return
    ------
    없음.
     */

    private fun eraseMarker(block: Block) {
        // 마커 지우기
        val itemGroup = getItemGroup(block.type)
        if ((itemGroup == ItemGroup.INTERACTIVE_BLOCK) or (itemGroup == ItemGroup.UPGRADE_BLOCK)) {
            val markerLocation: Location = block.location.let {
                Location(it.world, it.x + 0.5, it.y + 1.2, it.z + 0.5)
            } // 마커 지점
            markerLocation.getNearbyEntitiesByType(ArmorStand::class.java, 0.3).forEach {
                it.remove()
            } // 제거
        }
    }
    /*
    마커 지우기
    Input
    -----
    block : 마커와 연결 된 블록

    설명
    ---
    지정한 블록과 연결 된 마커를 지웁니다.

    Return
    ------
    없음.
     */

    private fun getMarker(block: Block): String? {
        // 마커 값 읽기
        val itemGroup = getItemGroup(block.type)
        return if ((itemGroup == ItemGroup.INTERACTIVE_BLOCK) or (itemGroup == ItemGroup.UPGRADE_BLOCK)) {
            val markerLocation: Location = block.location.let {
                Location(it.world, it.x + 0.5, it.y + 1.2, it.z + 0.5)
            } // 마커 지점
            val listOfMarkers = markerLocation.getNearbyEntitiesByType(ArmorStand::class.java, 0.3)
            if (listOfMarkers.isEmpty()) {
                null
            } else {
                listOfMarkers.first().customName
            }
        } else {
            null
        }
    }
    /*
    마커 읽기
    Input
    -----
    block : 마커와 연결 된 블록

    설명
    ---
    지정한 블록의 품질 문자열을 읽어옵니다. 만약, 품질 문자열이 없다면, null 을 반환합니다.

    Return
    ------
    품질 문자열 / null
     */

    private fun getMarkerEntity(block: Block): Entity? {
        // 마커 불러오기
        val itemGroup = getItemGroup(block.type)
        return if ((itemGroup == ItemGroup.INTERACTIVE_BLOCK) or (itemGroup == ItemGroup.UPGRADE_BLOCK)) {
            val markerLocation: Location = block.location.let {
                Location(it.world, it.x + 0.5, it.y + 1.2, it.z + 0.5)
            } // 마커 지점
            val listOfMarkers = markerLocation.getNearbyEntitiesByType(ArmorStand::class.java, 0.3)
            if (listOfMarkers.isEmpty()) {
                null
            } else {
                listOfMarkers.first()
            }
        } else {
            null
        }
    }
    /*
    마커 엔티티 가져오기
    Input
    -----
    block : 마커와 연결 된 블록

    설명
    ---
    지정한 블록과 연결 된 마커의 아머스탠드를 가져옵니다. 없다면, null 을 반환합니다.

    Return
    ------
    연결 된 마커의 아머스탠드 / null
     */

    private fun reinforceTables(block: Block, player: Player, item: String) {
        val marker: Entity = getMarkerEntity(block) ?: return // 마커가 없을 경우 강화할 수 없다.
        val qualityString = marker.customName
        val quality = qualityString?.let { getQuality(it) } ?: return // 마커가 없을 경우 강화할 수 없다.
        val maxQuality = getMaxQuality(qualityString)
        val itemInHand: ItemStack = player.inventory.itemInMainHand

        if (itemInHand.type.name.contains(item)) { // 맞는 재료일 경우
            val addQuality = getQuality(itemInHand) * getMaxQuality(itemInHand) // 자체 품질
            val newQuality = min(quality * maxQuality + addQuality, maxQuality.toDouble()) // 새로운 자체 품질
            itemInHand.amount -= 1
            player.inventory.setItemInMainHand(itemInHand)
            marker.customName = rank(maxQuality - newQuality, maxQuality) // 품질 표시 업데이트
        }
    }
    /*
    마커 강화
    Input
    -----
    block : 마커와 연결 된 블록
    player : 현재 강화를 시도하는 플레이어
    item : 강화 재료

    설명
    ---
    지정한 블록과 연결 된 마커가 있다면, 해당 마커의 자체 품질을 강화 재료의 자체 품질만큼 증가시킵니다.
    증가 된 품질이 최대 품질보다 커지면, 최대 품질로 품질이 고정됩니다.
    강화가 끝나면, 플레이어는 사용한 아이템 1개를 잃습니다.

    Return
    ------
    없음.
     */

    private fun crafting(q: Double, maxQ: Int,
                         ironCount: Map<String, Int>, recipe: Recipe): ItemStack {
        val rankDown = ironCount.getValue("PoorPigIron") + ironCount.getValue("FinePigIron") // 선철을 썼을 경우 품질 하락
        val quality = max(0.0, q - rankDown) // 랭크 다운만큼 랭크 하락한 후 자체 품질

        // 강철 개수 카운트
        val countIron = ironCount.getValue("Iron")
        val countSteel = ironCount.getValue("Steel")
        val countBestSteel = ironCount.getValue("BestSteel")

        // 변수 설정
        val recipeResult = recipe.result
        val recipeResultType = recipeResult.type

        // 아이템 준비
        val result = recipeResult.clone()

        if (recipeResultType.maxDurability > 0) {
            // 내구도 설정
            val maxDurability = recipeResultType.maxDurability
            var durability = maxDurability.toDouble()
            durability *= GlobalObject.durabilityCoefficient.pow(maxQ - quality)
            setDurabilityForTools(result, maxDurability, durability)
        } // 내구도가 존재하는 아이템일 경우

        // 설명란 추가
        applyDescription(result, quality / recipeResult.amount,
                ceil(maxQ.toDouble() / recipe.result.amount).toInt())

        // 강철 태그
        if (countIron > 0) {
            when {
                countSteel == countIron -> {
                    when {
                        countBestSteel == countIron -> {
                            addSpecialty(result, false, specialty(SpecialtyTags.BEST_STEEL)) // 설명란이 추가 되어 있으므로 update 가 필요 없음
                        } // 사용한 철이 모두 강철+일 경우
                        countBestSteel > 0 -> {
                            addSpecialty(result, false, specialty(SpecialtyTags.CONTAINS_STEEL_PLUS))
                        } // 강철+가 최소 1개 이상 사용되었을 경우
                        else -> {
                            addSpecialty(result, false, specialty(SpecialtyTags.ALL_STEEL))
                        } // 강철+가 사용되지 않았을 경우
                    }
                } // 사용한 철이 모두 강철일 경우
                countSteel > 0 -> {
                    addSpecialty(result, false, specialty(SpecialtyTags.CONTAINS_STEEL))
                } // 강철이 최소 1개 이상 사용되었을 경우
                else -> {
                    addSpecialty(result, false, specialty(SpecialtyTags.NO_STEEL))
                } // 사용한 철이 모두 주철 / 선철인 경우
            }
        } // 철괴가 사용되지 않으면, 강철 태그가 붙지 않음.

        return result
    }

    private fun tagAfterCrafting(recipeResult: ItemStack, craftingLocation: Location): ItemStack {
        val block = craftingLocation.block

        // 정교함 태그
        if (block.type == Material.CRAFTING_TABLE) {
            val qualityString = getMarker(block)
            if (qualityString != null) {
                val quality = getQuality(qualityString)
                if (quality > 0.99) {
                    val random = Random.nextDouble()
                    if (random < GlobalObject.sophisticatedCraftingProbability) {
                        addSpecialty(recipeResult, false, specialty(SpecialtyTags.SOPHISTICATED))
                    } // 갓-챠
                } // 품질이 0.99보다 클 때 정교함 태그가 확률적으로 붙음.
            } // 품질이 설정되어 있지 않으면, 정교함 태그가 붙지 않음
        } // 작업대를 사용하지 않으면, 정교함 태그가 붙지 않음

        return recipeResult
    }

    private fun canExplodeWhenReinforce(blockUnderPlayer: Block, player: Player) : Double {
        val qualityString: String? = getMarker(blockUnderPlayer)
        val tableQuality: Double = qualityString?.let { getQuality(it) } ?: 0.5 // 마커가 없을 경우 0.5 (기본 NPC 마을 세팅 등)
        val random = Random.nextDouble()
        val base = GlobalObject.itemBreakBaseProbability
        if (random > (base + (1 - base) * tableQuality)) {
            eraseMarker(blockUnderPlayer)
            blockUnderPlayer.breakNaturally()
            player.location.createExplosion(
                    player, 5.0f, false, false
            )
        }
        return tableQuality
    }

    @EventHandler
    fun onBlockBreakingEvent(event: BlockBreakEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.

        // 변수 설정
        val player: Player = event.player
        val block: Block = event.block
        val blockGroup: BlockGroup = getBlockGroup(block.type)
        val tool: ItemStack? = player.inventory.itemInMainHand
        val toolGroup: ToolGroup = getToolGroup(tool?.type)

        // 맞는 도구를 사용했는지 판단
        when (blockGroup) {
            BlockGroup.WOOD -> {
                if (toolGroup != ToolGroup.AXE) {
                    player.damage(1.0)
                    if (!GlobalObject.isFundamentallySame(tool, GlobalObject.flake)) {
                        // 뗀석기를 사용하지 않았을 경우
                        event.isCancelled = true
                        return
                    } // 사용한 도구가 뗀석기도 아닐 경우
                    else {
                        // 사용한 도구가 뗀석기일 경우
                        val random: Double = Random.nextDouble()
                        if (random < GlobalObject.flakeBreakProbability) {
                            if (tool!!.amount == 1)
                                player.inventory.setItemInMainHand(null)
                            else {
                                tool.amount -= 1
                                player.inventory.setItemInMainHand(tool)
                            }
                        } // 뗀석기 파괴 갓-챠
                    }
                } // 도끼가 아닐 경우
            } // 나무 블록을 캐는데

            BlockGroup.STONE, BlockGroup.ORE, BlockGroup.CONCRETE,
            BlockGroup.TERRACOTTA -> {
                if (toolGroup != ToolGroup.PICKAXE) {
                    // 곡괭이가 아닐 경우
                    player.damage(2.0)
                    event.isCancelled = true
                    return
                }
            } // 돌, 광석, 테라코타를 캐는데

            BlockGroup.CROP -> {
                if (toolGroup != ToolGroup.HOE) {
                    // 괭이가 아닐 경우
                    event.isDropItems = false // 캘 수는 있지만, 아이템 드랍이 되지 않음.
                    return
                }
            } // 농작물을 수확하는데

            else -> {
            } // 그 외에는 없음 (when 형식 맞추기 위한 dummy)
        } // 틀린 도구를 사용했을 경우 return 됨.

        if (getMarkerEntity(block) != null) {
            val specialties : List<String> = tool?.let { getSpecialties(it) } ?: listOf()
            if (specialties.contains(specialtyToTag(SpecialtyTags.SOPHISTICATED))) {
                val qualityString = getMarker(block)!!
                val markedMaxQuality: Int = getMaxQuality(qualityString) // 최대 품질
                val markedQuality: Double = getQuality(qualityString) * markedMaxQuality // 자체 품질

                val appliedQuality: Double = max(0.0, markedQuality - 1.0) // 품질 1 하락

                val dropItem = block.drops.find {
                    it.type == block.type
                } // 원래 드랍 되려던 아이템 중 자기 자신과 같은 아이템
                dropItem?.apply {
                    applyDescription(dropItem, appliedQuality, markedMaxQuality)
                    event.isDropItems = false // 원래 드랍은 취소
                    player.world.dropItemNaturally(block.location, dropItem)
                } // 품질 적용 및 아이템 스폰
            } // 정교함이 붙은 도구를 사용하였을 경우, 품질 1이 감소하고, 아이템은 드랍 됨.
            else {
                event.isDropItems = false
            } // 정교함이 붙은 도구를 사용하지 않았을 경우, 아이템이 드랍되지 않음.

            eraseMarker(block) // 마커 지우기
        } // 마커가 있는 블록을 파괴할 경우

        if (tool == null) return // 맨손일 경우 아래 사항 적용 X

        // 특수

        if ((block.type == Material.COAL_ORE)
                and (getToolGroup(tool.type) == ToolGroup.PICKAXE)) {
            val random = Random.nextDouble()
            if (random < GlobalObject.cokesDropProbability) {
                val coke = GlobalObject.coke.clone()
                player.world.dropItemNaturally(event.block.location, coke)
            } // 코크스 갓-챠
        } // 코크스 자연 채광

        if (((block.type == Material.DIAMOND_ORE)
                        or (block.type == Material.EMERALD_ORE))
                and (tool.type == Material.IRON_PICKAXE)) {
            when (getSteelType(tool)) {
                null, SteelType.NONE -> {
                    event.isDropItems = false
                } // 철곡괭이가 아니거나 주철 / 선철 곡괭이면 드랍 X
                SteelType.CONTAINS_STEEL -> {
                    val random = Random.nextDouble()
                    if (random < GlobalObject.jewelBreakProbability) {
                        event.isDropItems = false
                    }
                } // 강철이 포함된 철곡괭이일 경우 20% 확률로 파괴 됨.
                SteelType.ALL_STEEL -> {} // 강철 곡괭이는 변화 없음.
                SteelType.CONTAINS_BEST_STEEL -> {
                    event.expToDrop *= 2
                } // 강철+ 곡괭이의 경우 경험치 2배 드랍
                SteelType.ALL_BEST_STEEL -> {
                    event.expToDrop *= 2
                    val random = Random.nextDouble()
                    if (random < GlobalObject.bestSteelPickaxeJewelProbability) {
                        player.world.dropItemNaturally(block.location, ItemStack(block.type))
                    }
                } // 강철++ 곡괭이의 경우 경험치 2배 + 30% 확률로 원석이 1개 추가로 드랍 됨.
            } // 철곡괭이의 강철 태그에 따라 결과가 달라짐.
        } // 철곡괭이로 다이아몬드 혹은 에메랄드 원석을 캘 때
    }

    @EventHandler
    fun onBlockFallingEvent(event: EntityChangeBlockEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val fallingBlock: Entity = event.entity
        val block: Material = event.to
        if ((fallingBlock.type == EntityType.FALLING_BLOCK) and (block == Material.GRAVEL)) {
            /*
            자갈을 떨어뜨리면 일정 확률로 뗀석기가 나온다.
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
        } else {
            val location = event.inventory.location ?: return // 작업 장소가 불특정할 경우를 제외한다.
            val inventoryResult = event.inventory.result ?: return // 조합 결과가 없을 경우를 제외한다.
            val result = tagAfterCrafting(inventoryResult, location)

            event.inventory.result = result
        }
    }

    @EventHandler
    fun onPrepareCraftingEvent(event: PrepareItemCraftEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val matrix = event.inventory.matrix
        val recipe: Recipe = event.recipe ?: return

        // 품질 계산
        var quality = 0.0
        var maxQuality = 0
        matrix.forEach {
            if (it != null) {
                val q = getQuality(it)
                val maxQ = getMaxQuality(it)
                quality += q * maxQ
                maxQuality += maxQ
            }
        }

        if (
                GlobalObject.isFundamentallySame(recipe.result, GlobalObject.poorSteel)
        ) {
            val converter = matrix.find {
                GlobalObject.isFundamentallySame(it, GlobalObject.standardConverter)
            }

            val iron = matrix.find {
                GlobalObject.isFundamentallySame(it, GlobalObject.poorPigIron)
            }

            if ((converter == null) or (iron == null)) {
                event.inventory.result = GlobalObject.banItem.clone()
                return
            } // 전로나 철괴가 조건을 충족하지 못한 경우
            else {
                val converterQuality = getQuality(converter!!)
                val ironQuality = getQuality(iron!!)

                when {
                    ironQuality < 0.5 ->
                        event.inventory.result = GlobalObject.banItem.clone()
                    converterQuality <= (10.0 / 12) ->
                        event.inventory.result = GlobalObject.poorSteel.clone()
                    else ->
                        event.inventory.result = GlobalObject.fineSteel.clone()
                }
                return
            }
        } // 조합 결과가 강철일 경우 → 강철은 다른 아이템과 다르므로, 바로 return

        if (recipe.result.type == Material.BLAST_FURNACE) {
            var isAbleToCraft = true // true 일 때 조합 가능
            matrix.forEach {
                if (it == null) isAbleToCraft = false // 빈 공간이 있을 경우 제작 불가

                if (it.type == Material.IRON_INGOT) {
                    if (!GlobalObject.isFundamentallySame(it, GlobalObject.poorSteel)) {
                        isAbleToCraft = false
                    }
                } // 사용한 철괴가 강철이 아닐 경우 제작 불가

                if (it.type == Material.COAL) {
                    if (!GlobalObject.isFundamentallySame(it, GlobalObject.coke)) {
                        isAbleToCraft = false
                    }
                } // 사용한 석탄이 코크스가 아닐 경우 제작 불가
            } // 조합 조건 대조

            if (!isAbleToCraft) {
                event.inventory.result = GlobalObject.banItem.clone()
                return
            } // 조합 불가능
        } // 조합 결과가 용광로일 경우

        if (recipe.result.type == Material.ANVIL) {
            var isAbleToCraft = true // true 일 때 조합 가능
            matrix.forEach {
                if (it != null) {
                    if (it.type == Material.IRON_INGOT) {
                        if (!GlobalObject.isFundamentallySame(it, GlobalObject.poorSteel) and
                                !GlobalObject.isFundamentallySame(it, GlobalObject.poorCastIron)) {
                            isAbleToCraft = false
                        }
                    } // 사용한 철괴가 강철 또는 주철이 아닐 경우 제작 불가
                    if (it.type == Material.IRON_BLOCK) {
                        val q = getQuality(it)
                        val maxQ = getMaxQuality(it)
                        if (maxQ == GlobalObject.defaultMaxQuality) {
                            isAbleToCraft = false
                        } // 자연에서 얻은 철블록일 경우, 제작 불가

                        if (q < 0.8) {
                            isAbleToCraft = false
                        } // 품질이 0.8 이하일 경우, 제작 불가
                    }
                }
            } // 조합 조건 대조

            if (!isAbleToCraft) {
                event.inventory.result = GlobalObject.banItem.clone()
                return
            } // 조합 불가능
        } // 조합 결과가 모루일 경우

        val ironCount = countIron(matrix)
        val result = crafting(quality, maxQuality, ironCount, recipe)

        event.inventory.result = result
    }

    @EventHandler
    fun onSmelting(event: FurnaceExtractEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        if (event.itemType == Material.IRON_INGOT) {
            val player: Player = event.player
            val itemInMainHand: ItemStack = player.inventory.itemInMainHand
            val itemInOffHand: ItemStack = player.inventory.itemInOffHand
            val mainHandCount =
                    if (GlobalObject.isFundamentallySame(itemInMainHand, GlobalObject.coke)) {
                        itemInMainHand.amount
                    } else {
                        0
                    } // 많이 사용하는 손에 있는 코크스 개수
            val offHandCount =
                    if (GlobalObject.isFundamentallySame(itemInOffHand, GlobalObject.coke)) {
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

    @EventHandler
    fun onReinforcing(event: PlayerDropItemEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.

        val itemDrop: Item = event.itemDrop
        val droppedItem: ItemStack = itemDrop.itemStack // 떨어뜨린 아이템
        val dropLocation: Location = itemDrop.location // 떨어뜨린 위치
        val dropWorld = dropLocation.world
        val dropPlayer = event.player
        val blockUnderPlayer: Block = dropPlayer.location.let {
            val x = it.x
            val y = it.y
            val z = it.z
            val world = it.world

            val newLocation = Location(world, x, y - 0.5, z)
            newLocation.block
        } // 떨어뜨린 위치 아래에 깔린 블록
        var random: Double

        if (
                GlobalObject.isFundamentallySame(droppedItem, GlobalObject.poorSteel) and
                (getBlockGroup(blockUnderPlayer.type) == BlockGroup.ANVIL)
        ) {
            // 단조 작업을 시작할 때, 모루의 품질에 따라 모루가 터질 수도 있음.
            canExplodeWhenReinforce(blockUnderPlayer, event.player)

            event.player.sendActionBar("단조 시작")

            enabledBlocks.add(blockUnderPlayer)
            val forgingTicks = GlobalObject.forgingTicks * droppedItem.amount

            itemDrop.teleport(dropPlayer.location)
            itemDrop.velocity = Vector(0, 0, 0)
            itemDrop.pickupDelay = forgingTicks + 5 // 5틱은 Padding
            itemDrop.setWillAge(false)

            val forgingSpark =
                    Runnable {
                        val randomVolume = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.2f + 0.5f
                            when {
                                tmp > 1 -> 1.0f
                                tmp < 0 -> 0.0f
                                else -> tmp
                            }
                        } // 정규 분포로 볼륨 설정
                        val randomPitch = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.3f + 1.0f
                            when {
                                tmp > 2 -> 2.0f
                                tmp < 0.5 -> 0.5f
                                else -> tmp
                            }
                        } // 정규 분포로 볼륨 설정
                        dropWorld.playSound(dropLocation, Sound.BLOCK_ANVIL_USE,
                                randomVolume, randomPitch) // 모루질 소리

                        dropWorld.spawnParticle(Particle.CRIT, itemDrop.location, 10)
                        dropPlayer.sendActionBar("단조 작업 중")
                    } // 모루질
            val finishForging =
                    Runnable {
                        val quality = getQuality(droppedItem)
                        val maxQuality = getMaxQuality(droppedItem)

                        val forgedQuality = min(quality * maxQuality + 0.3, maxQuality.toDouble())
                        val specialties = getSpecialties(droppedItem)
                        applyDescription(droppedItem, forgedQuality, maxQuality, specialties)
                        enabledBlocks.remove(blockUnderPlayer)

                        // 폭죽
                        val finishEffect = FireworkEffect.builder().apply {
                            trail(false)
                            flicker(false)
                            withColor(Color.GREEN, Color.LIME, Color.WHITE, Color.YELLOW)
                            with(FireworkEffect.Type.BALL)

                        }.build()
                        dropWorld.playSound(dropLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.0f)
                        dropWorld.playFirework(itemDrop.location, finishEffect, 10.0)
                        dropPlayer.sendActionBar("단조 작업 완료!")
                    } // 강철 품질 업
            Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, forgingTicks.toLong())
            for (i in 1 until forgingTicks / 60) {
                Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
            }
        } // 모루 위에 강철을 던졌을 때 → 단조

        if (blockUnderPlayer.type == Material.SMITHING_TABLE) {
            // 강화 작업을 시작할 때, 대장장이 작업대의 품질에 따라 터질 수도 있음.
            if ((droppedItem.type == Material.IRON_INGOT) or (droppedItem.type.name.contains("DIAMOND"))) return // 다이아몬드 제품 불가
            val tableQuality = canExplodeWhenReinforce(blockUnderPlayer, event.player)

            val bookShelfArray: ArrayList<Block> = blockUnderPlayer.location.let {
                val x = it.x
                val y = it.y - 1
                val z = it.z
                val world = it.world

                val bookShelves = ArrayList<Block>()
                for (xIdx in -1..1) {
                    for (zIdx in -1..1) {
                        if ((xIdx != 0) or (zIdx != 0)) {
                            // xIdx 와 zIdx 가 모두 0이 아닐 경우 (바로 아래 칸이 아니면)
                            val checkLocation = Location(world, x + xIdx, y, z + zIdx)
                            if (checkLocation.block.type == Material.BOOKSHELF) {
                                bookShelves.add(checkLocation.block)
                            }
                        }
                    }
                }
                bookShelves
            } // 대장장이 작업대 아래 책장들 (바로 아래 제외)
            event.player.sendActionBar("강화 시작")

            // 파괴 불가능 블록 추가
            enabledBlocks.add(blockUnderPlayer)
            for (bookShelf in bookShelfArray) {
                enabledBlocks.add(bookShelf)
            }

            val reinforcingTicks = GlobalObject.reinforcingTicks * droppedItem.amount

            itemDrop.teleport(dropPlayer.location)
            itemDrop.velocity = Vector(0, 0, 0)
            itemDrop.pickupDelay = reinforcingTicks + 5 // 5틱은 Padding
            itemDrop.setWillAge(false)

            val forgingSpark =
                    Runnable {
                        val randomVolume = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.2f + 0.5f
                            when {
                                tmp > 1 -> 1.0f
                                tmp < 0 -> 0.0f
                                else -> tmp
                            }
                        }
                        val randomPitch = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.3f + 1.0f
                            when {
                                tmp > 2 -> 2.0f
                                tmp < 0.5 -> 0.5f
                                else -> tmp
                            }
                        }
                        dropWorld.playSound(dropLocation, Sound.BLOCK_ANVIL_USE,
                                randomVolume, randomPitch) // 모루질 소리

                        dropWorld.spawnParticle(Particle.DRAGON_BREATH, itemDrop.location, 10)
                        dropPlayer.sendActionBar("강화 작업 중")
                    } // 강화
            val finishForging =
                    Runnable {
                        // 책장의 품질 합계 계산
                        var totalBookShelfQuality = 0.0
                        for (bookShelf in bookShelfArray) {
                            val bookShelfQualityString: String? = getMarker(bookShelf)
                            val bookShelfQuality: Double = bookShelfQualityString?.let {
                                getQuality(bookShelfQualityString)
                            } ?: 0.5
                            totalBookShelfQuality += bookShelfQuality
                        }

                        val coefficient: Double = totalBookShelfQuality + tableQuality * 2 // 총 감률 계수

                        val failureProbability = GlobalObject.reinForceBaseFailProbability -
                                GlobalObject.reinForceFailProbabilityDecrease * coefficient // 실패 확률
                        val breakProbability = GlobalObject.reinForceBaseBreakProbability -
                                GlobalObject.reinForceBreakProbabilityDecrease * coefficient // 파괴 확률

                        // 파괴 가능 블록으로 복귀
                        enabledBlocks.remove(blockUnderPlayer)
                        for (bookShelf in bookShelfArray) {
                            enabledBlocks.remove(bookShelf)
                        }

                        random = Random.nextDouble()

                        when {
                            (random < breakProbability) -> {
                                dropWorld.playSound(itemDrop.location, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f)
                                itemDrop.remove()
                                dropPlayer.sendActionBar("${ChatColor.RED}아이템 파괴(⊙_⊙;)")
                            } // 파괴

                            ((breakProbability <= random) and
                                    (random < (breakProbability + failureProbability))) -> {
                                dropWorld.playSound(itemDrop.location, Sound.BLOCK_BELL_USE, 1.0f, 1.0f)
                                dropPlayer.sendActionBar("${ChatColor.RED}강화 실패")
                            } // 강화 실패

                            else -> {
                                val quality = getQuality(droppedItem)
                                val maxQuality = getMaxQuality(droppedItem)

                                val reinforcedQuality = min(quality * maxQuality + 0.5, maxQuality.toDouble())
                                val specialties = getSpecialties(droppedItem)
                                applyDescription(droppedItem, reinforcedQuality, maxQuality, specialties)

                                // 폭죽
                                val finishEffect = FireworkEffect.builder().apply {
                                    trail(false)
                                    flicker(false)
                                    withColor(Color.GREEN, Color.LIME, Color.WHITE, Color.YELLOW)
                                    with(FireworkEffect.Type.BALL)
                                }.build()
                                dropWorld.playSound(dropLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.0f)
                                dropWorld.playFirework(itemDrop.location, finishEffect, 10.0)
                                dropPlayer.sendActionBar("강화 작업 완료!")
                            } // 성공
                        }
                    } // 품질 상승
            Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, reinforcingTicks.toLong())
            for (i in 1 until reinforcingTicks / 60) {
                Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
            }
        } // 어떤 물건이든 대장장이 작업대 위에 올렸을 때 → 강화 (단, 철, 다이아몬드 제외)

        if (blockUnderPlayer.type == Material.ENCHANTING_TABLE) {
            // 세공 혹은 태그를 붙일 때 인챈트 테이블의 품질에 따라 테이블이 터질 수도 있음.
            val tableQuality = canExplodeWhenReinforce(blockUnderPlayer, event.player)
            if (droppedItem.type == Material.BOOK) {
                TODO("Not yet Implemented")
            } // 태그 붙이기
            else {
                val bookShelfArray: ArrayList<Block> = blockUnderPlayer.location.let {
                    val x = it.x
                    val y = it.y
                    val z = it.z
                    val world = it.world

                    val bookShelves = ArrayList<Block>()
                    for (xIdx in -2..2) {
                        for (zIdx in -2..2) {
                            if ((abs(xIdx) == 2) or (abs(zIdx) == 2)) {
                                // xIdx 와 zIdx 중 하나가 2일 경우 (일반적인 인챈트)
                                val checkLocation = Location(world, x + xIdx, y, z + zIdx)
                                if (checkLocation.block.type == Material.BOOKSHELF) {
                                    bookShelves.add(checkLocation.block)
                                }
                            }
                        }
                    }
                    bookShelves
                }
                event.player.sendActionBar("세공 시작")

                // 파괴 불가능 블록 추가
                enabledBlocks.add(blockUnderPlayer)
                for (bookShelf in bookShelfArray) {
                    enabledBlocks.add(bookShelf)
                }

                val reinforcingTicks = GlobalObject.reinforcingTicks * droppedItem.amount

                itemDrop.teleport(dropPlayer.location)
                itemDrop.velocity = Vector(0, 0, 0)
                itemDrop.pickupDelay = reinforcingTicks + 5 // 5틱은 Padding
                itemDrop.setWillAge(false)

                val forgingSpark =
                        Runnable {
                            val randomVolume = Random.asJavaRandom().nextGaussian().toFloat().let {
                                val tmp = it * 0.2f + 0.5f
                                when {
                                    tmp > 1 -> 1.0f
                                    tmp < 0 -> 0.0f
                                    else -> tmp
                                }
                            }
                            val randomPitch = Random.asJavaRandom().nextGaussian().toFloat().let {
                                val tmp = it * 0.3f + 1.0f
                                when {
                                    tmp > 2 -> 2.0f
                                    tmp < 0.5 -> 0.5f
                                    else -> tmp
                                }
                            }
                            dropWorld.playSound(dropLocation, Sound.BLOCK_ANVIL_USE,
                                    randomVolume, randomPitch) // 모루질 소리

                            dropWorld.spawnParticle(Particle.ENCHANTMENT_TABLE, itemDrop.location, 10)
                            dropPlayer.sendActionBar("세공 작업 중")
                        } // 강화
                val finishForging =
                        Runnable {
                            // 책장의 품질 합계 계산
                            var totalBookShelfQuality = 0.0
                            for (bookShelf in bookShelfArray) {
                                val bookShelfQualityString: String? = getMarker(bookShelf)
                                val bookShelfQuality: Double = bookShelfQualityString?.let {
                                    getQuality(bookShelfQualityString)
                                } ?: 0.5
                                totalBookShelfQuality += bookShelfQuality
                            }

                            val coefficient: Double = totalBookShelfQuality + tableQuality * 4 // 총 감률 계수

                            val failureProbability = GlobalObject.reinForceBaseFailProbability -
                                    GlobalObject.handworkFailProbabilityDecrease * coefficient // 실패 확률
                            val breakProbability = GlobalObject.reinForceBaseBreakProbability -
                                    GlobalObject.handworkBreakProbabilityDecrease * coefficient // 파괴 확률

                            // 파괴 가능 블록으로 복귀
                            enabledBlocks.remove(blockUnderPlayer)
                            for (bookShelf in bookShelfArray) {
                                enabledBlocks.remove(bookShelf)
                            }

                            random = Random.nextDouble()

                            when {
                                (random < breakProbability) -> {
                                    dropWorld.playSound(itemDrop.location, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f)
                                    itemDrop.remove()
                                    dropPlayer.sendActionBar("${ChatColor.RED}아이템 파괴(⊙_⊙;)")
                                } // 파괴

                                ((breakProbability <= random) and
                                        (random < (breakProbability + failureProbability))) -> {
                                    dropWorld.playSound(itemDrop.location, Sound.BLOCK_BELL_USE, 1.0f, 1.0f)
                                    dropPlayer.sendActionBar("${ChatColor.RED}강화 실패")
                                } // 강화 실패

                                else -> {
                                    val quality = getQuality(droppedItem)
                                    val maxQuality = getMaxQuality(droppedItem)

                                    val reinforcedQuality = min(quality * maxQuality + 1.5, maxQuality.toDouble())
                                    val specialties = getSpecialties(droppedItem)
                                    applyDescription(droppedItem, reinforcedQuality, maxQuality, specialties)

                                    // 폭죽
                                    val finishEffect = FireworkEffect.builder().apply {
                                        trail(false)
                                        flicker(false)
                                        withColor(Color.GREEN, Color.LIME, Color.WHITE, Color.YELLOW)
                                        with(FireworkEffect.Type.BALL)
                                    }.build()
                                    dropWorld.playSound(dropLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.0f)
                                    dropWorld.playFirework(itemDrop.location, finishEffect, 10.0)
                                    dropPlayer.sendActionBar("세공 작업 완료!")
                                } // 성공
                            }
                        } // 품질 상승
                Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, reinforcingTicks.toLong())
                for (i in 1 until reinforcingTicks / 60) {
                    Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
                }
            } // 다이아몬드 -> 세공
        } // 인챈트 테이블 위에서 다이아몬드를 던졌을 때 → 세공 / 태그 붙이기

        if (blockUnderPlayer.type == Material.CRAFTING_TABLE) {
            // 재가공할 때 테이블 터질 수 있음.
            val toolGroup = getToolGroup(droppedItem.type)
            if ((getItemGroup(droppedItem.type) != ItemGroup.TOOL) or
                    (toolGroup == ToolGroup.PICKAXE) or
                    (toolGroup == ToolGroup.BUCKET) or
                    (toolGroup == ToolGroup.HOE) or
                    (toolGroup == ToolGroup.SHOVEL) or
                    (toolGroup == ToolGroup.RANGED) or
                    (toolGroup == ToolGroup.PROTECT) or
                    (getSpecialties(droppedItem).contains(specialty(SpecialtyTags.REFORMED)))) return // 도구만 가능, 재가공 제품은 불가
            canExplodeWhenReinforce(blockUnderPlayer, event.player)

            event.player.sendActionBar("재가공 시작")

            enabledBlocks.add(blockUnderPlayer)
            val forgingTicks = GlobalObject.forgingTicks * droppedItem.amount

            itemDrop.teleport(dropPlayer.location)
            itemDrop.velocity = Vector(0, 0, 0)
            itemDrop.pickupDelay = forgingTicks + 5 // 5틱은 Padding
            itemDrop.setWillAge(false)

            val forgingSpark =
                    Runnable {
                        val randomVolume = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.2f + 0.5f
                            when {
                                tmp > 1 -> 1.0f
                                tmp < 0 -> 0.0f
                                else -> tmp
                            }
                        } // 정규 분포로 볼륨 설정
                        val randomPitch = Random.asJavaRandom().nextGaussian().toFloat().let {
                            val tmp = it * 0.3f + 1.0f
                            when {
                                tmp > 2 -> 2.0f
                                tmp < 0.5 -> 0.5f
                                else -> tmp
                            }
                        } // 정규 분포로 볼륨 설정
                        dropWorld.playSound(dropLocation, Sound.BLOCK_ANVIL_USE,
                                randomVolume, randomPitch) // 모루질 소리

                        dropWorld.spawnParticle(Particle.CRIT, itemDrop.location, 10)
                        dropPlayer.sendActionBar("재가공 작업 중")
                    } // 모루질
            val finishForging =
                    Runnable {
                        val quality = getQuality(droppedItem)

                        when (toolGroup) {
                            ToolGroup.AXE -> {
                                val damage = axeDamage(droppedItem.type)
                                val speed = axeSpeed(droppedItem.type)
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ATTACK_DAMAGE, "QAxeDamage",
                                        0.75 * damage + 1.25 * quality * damage, EquipmentSlot.HAND
                                )
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ATTACK_SPEED, "QAxeSpeed",
                                        speed, EquipmentSlot.HAND
                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.SWORD -> {
                                val damage = swordDamgage(droppedItem.type)
                                val speed = swordSpeed(droppedItem.type)
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ATTACK_DAMAGE, "QSwordDamage",
                                        0.75 * damage + 1.25 * quality * damage, EquipmentSlot.HAND
                                )
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ATTACK_SPEED, "QSwordSpeed",
                                        speed, EquipmentSlot.HAND
                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.FISHING_ROD -> {
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_LUCK, "QFishingRodLuck",
                                        -2 + 5 * quality, EquipmentSlot.HAND
                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.HELMET -> {
                                val armor = helmetArmor(droppedItem.type)
                                if (droppedItem.type == Material.DIAMOND_HELMET){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QHelmetToughness",
                                            2.0, EquipmentSlot.HEAD
                                    )
                                }
                                else if (droppedItem.type == Material.NETHERITE_HELMET){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QHelmetToughness",
                                            3.0, EquipmentSlot.HEAD
                                    )
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_KNOCKBACK_RESISTANCE, "QHelmetResist",
                                            1.0, EquipmentSlot.HEAD
                                    )
                                }
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ARMOR, "QHelmetArmor",
                                        0.75 * armor + 1.25 * quality * armor, EquipmentSlot.HEAD

                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.CHEST_PLATE -> {
                                val armor = chestArmor(droppedItem.type)
                                if (droppedItem.type == Material.DIAMOND_CHESTPLATE){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QChestToughness",
                                            2.0, EquipmentSlot.CHEST
                                    )
                                }
                                else if (droppedItem.type == Material.NETHERITE_CHESTPLATE){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QChestToughness",
                                            3.0, EquipmentSlot.CHEST
                                    )
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_KNOCKBACK_RESISTANCE, "QChestResist",
                                            1.0, EquipmentSlot.CHEST
                                    )
                                }
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ARMOR, "QChestArmor",
                                        0.75 * armor + 1.25 * quality * armor, EquipmentSlot.HEAD

                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.LEGGINGS -> {
                                val armor = leggingsArmor(droppedItem.type)
                                if (droppedItem.type == Material.DIAMOND_LEGGINGS){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QLeggingsToughness",
                                            2.0, EquipmentSlot.LEGS
                                    )
                                }
                                else if (droppedItem.type == Material.NETHERITE_LEGGINGS){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QLeggingsToughness",
                                            3.0, EquipmentSlot.LEGS
                                    )
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_KNOCKBACK_RESISTANCE, "QLeggingsResist",
                                            1.0, EquipmentSlot.LEGS
                                    )
                                }
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ARMOR, "QLeggingsArmor",
                                        0.75 * armor + 1.25 * quality * armor, EquipmentSlot.LEGS

                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            ToolGroup.BOOTS -> {
                                val armor = bootsArmor(droppedItem.type)
                                if (droppedItem.type == Material.DIAMOND_BOOTS){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QBootsToughness",
                                            2.0, EquipmentSlot.FEET
                                    )
                                }
                                else if (droppedItem.type == Material.NETHERITE_BOOTS){
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_ARMOR_TOUGHNESS, "QBootsToughness",
                                            3.0, EquipmentSlot.FEET
                                    )
                                    addAttribute(
                                            droppedItem, Attribute.GENERIC_KNOCKBACK_RESISTANCE, "QBootsResist",
                                            1.0, EquipmentSlot.FEET
                                    )
                                }
                                addAttribute(
                                        droppedItem, Attribute.GENERIC_ARMOR, "QHelmetArmor",
                                        0.75 * armor + 1.25 * quality * armor, EquipmentSlot.FEET

                                )
                                addSpecialty(droppedItem, false, specialty(SpecialtyTags.REFORMED))
                            }
                            else -> {}
                        }
                        enabledBlocks.remove(blockUnderPlayer)

                        // 폭죽
                        val finishEffect = FireworkEffect.builder().apply {
                            trail(false)
                            flicker(false)
                            withColor(Color.GREEN, Color.LIME, Color.WHITE, Color.YELLOW)
                            with(FireworkEffect.Type.BALL)

                        }.build()
                        dropWorld.playSound(dropLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.0f)
                        dropWorld.playFirework(itemDrop.location, finishEffect, 10.0)
                        dropPlayer.sendActionBar("재가공 작업 완료!")
                    } // 강철 품질 업
            Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, forgingTicks.toLong())
            for (i in 1 until forgingTicks / 60) {
                Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
            }
        } // 작업대 위에서 도구를 던졌을 때 → 재가공
    }

    @EventHandler
    fun onEnableBlockDamaging(event: BlockDamageEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.

        val block = event.block
        if (enabledBlocks.contains(block)) { // 부술 수 없는 블록은 때릴 수 없다.
            event.player.sendActionBar("작업 중에는 부수거나 수리할 수 없다")
            event.isCancelled = true
        } else if (isAbleToReinforce(block)) { // 강화할 수 있는 블록들을 때릴 때
            when (block.type) {
                Material.CRAFTING_TABLE -> {
                    reinforceTables(block, event.player, "PLANKS") // 판자를 통해 강화 가능
                } // 작업대 강화

                Material.SMITHING_TABLE -> {
                    reinforceTables(block, event.player, "SMOOTH_STONE") // 매끄러운 돌을 통해 강화 가능
                } // 대장장이 작업대 강화

                Material.ANVIL -> {
                    reinforceTables(block, event.player, "INGOT") // 괴 (철, 금, 네더라이트)를 통해 강화 가능
                } // 모루 강화

                Material.ENCHANTING_TABLE -> {
                    reinforceTables(block, event.player, "LAPIS_LAZULI") // 청금석으로 강화 가능
                } // 인챈트 테이블 강화

                Material.FURNACE -> {
                    reinforceTables(block, event.player, "COAL") // 석탄, 목탄, 석탄 블록 등으로 강화
                } // 화로 강화

                Material.BLAST_FURNACE -> {
                    reinforceTables(block, event.player, "COAL") // 석탄, 목탄, 석탄 블록 등으로 강화
                } // 용광로 강화

                Material.SMOKER -> {
                    reinforceTables(block, event.player, "COAL") // 석탄, 목탄, 석탄 블록 등으로 강화
                } // 훈연기 강화

                Material.TORCH -> {
                    reinforceTables(block, event.player, "COAL") // 석탄, 목탄, 석탄 블록
                } // 횃불 강화

                Material.REDSTONE_TORCH -> {
                    reinforceTables(block, event.player, "REDSTONE")
                } // 레드스톤 횃불 강화

                Material.CHEST -> {
                    reinforceTables(block, event.player, "PLANKS")
                } // 상자 강화

                Material.BOOKSHELF -> {
                    reinforceTables(block, event.player, "BOOK")
                }

                else -> {
                }
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!GlobalObject.isOn) return

        if (event.clickedBlock != null) {
            val clickedBlock = event.clickedBlock!!
            if (enabledBlocks.contains(clickedBlock)) {
                event.player.sendActionBar("작업 중에는 사용할 수 없다")
                event.isCancelled = true
                return
            } // 작업 중인 블록을 사용했을 경우
            else if (getItemGroup(clickedBlock.type) == ItemGroup.INTERACTIVE_BLOCK) {
                if ((event.action == Action.LEFT_CLICK_BLOCK) and
                        isAbleToReinforce(clickedBlock)) return // 강화 작업 중에는 터지지 않는다.

                val qualityString: String? = getMarker(clickedBlock)
                val quality: Double = qualityString?.let { getQuality(it) } ?: 0.5 // 마커가 없을 경우 0.5 (기본 NPC 마을 세팅 등)
                val random = Random.nextDouble()
                val base = GlobalObject.itemBreakBaseProbability
                if (random > (base + (1 - base) * quality)) {
                    eraseMarker(clickedBlock)
                    clickedBlock.breakNaturally()
                    event.player.location.createExplosion(
                            event.player, 5.0f, false, false
                    )
                }
            } // 상호작용할 수 있는 블록일 경우
        }

        val hand = event.hand ?: return
        val itemOnHand = event.item ?: return
        val itemGroup: ItemGroup = getItemGroup(itemOnHand.type)

        if ((itemGroup == ItemGroup.TOOL) or (itemGroup == ItemGroup.FOOD)) return // 손에 들고 있는 아이템이 도구 혹은 음식인 경우, 깨지지 않음.

        val quality = getQuality(itemOnHand)
        val base = GlobalObject.itemBreakBaseProbability
        val random = Random.nextDouble()
        if (random > (base + (1 - base) * quality)) {
            var superSave = false
            event.player.inventory.forEach {
                if (it?.type == Material.CHEST) {
                    val chestQuality = getQuality(it)
                    val chestRandom = Random.nextDouble()
                    if (chestRandom < chestQuality/2) { // Quality / 2의 확률로
                        superSave = true
                    }
                }
            }
            if (!superSave) {
                event.player.world.playSound(event.player.location, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f)
                event.player.sendActionBar("${ChatColor.RED}아이템이 깨져 버렸다.")
                itemOnHand.amount -= 1
                event.player.inventory.setItem(hand, itemOnHand.clone())
                event.isCancelled = true
            } else {
                event.player.world.playSound(event.player.location, Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.0f)
                event.player.sendActionBar("${ChatColor.GREEN}상자 슈퍼 세이브")
                event.player.world.spawnParticle(Particle.TOTEM, event.player.location, 15)
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.

        val block = event.block
        val location = block.location
        val player = event.player
        val blockAsItemStack: ItemStack = player.inventory.let {
            val mainHand = it.itemInMainHand
            val offHand = it.itemInOffHand
            if (mainHand.type.isBlock) { // 주요 손에 있는 아이템이 블록일 경우
                mainHand
            } else { // 아닐 경우
                offHand
            }
        } // 설치하는 블록을 아이템 스택으로
        val quality = getQuality(blockAsItemStack)
        val maxQuality = getMaxQuality(blockAsItemStack)
        val itemGroup = getItemGroup(blockAsItemStack.type)

        if ((itemGroup == ItemGroup.INTERACTIVE_BLOCK) or (itemGroup == ItemGroup.UPGRADE_BLOCK)) {
            writeMarker(location, quality, maxQuality)
        }
        else if (itemGroup == ItemGroup.TORCH) {
            val breakTorch = Runnable {
                block.breakNaturally()
                location.getNearbyEntitiesByType(Item::class.java, 0.5).forEach{
                    it.remove()
                }
            }
            val burningTime = GlobalObject.torchBurningTicks + (quality * 24000).toLong()
            Bukkit.getScheduler().runTaskLater(plugin!!, breakTorch, burningTime)
        } // 횃불 꺼짐
    }

    @EventHandler
    fun onBlockFade(event: BlockFadeEvent) {
        if (!GlobalObject.isOn) return

        eraseMarker(event.block)
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        if (!GlobalObject.isOn) return

        event.blockList().forEach {
            eraseMarker(it)
        }
    }

    @EventHandler
    fun onBlockExplodeByEntity(event: EntityExplodeEvent) {
        if (!GlobalObject.isOn) return

        event.blockList().forEach {
            eraseMarker(it)
        }
    }
}