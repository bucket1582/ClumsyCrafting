package com.github.bucket1572.clumsycrafting

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin

object ClumsyRecipes {
    var plugin: JavaPlugin? = null // 대상 플러그인

    fun loadAll() {
        val addedRecipes = listOf(
                cokesFuelRecipe(),
                cokesBlastingRecipe(),
                vanillaIronIngot(),
                blastIronIngot(),
                poorCastIronIngot(),
                fineCastIronIngot(),
                steelIngot(),
                steelConverter(),
                steelConverter2(),
                banRepairs(),
                banBlast(),
                alternateBlast(),
        )

        for (recipe in addedRecipes) {
            plugin!!.server.addRecipe(recipe)
        }
    } // 추가 된 모든 조합법 로딩

    private fun cokesFuelRecipe() : FurnaceRecipe {
        val result = GlobalObject.coke.clone()

        val namespacedKey = NamespacedKey(plugin!!, "coke")
        return FurnaceRecipe(
                namespacedKey, result, Material.COAL,
                GlobalObject.cokesDryingExperience.toFloat(), GlobalObject.cokesDryingTickTime
        )
    } // 코크스 조합법

    private fun cokesBlastingRecipe() : BlastingRecipe {
        val result = GlobalObject.coke.clone()

        val namespacedKey = NamespacedKey(plugin!!, "coke_blast")
        return BlastingRecipe(
                namespacedKey, result, Material.COAL,
                GlobalObject.cokesDryingExperience.toFloat(), GlobalObject.cokesDryingTickTime / 2
        )
    } // 코크스 (용광로) 조합법

    private fun vanillaIronIngot() : FurnaceRecipe {
        val result = GlobalObject.poorPigIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "original_iron_ingot")

        return FurnaceRecipe(
                namespacedKey, result, Material.IRON_ORE,
                0.7f, 200
        )
    } // 선철 조합법

    private fun blastIronIngot() : BlastingRecipe {
        val result = GlobalObject.finePigIron.clone()

        val namespacedKey = NamespacedKey(plugin!!, "original_iron_ingot_blast")

        return BlastingRecipe(
                namespacedKey, result, Material.IRON_ORE,
                0.7f, 100
        )
    } // 선철 (용광로) 조합법

    private fun poorCastIronIngot() : SmithingRecipe {
        val result = GlobalObject.poorCastIron.clone()
        val mineral = GlobalObject.poorPigIron.clone()

        val addition = RecipeChoice.ExactChoice(mineral)
        val base = RecipeChoice.MaterialChoice(Material.FLOWER_POT)

        val namespacedKey = NamespacedKey(plugin!!, "poor_cast_ingot")
        return SmithingRecipe(
                namespacedKey, result, base, addition
        )
    } // 주철 (0/1) 조합법

    private fun fineCastIronIngot() : SmithingRecipe {
        val result = GlobalObject.fineCastIron.clone()
        val mineral = GlobalObject.finePigIron.clone()

        val addition = RecipeChoice.ExactChoice(mineral)
        val base = RecipeChoice.MaterialChoice(Material.FLOWER_POT)

        val namespacedKey = NamespacedKey(plugin!!, "fine_cast_ingot")
        return SmithingRecipe(
                namespacedKey, result, base, addition
        )
    } // 주철 (1/1) 조합법

    private fun steelIngot() : ShapelessRecipe {
        val result = GlobalObject.poorSteel.clone()

        val workSpace = RecipeChoice.MaterialChoice(Material.SMITHING_TABLE, Material.ANVIL)

        val namespacedKey = NamespacedKey(plugin!!, "poor_steel_ingot")
        val recipe = ShapelessRecipe(namespacedKey, result)
        recipe.apply {
            addIngredient(Material.IRON_INGOT)
            addIngredient(Material.CAULDRON)
            addIngredient(workSpace)
        }
        return recipe
    } // 강철 조합법

    private fun steelConverter() : ShapedRecipe {
        val result = ItemStack(Material.CAULDRON)
        applyDescription(result, GlobalObject.converterName, 0.0, 0)
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
    } // 전로 조합법

    private fun steelConverter2() : ShapedRecipe {
        val result = ItemStack(Material.CAULDRON)
        applyDescription(result, GlobalObject.converterName, 0.0, 0)
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
    } // 전로 거울상 조합법

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
                Material.NETHERITE_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL,
                Material.SHIELD, Material.SHEARS, Material.BOW, Material.CROSSBOW,
                Material.FISHING_ROD, Material.CARROT_ON_A_STICK,
                Material.WARPED_FUNGUS_ON_A_STICK, Material.ELYTRA, Material.FLINT_AND_STEEL,
                Material.TRIDENT
        )
        val tools2 = tools.clone()
        recipe.apply {
            addIngredient(tools)
            addIngredient(tools2)
        }
        return recipe
    } // 수리 금지

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
    } // 용광로 조합 금지

    private fun alternateBlast() : ShapedRecipe {
        val namespacedKey = NamespacedKey(plugin!!, "alter_blast")

        val recipe = ShapedRecipe(namespacedKey, ItemStack(Material.BLAST_FURNACE))
        recipe.apply {
            shape("III", "IFI", "CKC")
            setIngredient('I', Material.IRON_INGOT)
            setIngredient('F', Material.FURNACE)
            setIngredient('C', Material.COAL_BLOCK)
            setIngredient('K', Material.COAL)
        }
        return recipe
    } // 용광로 조합 대체
}