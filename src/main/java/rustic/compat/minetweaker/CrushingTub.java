package rustic.compat.minetweaker;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.minecraft.MineTweakerMC;
import rustic.common.crafting.CrushingTubRecipe;
import rustic.common.crafting.Recipes;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.rustic.CrushingTub")
public class CrushingTub {

    @ZenMethod
    public static void addRecipe(ILiquidStack output, IItemStack byproduct, IItemStack input) {
        final CrushingTubRecipe r = new CrushingTubRecipe(
            MineTweakerMC.getLiquidStack(output),
            MineTweakerMC.getItemStack(input),
            byproduct == null ? null : MineTweakerMC.getItemStack(byproduct));
        MineTweakerAPI.apply(
            SnapshotAction.onList(
                Recipes.crushingTubRecipes,
                "Adding Crushing Tub Recipe for Fluid " + r.getResult()
                    .getLocalizedName(),
                () -> Recipes.crushingTubRecipes.add(r)));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack input) {
        final ItemStack in = MineTweakerMC.getItemStack(input);
        MineTweakerAPI.apply(
            SnapshotAction.onList(
                Recipes.crushingTubRecipes,
                "Removing Crushing Tub Recipes for Item " + in.getDisplayName(),
                () -> Recipes.removeCrushingRecipe(in)));
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack output, @Optional IItemStack input) {
        if (output == null) {
            MineTweakerAPI.logError("Cannot remove Crushing Tub recipe for null Fluid output");
            return;
        }
        final FluidStack out = MineTweakerMC.getLiquidStack(output);
        final ItemStack in = input == null ? null : MineTweakerMC.getItemStack(input);
        MineTweakerAPI.apply(
            SnapshotAction.onList(
                Recipes.crushingTubRecipes,
                "Removing Crushing Tub Recipes for Fluid " + out.getLocalizedName(),
                () -> Recipes.removeCrushingRecipe(out, in)));
    }

}
