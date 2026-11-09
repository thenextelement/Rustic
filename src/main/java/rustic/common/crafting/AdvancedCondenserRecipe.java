package rustic.common.crafting;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fluids.Fluid;

import rustic.common.items.ModItems;
import rustic.common.util.ElixirUtils;

public class AdvancedCondenserRecipe extends CondenserRecipe {

    private ItemStack modifier;

    public AdvancedCondenserRecipe(ItemStack output, ItemStack modifier, ItemStack... inputs) {
        this.output = output;
        this.modifier = modifier;
        this.inputs = Arrays.asList(inputs);
    }

    public AdvancedCondenserRecipe(PotionEffect effect, ItemStack modifier, ItemStack... inputs) {
        this.output = new ItemStack(ModItems.ELIXIR);
        ElixirUtils.addEffect(effect, this.output);
        this.modifier = modifier;
        this.inputs = Arrays.asList(inputs);
    }

    @Override
    public boolean matches(Fluid fluid, ItemStack mod, ItemStack bottle, ItemStack[] stacks) {
        if (((this.modifier != null) && (mod == null)) || ((this.modifier == null) && (mod != null))) {
            return false;
        }
        if (this.modifier == null) {
            return super.matches(fluid, mod, bottle, stacks);
        }
        if (!this.modifier.getItem()
            .equals(mod.getItem())) {
            return false;
        }
        if (this.modifier.getItemDamage() != mod.getItemDamage()) {
            return false;
        }
        if (this.modifier.hasTagCompound() && (mod.hasTagCompound())) {
            if (!this.modifier.getTagCompound()
                .equals(mod.getTagCompound())) {
                return false;
            }
        } else if (this.modifier.hasTagCompound() && !mod.hasTagCompound()) {
            return false;
        }
        return super.matches(fluid, mod, bottle, stacks);
    }

    @Override
    public boolean isBasic() {
        return false;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    public int getTime() {
        return 300;
    }

    public List<ItemStack> getModifiers() {
        return this.modifier == null ? java.util.Collections.<ItemStack>emptyList()
            : Arrays.asList(this.modifier.copy());
    }

}
