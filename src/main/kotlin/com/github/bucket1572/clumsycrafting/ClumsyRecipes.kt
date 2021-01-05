package com.github.bucket1572.clumsycrafting

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin

object ClumsyRecipes {
    var plugin: JavaPlugin? = null

    fun loadAll() {
        val addedRecipes = listOf(
                cokesFuelRecipe(),
                cokesBlastingRecipe(),
                vanillaIronIngot(),
                blastIronIngot(),
                poorCastIronIngot(),
                fineCastIronIngot(),
                poorSteelIngot(),
                fineSteelIngot(),
                steelConverter(),
                steelConverter2(),
                banRepairs(),
                banBlast(),
                alternateBlast(),
                banAnvil(),
                alterAnvil()
        )

        for (recipe in addedRecipes) {
            plugin!!.server.addRecipe(recipe)
        }
    }

    private fun cokesFuelRecipe() : FurnaceRecipe {
        val result = GlobalObject.coke.clone()

        val namespacedKey = NamespacedKey(plugin!!, "coke")
        return FurnaceRecipe(
                namespacedKey, result, Material.COAL,
                GlobalObject.cokesDryingExperience.toFloat(), GlobalObject.cokesDryingTickTime
        )
    }

    private fun cokesBlastingRecipe() : BlastingRecipe {
        val result = GlobalObject.coke.clone()

        val namespacedKey = NamespacedKey(plugin!!, "coke_blast")
        return BlastingRecipe(
                namespacedKey, result, Material.COAL,
                GlobalObject.cokesDryingExperience.toFloat(), GlobalObject.cokesDryingTickTime / 2
        )
    }

    private fun vanillaIronIngot() : FurnaceRecipe {
        val result = GlobalObject.poorPigIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "original_iron_ingot")

        return FurnaceRecipe(
                namespacedKey, result, Material.IRON_ORE,
                0.7f, 200
        )
    }

    private fun blastIronIngot() : BlastingRecipe {
        val result = GlobalObject.finePigIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "original_iron_ingot")

        return BlastingRecipe(
                namespacedKey, result, Material.IRON_ORE,
                0.7f, 200
        )
    }

    private fun poorCastIronIngot() : SmithingRecipe {
        val result = GlobalObject.poorCastIron.clone()
        val mineral = GlobalObject.poorPigIron.clone()

        val addition = RecipeChoice.ExactChoice(mineral)
        val base = RecipeChoice.MaterialChoice(Material.FLOWER_POT)

        val namespacedKey = NamespacedKey(plugin!!, "poor_cast_ingot")
        return SmithingRecipe(
                namespacedKey, result, base, addition
        )
    }

    private fun fineCastIronIngot() : SmithingRecipe {
        val result = GlobalObject.fineCastIron.clone()
        val mineral = GlobalObject.finePigIron.clone()

        val addition = RecipeChoice.ExactChoice(mineral)
        val base = RecipeChoice.MaterialChoice(Material.FLOWER_POT)

        val namespacedKey = NamespacedKey(plugin!!, "fine_cast_ingot")
        return SmithingRecipe(
                namespacedKey, result, base, addition
        )
    }

    private fun poorSteelIngot() : ShapelessRecipe {
        val result = ItemStack(Material.IRON_INGOT)
        result.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.steelName)
            meta.lore = listOf(
                    GlobalObject.rank(2, 2)
            )
            itemMeta = meta
        }
        val finePig = GlobalObject.finePigIron.clone()

        val poorConverter = ArrayList<ItemStack>()
        for (idx in 0..10) {
            val converterRank = ItemStack(Material.CAULDRON)
            converterRank.apply {
                val meta = itemMeta
                meta.setDisplayName(GlobalObject.converterName)
                meta.lore = listOf(
                        GlobalObject.rank(12 - idx, 12)
                )
                itemMeta = meta
            }
            poorConverter.add(converterRank)
        }
        val converter = RecipeChoice.ExactChoice(poorConverter)

        val workSpace = RecipeChoice.MaterialChoice(Material.SMITHING_TABLE, Material.ANVIL)

        val namespacedKey = NamespacedKey(plugin!!, "poor_steel_ingot")
        val recipe = ShapelessRecipe(namespacedKey, result)
        recipe.apply {
            addIngredient(finePig)
            addIngredient(converter)
            addIngredient(workSpace)
        }
        return recipe
    }

    private fun fineSteelIngot() : ShapelessRecipe {
        val result = ItemStack(Material.IRON_INGOT)
        result.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.steelName)
            meta.lore = listOf(
                    GlobalObject.rank(1, 2)
            )
            itemMeta = meta
        }
        val finePig = GlobalObject.finePigIron.clone()

        val poorConverter = ArrayList<ItemStack>()
        for (idx in 11..12) {
            val converterRank = ItemStack(Material.CAULDRON)
            converterRank.apply {
                val meta = itemMeta
                meta.setDisplayName(GlobalObject.converterName)
                meta.lore = listOf(
                        GlobalObject.rank(12 - idx, 12)
                )
                itemMeta = meta
            }
            poorConverter.add(converterRank)
        }
        val converter = RecipeChoice.ExactChoice(poorConverter)

        val workSpace = RecipeChoice.MaterialChoice(Material.SMITHING_TABLE, Material.ANVIL)

        val namespacedKey = NamespacedKey(plugin!!, "fine_steel_ingot")
        val recipe = ShapelessRecipe(namespacedKey, result)
        recipe.apply {
            addIngredient(finePig)
            addIngredient(converter)
            addIngredient(workSpace)
        }
        return recipe
    }

    private fun steelConverter() : ShapedRecipe {
        val result = ItemStack(Material.CAULDRON)
        result.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.converterName)
            itemMeta = meta
        }
        val poorIngot = GlobalObject.poorCastIron.clone()
        val fineIngot = GlobalObject.fineCastIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "steel_converter")
        val magma = RecipeChoice.MaterialChoice(Material.MAGMA_BLOCK, Material.MAGMA_CREAM)
        val ironIngot = RecipeChoice.ExactChoice(poorIngot, fineIngot)
        val recipe = ShapedRecipe(namespacedKey, result)
        recipe.apply {
            shape("C  ", "CMC", "CCC")
            setIngredient('C', ironIngot)
            setIngredient('M', magma)
        }
        return recipe
    }

    private fun steelConverter2() : ShapedRecipe {
        val result = ItemStack(Material.CAULDRON)
        result.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.converterName)
            itemMeta = meta
        }
        val poorIngot = GlobalObject.poorCastIron.clone()
        val fineIngot = GlobalObject.fineCastIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "steel_converter_mirrored")
        val magma = RecipeChoice.MaterialChoice(Material.MAGMA_BLOCK, Material.MAGMA_CREAM)
        val ironIngot = RecipeChoice.ExactChoice(poorIngot, fineIngot)
        val recipe = ShapedRecipe(namespacedKey, result)
        recipe.apply {
            shape("  C", "CMC", "CCC")
            setIngredient('C', ironIngot)
            setIngredient('M', magma)
        }
        return recipe
    }

    private fun banRepairs() : ShapelessRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "ban_repairs")
        val banItem = GlobalObject.banItem.clone()
        val recipe = ShapelessRecipe(namespacedKey, banItem)
        val tools = RecipeChoice.MaterialChoice(
                Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE,
                Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE,
                Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE,
                Material.NETHERITE_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE,
                Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.IRON_HOE,
                Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE,
                Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.GOLDEN_SWORD,
                Material.NETHERITE_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
                Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.GOLDEN_SHOVEL,
                Material.NETHERITE_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL
        )
        val tools2 = tools.clone()
        recipe.apply {
            addIngredient(tools)
            addIngredient(tools2)
        }
        return recipe
    }

    private fun banBlast() : ShapedRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "ban_blast")
        val banItem = GlobalObject.banItem.clone()
        val recipe = ShapedRecipe(namespacedKey, banItem)
        recipe.apply {
            shape("III", "IFI", "SSS")
            setIngredient('I', Material.IRON_INGOT)
            setIngredient('F', Material.FURNACE)
            setIngredient('S', Material.SMOOTH_STONE)
        }
        return recipe
    }

    private fun alternateBlast() : ShapedRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "alter_blast")
        val steel = ArrayList<ItemStack>()
        for (idx in 0..1) {
            val fineSteel = ItemStack(Material.IRON_INGOT)
            fineSteel.apply {
                val meta = itemMeta
                meta.setDisplayName(GlobalObject.steelName)
                meta.lore = listOf(
                        GlobalObject.rank(idx, 2)
                )
                itemMeta = meta
            }
            steel.add(fineSteel)
        }

        val steelChoice = RecipeChoice.ExactChoice(steel)
        val coke = GlobalObject.coke.clone()

        val recipe = ShapedRecipe(namespacedKey, ItemStack(Material.BLAST_FURNACE))
        recipe.apply {
            shape("III", "IFI", "CKC")
            setIngredient('I', steelChoice)
            setIngredient('F', Material.FURNACE)
            setIngredient('C', Material.COAL_BLOCK)
            setIngredient('K', coke)
        }
        return recipe
    }

    private fun banAnvil() : ShapedRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "ban_anvil")
        val banItem = GlobalObject.banItem.clone()

        val recipe = ShapedRecipe(namespacedKey, banItem)
        recipe.apply {
            shape("BBB", " I ", "III")
            setIngredient('I', Material.IRON_INGOT)
            setIngredient('B', Material.IRON_BLOCK)
        }
        return recipe
    }

    private fun alterAnvil() : ShapedRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "alter_anvil")

        val niceCastBlock = ItemStack(Material.IRON_BLOCK)
        niceCastBlock.apply {
            val meta = itemMeta
            meta.lore = listOf(
                    GlobalObject.rank(0, 18),
            )
            itemMeta = meta
        }
        val niceSteelBlock = ItemStack(Material.IRON_BLOCK)
        niceSteelBlock.apply {
            val meta = itemMeta
            meta.lore = listOf(
                    GlobalObject.rank(0, 18),
                    "${ChatColor.YELLOW}강철"
            )
            itemMeta = meta
        }
        val niceBlock = RecipeChoice.ExactChoice(niceCastBlock, niceSteelBlock)

        val niceCastIngot = GlobalObject.fineCastIron.clone()
        val fineSteelIngot = ItemStack(Material.IRON_INGOT)
        fineSteelIngot.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.steelName)
            meta.lore = listOf(
                    GlobalObject.rank(1, 2)
            )
            itemMeta = meta
        }
        val bestSteelIngot = ItemStack(Material.IRON_INGOT)
        bestSteelIngot.apply {
            val meta = itemMeta
            meta.setDisplayName(GlobalObject.steelName)
            meta.lore = listOf(
                    GlobalObject.rank(0, 2)
            )
            itemMeta = meta
        }

        val niceIngot = RecipeChoice.ExactChoice(niceCastIngot, fineSteelIngot, bestSteelIngot)

        val recipe = ShapedRecipe(namespacedKey, ItemStack(Material.ANVIL))
        recipe.apply {
            shape("BBB", " I ", "III")
            setIngredient('I', niceIngot)
            setIngredient('B', niceBlock)
        }
        return recipe
    }
}