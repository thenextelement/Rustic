package rustic.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * In 1.12 this stripped returned containers (buckets, bottles) from the crafting grid. Rustic's 1.7.10 fluid bottle
 * and liquid barrel have no container item, so a plain shapeless recipe behaves the same.
 */
public class RecipeNonIngredientReturn extends ShapelessOreRecipe {

	public RecipeNonIngredientReturn(ItemStack result, Object... recipe) {
		super(result, recipe);
	}

}
