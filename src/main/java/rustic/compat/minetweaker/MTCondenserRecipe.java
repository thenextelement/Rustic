package rustic.compat.minetweaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import rustic.common.crafting.ICondenserRecipe;

/** Condenser recipe defined by a MineTweaker script (port of the 1.12 CrTCondenserRecipe). */
public class MTCondenserRecipe implements ICondenserRecipe {

    private final int time;
    private final ItemStack output;
    private final FluidStack fluid;
    private final IIngredient bottle;
    private final IIngredient modifier;
    private final List<IIngredient> inputs = new ArrayList<>();

    public MTCondenserRecipe(ItemStack output, IIngredient[] inputs, IIngredient modifier, IIngredient bottle,
        FluidStack fluid, int time) {
        this.output = output;
        this.fluid = fluid;
        this.bottle = bottle;
        this.modifier = modifier;
        for (IIngredient ing : inputs) if (ing != null) this.inputs.add(ing);
        this.time = Math.max(0, time);
    }

    private static IItemStack wrap(ItemStack stack) {
        return stack == null ? null : MineTweakerMC.getIItemStack(stack);
    }

    @Override
    public boolean matches(Fluid fluid, ItemStack modifier, ItemStack bottle, ItemStack[] inputs) {
        if (this.fluid == null || fluid != this.fluid.getFluid()) return false;
        if (this.modifier != null ? (modifier == null || !this.modifier.matches(wrap(modifier))) : modifier != null) {
            return false;
        }
        if (this.bottle != null && (bottle == null || !this.bottle.matches(wrap(bottle)))) return false;

        List<IIngredient> remaining = new ArrayList<>(this.inputs);
        for (ItemStack stack : inputs) {
            if (stack == null) continue;
            IItemStack input = wrap(stack);
            IIngredient found = null;
            for (IIngredient in : remaining) {
                if (in.matches(input)) {
                    found = in;
                    break;
                }
            }
            if (found == null) return false;
            remaining.remove(found);
        }
        return remaining.isEmpty();
    }

    @Override
    public boolean isBasic() {
        return modifier == null && inputs.size() <= 2;
    }

    @Override
    public boolean isAdvanced() {
        return inputs.size() <= 3;
    }

    @Override
    public FluidStack getFluid() {
        return fluid;
    }

    private static List<ItemStack> examples(IIngredient ing) {
        List<ItemStack> list = new ArrayList<>();
        if (ing == null || ing.getItems() == null) return list;
        for (IItemStack s : ing.getItems()) list.add(MineTweakerMC.getItemStack(s));
        return list;
    }

    @Override
    public List<ItemStack> getModifiers() {
        return examples(modifier);
    }

    @Override
    public List<ItemStack> getBottles() {
        return bottle == null ? Collections.<ItemStack>emptyList() : examples(bottle);
    }

    @Override
    public List<List<ItemStack>> getInputs() {
        List<List<ItemStack>> list = new ArrayList<>();
        for (IIngredient in : inputs) list.add(examples(in));
        return list;
    }

    @Override
    public int getTime() {
        return time;
    }

    private static int amountFor(IIngredient ing, ItemStack stack, int fallback) {
        if (ing == null || stack == null) return fallback;
        IItemStack wrapped = wrap(stack);
        if (ing.matches(wrapped)) return Math.max(1, ing.getAmount());
        return fallback;
    }

    @Override
    public int getModifierConsumption(ItemStack modifier) {
        return modifier == null ? 0 : amountFor(this.modifier, modifier, 0);
    }

    @Override
    public int getBottleConsumption(ItemStack bottle) {
        if (this.bottle == null) return 0;
        return amountFor(this.bottle, bottle, 1);
    }

    @Override
    public int[] getInputConsumption(ItemStack[] stacks) {
        int[] consume = new int[stacks.length];
        List<IIngredient> remaining = new ArrayList<>(inputs);
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == null) continue;
            IItemStack current = wrap(stacks[i]);
            for (IIngredient in : remaining) {
                if (in.matches(current)) {
                    consume[i] = Math.max(1, in.getAmount());
                    remaining.remove(in);
                    break;
                }
            }
        }
        return consume;
    }

    @Override
    public ItemStack getResult() {
        return output.copy();
    }

}
