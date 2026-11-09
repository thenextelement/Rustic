package rustic.compat.minetweaker;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.minecraft.MineTweakerMC;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.crafting.Recipes;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.rustic.Condenser")
public class Condenser {

    /**
     * {@code mods.rustic.Condenser.addRecipe(output, [inputs], modifier?, bottle?, fluid?, time?)}. Inputs may also be
     * given as two separate ingredients for the 1.12 {@code addRecipe(output, input1, input2)} form.
     */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient[] inputs, @Optional IIngredient modifier,
        @Optional IIngredient bottle, @Optional ILiquidStack fluid, @Optional int time) {
        if (output == null || inputs == null) {
            MineTweakerAPI.logError("Condenser recipe needs an output and inputs");
            return;
        }
        if (inputs.length > 3) {
            MineTweakerAPI.logError("Condenser recipe has at most 3 inputs");
            return;
        }
        if (time < 0) {
            MineTweakerAPI.logError("Brew time must be positive");
            return;
        }
        FluidStack fluidStack = fluid == null ? new FluidStack(FluidRegistry.WATER, 125)
            : MineTweakerMC.getLiquidStack(fluid);
        if (bottle == null) bottle = MineTweakerMC.getIItemStack(new ItemStack(net.minecraft.init.Items.glass_bottle));
        add(
            new MTCondenserRecipe(
                MineTweakerMC.getItemStack(output),
                inputs,
                modifier,
                bottle,
                fluidStack,
                time == 0 ? 400 : time));
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input1, IIngredient input2) {
        addRecipe(output, new IIngredient[] { input1, input2 }, null, null, null, 0);
    }

    private static void add(final ICondenserRecipe r) {
        MineTweakerAPI.apply(
            SnapshotAction.onList(
                Recipes.condenserRecipes,
                "Adding Alchemy Recipe for Item " + r.getResult()
                    .getDisplayName(),
                () -> Recipes.condenserRecipes.add(r)));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        final ItemStack out = MineTweakerMC.getItemStack(output);
        MineTweakerAPI.apply(
            SnapshotAction.onList(
                Recipes.condenserRecipes,
                "Removing Condenser Recipes for Item " + out.getDisplayName(),
                () -> Recipes.removeCondenserRecipe(out)));
    }

}
