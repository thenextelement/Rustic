package rustic.compat.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;

import codechicken.nei.recipe.ShapedRecipeHandler;

/**
 * NEI's shaped crafting handler plus Rustic's custom shaped recipes (cabinets). It replaces NEI's own instance and
 * keeps
 * its handler id, so the recipes appear in the normal "Shaped Crafting" tab with its name, icon and settings.
 */
public class RusticShapedRecipeHandler extends ShapedRecipeHandler {

    @Override
    public String getHandlerId() {
        return ShapedRecipeHandler.class.getName();
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        boolean all = "crafting".equals(outputId), small = "crafting2x2".equals(outputId);
        if (!all && !small) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        // NEI's "show all" only runs for the exact ShapedRecipeHandler class, so it is repeated here
        for (IRecipe irecipe : getCraftingRecipesSnapshot()) {
            CachedShapedRecipe recipe = null;
            if (irecipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) irecipe;
                if (!small || shaped.recipeWidth <= 2 && shaped.recipeHeight <= 2)
                    recipe = new CachedShapedRecipe(shaped);
            } else if (all && irecipe instanceof ShapedOreRecipe) {
                recipe = forgeShapedRecipe((ShapedOreRecipe) irecipe);
            }
            if (recipe != null) {
                recipe.computeVisuals();
                arecipes.add(recipe);
            }
        }
        if (all) for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(false)) arecipes.add(cached(r));
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        super.loadCraftingRecipes(result);
        for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(false)) {
            if (RusticCraftingRecipes.produces(r, result)) arecipes.add(cached(r));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        super.loadUsageRecipes(ingredient);
        for (RusticCraftingRecipes.Recipe r : RusticCraftingRecipes.of(false)) {
            if (!RusticCraftingRecipes.uses(r, ingredient)) continue;
            CachedShapedRecipe c = cached(r);
            c.setIngredientPermutation(c.ingredients, ingredient);
            arecipes.add(c);
        }
    }

    private CachedShapedRecipe cached(RusticCraftingRecipes.Recipe r) {
        CachedShapedRecipe c = new CachedShapedRecipe(3, 3, r.inputs, r.result);
        c.computeVisuals();
        return c;
    }
}
