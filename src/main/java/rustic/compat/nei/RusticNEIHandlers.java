package rustic.compat.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import codechicken.nei.PositionedStack;
import rustic.common.crafting.IBrewingBarrelRecipe;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.crafting.ICrushingTubRecipe;
import rustic.common.crafting.IEvaporatingBasinRecipe;
import rustic.common.crafting.Recipes;

/** NEI handlers for Rustic's machines, mirroring the 1.12 JEI categories and layouts. */
public final class RusticNEIHandlers {

    private RusticNEIHandlers() {}

    /** Generic cached recipe: a fixed set of ingredient / other stacks and one result. */
    private static List<PositionedStack> nonNull(PositionedStack... stacks) {
        List<PositionedStack> list = new ArrayList<>();
        for (PositionedStack s : stacks) if (s != null) list.add(s);
        return list;
    }

    public static class CrushingTub extends RusticRecipeHandler<ICrushingTubRecipe> {

        public CrushingTub() {
            super("rustic.crushing_tub", "jei_crushing_tub", 108, 64);
        }

        @Override
        protected String getNameKey() {
            return "rustic.jei.recipe.crushing_tub";
        }

        @Override
        protected Collection<? extends ICrushingTubRecipe> getRecipes() {
            return Recipes.crushingTubRecipes;
        }

        @Override
        protected boolean produces(ICrushingTubRecipe r, ItemStack result, FluidStack fluid) {
            return sameFluid(fluid, r.getResult()) || matches(result, r.getByproduct());
        }

        @Override
        protected boolean uses(ICrushingTubRecipe r, ItemStack ingredient, FluidStack fluid) {
            return matches(ingredient, r.getInput());
        }

        @Override
        protected CachedRecipe createCached(ICrushingTubRecipe r) {
            return new Cached(r);
        }

        private class Cached extends CachedRecipe {

            final PositionedStack input, result, fluid;

            Cached(ICrushingTubRecipe r) {
                input = slot(r.getInput(), 20, 23);
                fluid = tank(r.getResult(), 71, 5, 16, 32);
                result = slot(r.getByproduct(), 70, 42);
            }

            @Override
            public List<PositionedStack> getIngredients() {
                return getCycledIngredients(cycleticks / 20, nonNull(input));
            }

            @Override
            public PositionedStack getResult() {
                return result != null ? result : fluid;
            }

            @Override
            public List<PositionedStack> getOtherStacks() {
                return result != null ? nonNull(fluid) : Collections.<PositionedStack>emptyList();
            }
        }
    }

    public static class Evaporating extends RusticRecipeHandler<IEvaporatingBasinRecipe> {

        public Evaporating() {
            super("rustic.evaporating", "jei_evaporating_basin", 108, 64);
        }

        @Override
        protected String getNameKey() {
            return "rustic.jei.recipe.evaporating";
        }

        @Override
        protected Collection<? extends IEvaporatingBasinRecipe> getRecipes() {
            return Recipes.evaporatingRecipesMap.values();
        }

        @Override
        protected boolean produces(IEvaporatingBasinRecipe r, ItemStack result, FluidStack fluid) {
            return matches(result, r.getOutput());
        }

        @Override
        protected boolean uses(IEvaporatingBasinRecipe r, ItemStack ingredient, FluidStack fluid) {
            return sameFluid(fluid, r.getInput());
        }

        @Override
        protected CachedRecipe createCached(IEvaporatingBasinRecipe r) {
            return new Cached(r);
        }

        private class Cached extends CachedRecipe {

            final PositionedStack fluid, result;

            Cached(IEvaporatingBasinRecipe r) {
                fluid = tank(r.getInput(), 21, 16, 16, 32);
                result = slot(r.getOutput(), 70, 23);
            }

            @Override
            public List<PositionedStack> getIngredients() {
                return nonNull(fluid);
            }

            @Override
            public PositionedStack getResult() {
                return result;
            }
        }
    }

    public static class Brewing extends RusticRecipeHandler<IBrewingBarrelRecipe> {

        public Brewing() {
            super("rustic.brewing", "jei_brewing", 130, 80);
        }

        @Override
        protected String getNameKey() {
            return "rustic.jei.recipe.brewing";
        }

        @Override
        protected Collection<? extends IBrewingBarrelRecipe> getRecipes() {
            return Recipes.brewingRecipes;
        }

        @Override
        protected Rectangle getTransferArea() {
            return new Rectangle(63, 32, 24, 16);
        }

        @Override
        protected boolean produces(IBrewingBarrelRecipe r, ItemStack result, FluidStack fluid) {
            return sameFluid(fluid, r.getOuput());
        }

        @Override
        protected boolean uses(IBrewingBarrelRecipe r, ItemStack ingredient, FluidStack fluid) {
            return sameFluid(fluid, r.getInput()) || sameFluid(fluid, r.getOuput());
        }

        @Override
        public void drawExtras(int recipe) {
            drawSprite(117, 26, 130, 0, 11, 28, 100, 3);
            drawSprite(63, 32, 130, 28, 24, 16, 1200, 0);
            drawSprite(23, 35, 130, 44, 10, 10, 1200, 0);
        }

        @Override
        protected CachedRecipe createCached(IBrewingBarrelRecipe r) {
            return new Cached(r);
        }

        private class Cached extends CachedRecipe {

            final PositionedStack input, output, aux;

            Cached(IBrewingBarrelRecipe r) {
                input = tank(r.getInput(), 40, 24, 16, 32);
                output = tank(r.getOuput(), 94, 24, 16, 32);
                FluidStack auxFluid = r.getOuput() == null ? null
                    : r.getOuput()
                        .copy();
                if (auxFluid != null) auxFluid.amount = 1000;
                aux = tank(auxFluid, 4, 32, 16, 16);
                if (aux != null) aux.setTooltip(
                    Collections
                        .singletonList(net.minecraft.util.StatCollector.translateToLocal("rustic.nei.optional_aux")));
            }

            @Override
            public List<PositionedStack> getIngredients() {
                return nonNull(input);
            }

            @Override
            public PositionedStack getResult() {
                return output;
            }

            @Override
            public List<PositionedStack> getOtherStacks() {
                return nonNull(aux);
            }
        }
    }

    public static class Alchemy extends RusticRecipeHandler<ICondenserRecipe> {

        private final boolean advanced;

        public Alchemy(boolean advanced) {
            super(
                advanced ? "rustic.alchemy_advanced" : "rustic.alchemy_simple",
                advanced ? "jei_alchemy_advanced" : "jei_alchemy_simple",
                130,
                79);
            this.advanced = advanced;
            transferRects.clear(); // the area depends on 'advanced', which wasn't set yet in super()
            addTransferRect();
        }

        /** NEI instantiates handlers reflectively per lookup through {@link #newInstance()}. */
        @Override
        public codechicken.nei.recipe.TemplateRecipeHandler newInstance() {
            return new Alchemy(advanced);
        }

        @Override
        protected String getNameKey() {
            return advanced ? "rustic.jei.recipe.advanced_alchemy" : "rustic.jei.recipe.simple_alchemy";
        }

        @Override
        protected Collection<? extends ICondenserRecipe> getRecipes() {
            List<ICondenserRecipe> list = new ArrayList<>();
            for (ICondenserRecipe r : Recipes.condenserRecipes) {
                if (advanced ? r.isAdvanced() : r.isBasic()) list.add(r);
            }
            return list;
        }

        @Override
        protected Rectangle getTransferArea() {
            return advanced ? new Rectangle(21, 14, 50, 53) : new Rectangle(21, 26, 50, 28);
        }

        @Override
        protected boolean produces(ICondenserRecipe r, ItemStack result, FluidStack fluid) {
            return matches(result, r.getResult());
        }

        @Override
        protected boolean uses(ICondenserRecipe r, ItemStack ingredient, FluidStack fluid) {
            if (sameFluid(fluid, r.getFluid())) return true;
            if (ingredient == null) return false;
            if (matchesAny(ingredient, r.getBottles()) || matchesAny(ingredient, r.getModifiers())) return true;
            for (List<ItemStack> input : r.getInputs()) if (matchesAny(ingredient, input)) return true;
            return false;
        }

        @Override
        public void drawExtras(int recipe) {
            drawSprite(44, 43, 130, 0, 14, 14, 300, 7);
            if (advanced) drawSprite(21, 14, 130, 14, 50, 53, 300, 0);
            else drawSprite(21, 26, 130, 14, 50, 28, 400, 0);
        }

        @Override
        protected CachedRecipe createCached(ICondenserRecipe r) {
            return new Cached(r);
        }

        private class Cached extends CachedRecipe {

            final List<PositionedStack> ingredients = new ArrayList<>();
            final PositionedStack result;

            Cached(ICondenserRecipe r) {
                List<List<ItemStack>> inputs = r.getInputs();
                ingredients.add(slot(r.getBottles(), 81, 3));
                if (advanced) {
                    ingredients.add(slot(r.getModifiers(), 42, 3));
                    int[][] pos = { { 3, 55 }, { 3, 31 }, { 3, 7 } };
                    for (int i = 0; i < pos.length && i < inputs.size(); i++) {
                        ingredients.add(slot(inputs.get(i), pos[i][0], pos[i][1]));
                    }
                } else {
                    int[][] pos = { { 3, 43 }, { 3, 19 } };
                    for (int i = 0; i < pos.length && i < inputs.size(); i++) {
                        ingredients.add(slot(inputs.get(i), pos[i][0], pos[i][1]));
                    }
                }
                ingredients.add(tank(r.getFluid(), 110, 24, 16, 32));
                ingredients.removeAll(Collections.singleton(null));
                result = slot(r.getResult(), 81, 31);
            }

            @Override
            public List<PositionedStack> getIngredients() {
                return getCycledIngredients(cycleticks / 20, ingredients);
            }

            @Override
            public PositionedStack getResult() {
                return result;
            }
        }
    }

}
