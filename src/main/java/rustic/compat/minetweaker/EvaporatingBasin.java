package rustic.compat.minetweaker;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.minecraft.MineTweakerMC;
import rustic.common.crafting.EvaporatingBasinRecipe;
import rustic.common.crafting.IEvaporatingBasinRecipe;
import rustic.common.crafting.Recipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.rustic.EvaporatingBasin")
public class EvaporatingBasin {

    @ZenMethod
    public static void addRecipe(IItemStack output, ILiquidStack input) {
        add(new EvaporatingBasinRecipe(MineTweakerMC.getItemStack(output), MineTweakerMC.getLiquidStack(input)));
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, ILiquidStack input, int time) {
        if (time < 20) {
            MineTweakerAPI.logError("Minimum evaporation time for evaporating basin is 20 ticks");
            return;
        }
        add(new TimedRecipe(MineTweakerMC.getItemStack(output), MineTweakerMC.getLiquidStack(input), time));
    }

    private static void add(final IEvaporatingBasinRecipe r) {
        MineTweakerAPI.apply(
            SnapshotAction.onMap(
                Recipes.evaporatingRecipesMap,
                "Adding Evaporating Recipe for Item " + r.getOutput()
                    .getDisplayName(),
                () -> Recipes.add(r)));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        final ItemStack out = MineTweakerMC.getItemStack(output);
        MineTweakerAPI.apply(
            SnapshotAction.onMap(
                Recipes.evaporatingRecipesMap,
                "Removing Evaporating Recipes for Item " + out.getDisplayName(),
                () -> Recipes.removeEvaporatingRecipe(out)));
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack input) {
        final FluidStack in = MineTweakerMC.getLiquidStack(input);
        MineTweakerAPI.apply(
            SnapshotAction.onMap(
                Recipes.evaporatingRecipesMap,
                "Removing Evaporating Recipe for Fluid " + in.getLocalizedName(),
                () -> Recipes.removeEvaporatingRecipe(in)));
    }

    /** Evaporating recipe with a custom duration. */
    public static class TimedRecipe implements IEvaporatingBasinRecipe {

        private final FluidStack input;
        private final ItemStack output;
        private final int time;

        public TimedRecipe(ItemStack out, FluidStack in, int time) {
            this.output = out;
            this.input = in;
            this.time = time;
        }

        @Override
        public Fluid getFluid() {
            return input.getFluid();
        }

        @Override
        public int getAmount() {
            return input.amount;
        }

        @Override
        public FluidStack getInput() {
            return input.copy();
        }

        @Override
        public ItemStack getOutput() {
            return output.copy();
        }

        @Override
        public int getTime() {
            return time;
        }
    }

}
