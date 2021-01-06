package com.github.bucket1572.clumsycrafting

import com.github.noonmaru.tap.effect.playFirework
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.random.asJavaRandom

class EventListener : Listener {
    var plugin: JavaPlugin? = null
    private val enabledBlocks = ArrayList<Block>()

    @EventHandler
    fun onBlockBreakingEvent(event: BlockBreakEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.
        val player: Player = event.player

        val block: Block = event.block
        val blockGroup: BlockGroup = getBlockGroup(block.type)

        val tool: ItemStack? = player.inventory.itemInMainHand
        val toolGroup: ToolGroup = getToolGroup(tool?.type)

        // 맞는 도구를 사용했는지 판단
        when (blockGroup) {
            BlockGroup.WOOD -> {
                if (toolGroup != ToolGroup.AXE) {
                    //나무 블록을 캐는데 도끼가 아닐 경우
                    player.damage(1.0)
                    if (!GlobalObject.isFundamentallySame(tool, GlobalObject.flake)) {
                        // 뗀석기를 사용하지 않았을 경우
                        event.isCancelled = true
                        return
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
                }
            }

            BlockGroup.STONE, BlockGroup.ORE, BlockGroup.CONCRETE,
            BlockGroup.TERRACOTTA -> {
                if (toolGroup != ToolGroup.PICKAXE) {
                    // 돌, 광석, 테라코타를 캐는데 곡괭이가 아닐 경우
                    player.damage(2.0)
                    event.isCancelled = true
                    return
                }
            }

            BlockGroup.CROP -> {
                if (toolGroup != ToolGroup.HOE) {
                    // 농작물을 수확하는데 괭이가 아닐 경우
                    event.isDropItems = false // 캘 수는 있지만, 아이템 드랍이 되지 않음.
                    return
                }
            }

            else -> {}
        }

        if (tool == null) return // 맨손일 경우 아래 사항 적용 X

        // 특수
        // 코크스 자연 채광
        if ((block.type == Material.COAL_ORE)
                and (getToolGroup(tool.type) == ToolGroup.PICKAXE)) {
            val random = Random.nextDouble()
            if (random < GlobalObject.cokesDropProbability) {
                val coke = GlobalObject.coke.clone()
                player.world.dropItemNaturally(event.block.location, coke)
            }
        }

        if (((block.type == Material.DIAMOND_ORE)
                        or (block.type == Material.EMERALD_ORE))
                and (tool.type == Material.IRON_PICKAXE)) {
            // 철곡괭이로 다이아몬드 혹은 에메랄드 원석을 캘 때
            if (!isSteelTool(tool)) {
                event.isDropItems = false
            } else if (!isPerfectSteelTool(tool)) {
                val random = Random.nextDouble()
                if (random < GlobalObject.jewelBreakProbability) {
                    event.isDropItems = false
                }
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

        if (matrix.size == 9) { // 작업대
            val ironCount = countIron(matrix) // 조합에 들어간 철괴 개수
            when {
                (ironCount["Iron"]!! > 0) // 철이 사용 됨.
                        and (recipe.result.type.maxDurability > 0) -> {
                    // 선철 썼을 경우 퀄리티 하락
                    val rankDown = ironCount["PoorPigIron"]!! + ironCount["FinePigIron"]!! // 선철을 썼을 경우 품질 하락
                    quality = max(0.0, quality - rankDown)

                    // 강철 개수 카운트
                    val steelCount = ironCount["Steel"]!!
                    val bestSteelCount = ironCount["BestSteel"]!!

                    // 내구도 설정
                    val result = recipe.result.clone()
                    val maxDurability = result.type.maxDurability
                    var durability = maxDurability.toDouble()
                    durability *= GlobalObject.durabilityCoefficient.pow(maxQuality - quality)
                    setDurabilityForTools(result, maxDurability, durability)

                    // 강철 특수
                    when {
                        steelCount == ironCount["Iron"]!! -> {
                            // 모두 강철을 사용하였을 때 Best Steel 특수
                            when {
                                bestSteelCount == ironCount["Iron"]!! ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.ALL_BEST_STEEL))
                                bestSteelCount > 0 ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.CONTAINS_BEST_STEEL))
                                else ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.ALL_STEEL))
                            }
                        }
                        steelCount > 0 -> {
                            applyDescription(result, quality / recipe.result.amount,
                                    ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.CONTAINS_STEEL))
                        }
                        else -> {
                            applyDescription(result, quality / recipe.result.amount,
                                    ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.NONE))
                        }
                    }
                    event.inventory.result = result
                    return
                }
                ironCount["Iron"]!! > 0 -> {
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
                            event.inventory.result = null
                        } else {
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
                        }
                        return
                    } // 조합 결과가 강철일 경우

                    if (recipe.result.type == Material.BLAST_FURNACE) {
                        var craftable = true
                        matrix.forEach {
                            if (it == null) craftable = false // 빈 공간이 있을 경우 제작 불가

                            if (it.type == Material.IRON_INGOT) {
                                if (!GlobalObject.isFundamentallySame(it, GlobalObject.poorSteel)) {
                                    craftable = false
                                }
                            } // 사용한 철괴가 강철이 아닐 경우 제작 불가

                            if (it.type == Material.COAL) {
                                if (!GlobalObject.isFundamentallySame(it, GlobalObject.coke)) {
                                    craftable = false
                                }
                            } // 사용한 석탄이 코크스가 아닐 경우 제작 불가
                        }

                        if (!craftable) {
                            event.inventory.result = GlobalObject.banItem.clone()
                            return
                        }
                    } // 조합 결과가 용광로일 경우

                    if (recipe.result.type == Material.ANVIL) {
                        var craftable = true
                        matrix.forEach {
                            if (it != null) {
                                if (it.type == Material.IRON_INGOT) {
                                    if (!GlobalObject.isFundamentallySame(it, GlobalObject.poorSteel) and
                                            !GlobalObject.isFundamentallySame(it, GlobalObject.poorCastIron)) {
                                        craftable = false
                                    }
                                } // 사용한 철괴가 강철 또는 주철이 아닐 경우 제작 불가
                                if (it.type == Material.IRON_BLOCK) {
                                    val q = getQuality(it)
                                    val maxQ = getMaxQuality(it)
                                    if (maxQ == GlobalObject.defaultMaxQuality) {
                                        craftable = false
                                    } // 자연에서 얻은 철블록일 경우, 제작 불가

                                    if (q < 0.8) {
                                        craftable = false
                                    } // 품질이 0.8 이하일 경우, 제작 불가
                                }
                            }
                        }

                        if (!craftable) {
                            event.inventory.result = GlobalObject.banItem.clone()
                            return
                        }
                    }

                    // 선철 썼을 경우 퀄리티 하락
                    val rankDown = ironCount["PoorPigIron"]!! + ironCount["FinePigIron"]!! // 선철을 썼을 경우 품질 하락
                    quality = max(0.0, quality - rankDown)

                    // 강철 개수 카운트
                    val steelCount = ironCount["Steel"]!!
                    val bestSteelCount = ironCount["BestSteel"]!!

                    val result = recipe.result.clone()
                    when {
                        steelCount == ironCount["Iron"]!! -> {
                            when {
                                bestSteelCount == ironCount["Iron"]!! ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.ALL_BEST_STEEL))
                                bestSteelCount > 0 ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.CONTAINS_BEST_STEEL))
                                else ->
                                    applyDescription(result, quality / recipe.result.amount,
                                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.ALL_STEEL))
                            }
                        }
                        steelCount > 0 ->
                            applyDescription(result, quality / recipe.result.amount,
                                    ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.CONTAINS_STEEL))
                        else ->
                            applyDescription(result, quality / recipe.result.amount,
                                    ceil(maxQuality.toDouble() / recipe.result.amount).toInt(), specialty(SteelType.NONE))
                    }
                    event.inventory.result = result
                    return
                } // 철이 사용 됨.
                else -> {
                    val result = recipe.result.clone()

                    if (recipe.result.type.maxDurability > 0) {
                        val maxDurability = result.type.maxDurability
                        var durability = maxDurability.toDouble()
                        durability *= GlobalObject.durabilityCoefficient.pow(maxQuality - quality)
                        setDurabilityForTools(result, maxDurability, durability)
                    }

                    applyDescription(result, quality / recipe.result.amount,
                            ceil(maxQuality.toDouble() / recipe.result.amount).toInt())
                    event.inventory.result = result
                }
            }
        } else {
            val result = recipe.result.clone()

            if (recipe.result.type.maxDurability > 0) {
                val maxDurability = result.type.maxDurability
                var durability = maxDurability.toDouble()
                durability *= GlobalObject.durabilityCoefficient.pow(maxQuality - quality)
                setDurabilityForTools(result, maxDurability, durability)
            }

            applyDescription(result, quality / recipe.result.amount,
                    ceil(maxQuality.toDouble() / recipe.result.amount).toInt())
            event.inventory.result = result
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

            val newLocation = Location(world, x, y - 1, z)
            newLocation.block
        } // 떨어뜨린 위치 아래에 깔린 블록

        if (
                GlobalObject.isFundamentallySame(droppedItem, GlobalObject.poorSteel) and
                (getBlockGroup(blockUnderPlayer.type) == BlockGroup.ANVIL)
        ) {
            // 모루 위에 강철을 던졌을 때 → 단조
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
                    } // 강철로 레벨 업
            Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, forgingTicks.toLong())
            for (i in 1 until forgingTicks / 60) {
                Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
            }
        }

        if (blockUnderPlayer.type == Material.SMITHING_TABLE) {
            // 어떤 물건이든 대장장이 작업대 위에 올렸을 때 → 강화 (단, 철, 다이아몬드 제외)
            if ((droppedItem.type == Material.IRON_INGOT) or (droppedItem.type == Material.DIAMOND)) return
            val bookShelfArray: ArrayList<Block> = blockUnderPlayer.location.let {
                val x = it.x
                val y = it.y - 1
                val z = it.z
                val world = it.world

                val bookShelves = ArrayList<Block>()
                for (xIdx in -1..1) {
                    for (zIdx in -1..1) {
                        if ((xIdx != 0) or (zIdx != 0)) {
                            // xIdx와 zIdx가 모두 0이 아닐 경우 (바로 아래 칸이 아니면)
                            val checkLocation = Location(world, x + xIdx, y, z + zIdx)
                            if (checkLocation.block.type == Material.BOOKSHELF) {
                                bookShelves.add(checkLocation.block)
                            }
                        }
                    }
                }
                bookShelves
            } // 대장장이 작업대 아래 책장 개수 (바로 아래 제외)
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
                        val failureProbability = GlobalObject.reinForceBaseFailProbability -
                                GlobalObject.reinForceFailProbabilityDecrease * bookShelfArray.size // 실패 확률
                        val breakProbability = GlobalObject.reinForceBaseBreakProbability -
                                GlobalObject.reinForceBreakProbabilityDecrease * bookShelfArray.size // 파괴 확률

                        // 파괴 가능 블록으로 복귀
                        enabledBlocks.remove(blockUnderPlayer)
                        for (bookShelf in bookShelfArray) {
                            enabledBlocks.remove(bookShelf)
                        }

                        val random = Random.nextDouble()

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
                            }
                        }
                    } // 품질 상승
            Bukkit.getScheduler().runTaskLater(plugin!!, finishForging, reinforcingTicks.toLong())
            for (i in 1 until reinforcingTicks / 60) {
                Bukkit.getScheduler().runTaskLater(plugin!!, forgingSpark, i * 60L) // 3초에 한 번씩 모루질 소리
            }
        }
    }

    @EventHandler
    fun onEnableBlockDamaging(event: BlockDamageEvent) {
        if (!GlobalObject.isOn) return // 시작 전에는 따로 판단하지 않는다.

        val block = event.block
        if (enabledBlocks.contains(block)) { // 부술 수 없는 블록은 때릴 수 없다.
            event.player.sendActionBar("작업 중에는 부술 수 없다")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!GlobalObject.isOn) return

        if (event.clickedBlock != null) {
            if (enabledBlocks.contains(event.clickedBlock)) {
                event.player.sendActionBar("작업 중에는 사용할 수 없다")
                event.isCancelled = true
            }
        }

        val hand = event.hand ?: return
        val itemOnHand = event.item ?: return

        val quality = getQuality(itemOnHand)
        val base = GlobalObject.itemBreakBaseProbability
        val random = Random.nextDouble()
        if (random > (base + (1 - base) * quality)) {
            event.player.world.playSound(event.player.location, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f)
            event.player.sendActionBar("${ChatColor.RED}아이템이 깨져 버렸다.")
            event.player.inventory.setItem(hand, null)
        }
    }
}