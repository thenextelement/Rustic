package rustic.common.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import rustic.common.blocks.ModBlocks;
import rustic.common.util.RusticUtils;

public class RecipeCabinet implements IRecipe {

    protected boolean hasOre(ItemStack stack, int ore) {
        if ((stack == null)) return false;

        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            if (oreId == ore) return true;
        }
        return false;
    }

    private boolean isTrapdoor(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        if (item == Item.getItemFromBlock(Blocks.trapdoor)) return true;

        int trapdoorWood = OreDictionary.getOreID("trapdoorWood");
        return hasOre(stack, trapdoorWood);
    }

    protected boolean isWood(ItemStack stack) {
        if (stack == null) return false;
        if (hasOre(stack, OreDictionary.getOreID("plankWood"))) return true;
        if (RusticUtils.getRegistryName(stack.getItem())
            .equals("embers:sealed_planks")) return true;
        if (RusticUtils.getRegistryName(stack.getItem())
            .equals("botania:livingwood") && stack.getItemDamage() == 1) return true;
        if (RusticUtils.getRegistryName(stack.getItem())
            .equals("botania:dreamwood") && stack.getItemDamage() == 1) return true;
        if (RusticUtils.getRegistryName(stack.getItem())
            .equals("astralsorcery:blockinfusedwood") && stack.getItemDamage() == 1) return true;
        if (hasOre(stack, OreDictionary.getOreID("plankTreatedWood"))) return true;

        return false;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack stack = null;
        for (int y = 0; y < 3; y += 2) {
            for (int x = 0; x < 3; x++) {
                stack = inv.getStackInRowAndColumn(x, y);
                if (!isWood(stack)) return false;
            }
        }
        stack = inv.getStackInRowAndColumn(1, 1);
        if ((stack != null)) return false;

        // int trapdoorWood = OreDictionary.getOreID("trapdoorWood");
        ItemStack stack2 = inv.getStackInRowAndColumn(2, 1);
        stack = inv.getStackInRowAndColumn(0, 1);
        return (isWood(stack) && isTrapdoor(stack2)) || (isWood(stack2) && isTrapdoor(stack));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack result = getRecipeOutput();
        ItemStack material = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (inv.getStackInSlot(i) == null) continue;
            ItemStack stack = inv.getStackInSlot(i)
                .copy();
            stack.stackSize = 1;

            if (isWood(stack)) {
                if ((material != null) && !ItemStack.areItemStacksEqual(material, stack)) {
                    material = null;
                    break;
                }
                material = stack;
            }
        }

        NBTTagCompound tag = new NBTTagCompound();
        if (material != null) tag.setTag("material", material.writeToNBT(new NBTTagCompound()));
        result.setTagCompound(tag);

        return result;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModBlocks.CABINET);
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

}
