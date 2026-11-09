package rustic.compat.nei;

import java.util.Arrays;
import java.util.Collections;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import codechicken.nei.recipe.ShapelessRecipeHandler;

/**
 * NEI's shapeless crafting handler plus Rustic's custom shapeless recipes (olive oiling, vanta oiling). It replaces
 * NEI's own instance and keeps its handler id, so the recipes appear in the normal "Shapeless Crafting" tab.
 */
public class RusticShapelessRecipeHandler extends ShapelessRecipeHandler {

    @Override
    public String getHandlerId() {
        return ShapelessRecipeHandler.class.getName();
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        boolean all = "crafting".equals(outputId), small = "crafting2x2".equals(outputId);
        if (!all && !small) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        // NEI's "show all" only runs for the exact ShapelessRecipeHandler class, so it is repeated here
        for (IRecipe irecipe : getCraftingRecipesSnapshot()) {
            if (small && irecipe.getRecipeSize() > 4) continue;
            CachedShapelessRecipe recipe = null;
            if (irecipe instanceof ShapelessRecipes) recipe = vanilla((ShapelessRecipes) irecipe);
            else if (irecipe instanceof ShapelessOreRecipe) recipe = forgeShapelessRecipe((ShapelessOreRecipe) irecipe);
            if (recipe != null) arecipes.add(recipe);
        }
        for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(true)) {
            if (!small || r.inputs.length <= 4) arecipes.add(new Cached(r, null));
        }
    }

    private CachedShapelessRecipe vanilla(ShapelessRecipes recipe) {
        if (recipe.recipeItems == null) return null;
        try {
            return new CachedShapelessRecipe(recipe.recipeItems, recipe.getRecipeOutput());
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        super.loadCraftingRecipes(result);
        for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(true)) {
            if (RusticCraftingRecipes.produces(r, result)) arecipes.add(new Cached(r, r.isVanta() ? result : null));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        super.loadUsageRecipes(ingredient);
        for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(true)) {
            if (!RusticCraftingRecipes.uses(r, ingredient)) continue;
            Cached c = new Cached(r, null);
            c.setIngredientPermutation(c.ingredients, ingredient);
            arecipes.add(c);
        }
    }

    /**
     * Vanta oiling is one recipe per weapon and olive oiling one per food; grouping each set lets NEI show it as a
     * single cycling recipe with every member's inputs and output in step.
     */
    @Override
    public Object getRecipeGroupId(int recipe) {
        if (recipe >= 0 && recipe < arecipes.size() && arecipes.get(recipe) instanceof Cached) {
            Cached c = (Cached) arecipes.get(recipe);
            if (c.recipe.isVanta()) return "rustic:vanta_oiling";
            if (c.recipe.result != null && c.recipe.result.hasTagCompound()
                && c.recipe.result.getTagCompound()
                    .hasKey("oiled"))
                return "rustic:olive_oiling";
        }
        return super.getRecipeGroupId(recipe);
    }

    /** A Rustic shapeless recipe; for vanta oiling the output can be the oiled weapon being looked up. */
    public class Cached extends CachedShapelessRecipe {

        final RusticCraftingRecipes.Recipe recipe;

        Cached(RusticCraftingRecipes.Recipe r, ItemStack oiledWeapon) {
            this.recipe = r;
            Object[] inputs = r.inputs.clone();
            if (oiledWeapon != null) inputs[0] = RusticCraftingRecipes.stripVanta(oiledWeapon);
            setIngredients(Arrays.asList(inputs));
            setResult(oiledWeapon != null ? oiledWeapon : r.result);
            // the note doesn't fit the crafting grid's width, so it goes on the output's tooltip
            if (r.note != null) result.setTooltip(Collections.singletonList(StatCollector.translateToLocal(r.note)));
        }
    }
}
