package rustic.common.crafting;

import rustic.common.util.FluidHelper;
import rustic.common.util.RusticUtils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.Config;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.items.ModItems;

public class RecipeOliveOil implements IRecipe {
	
	private int foodSlot = 0;

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int numStacks = 0;
		List<Integer> occupiedSlots = new ArrayList<Integer>();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (!(inv.getStackInSlot(i) == null)) {
				numStacks++;
				occupiedSlots.add(i);
			}
		}
		if (numStacks != 2) {
			return false;
		}
		ItemStack foodStack = null;
		ItemStack oilStack = null;
		for (int i : occupiedSlots) {
			ItemStack tempStack = inv.getStackInSlot(i);
			if (tempStack.getItem() instanceof ItemFood && (foodStack == null)) {
				//boolean flag = false;
				String itemName = RusticUtils.getRegistryName(tempStack.getItem());
				if (Config.OLIVE_OIL_USE_WHITELIST != Config.OLIVE_OIL_BLACKLIST.contains(itemName)) {  // item not found in list
					return false;
				}
				foodStack = tempStack;
				this.foodSlot = i;
			} else if (tempStack.getItem().equals(ModItems.FLUID_BOTTLE) && (oilStack == null)) {
				FluidStack fluidStack = FluidHelper.getFluidContained(tempStack);
				if (fluidStack != null && fluidStack.getFluid() != null) {
					if (fluidStack.getFluid().equals(ModFluids.OLIVE_OIL)) {
						oilStack = tempStack;
					}
				}
			} else {
				return false;
			}
		}
		if (foodStack == null || oilStack == null) {
			return false;
		}
		if (foodStack.hasTagCompound() && foodStack.getTagCompound().hasKey("oiled")) {
			return false;
		}

		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack foodStack = inv.getStackInSlot(foodSlot);
		if ((foodStack == null) || !(foodStack.getItem() instanceof ItemFood)) {
			return null;
		}
		ItemStack returnStack = new ItemStack(foodStack.getItem(), 1, foodStack.getItemDamage());
		NBTTagCompound amendedTag = foodStack.getTagCompound();
		if (amendedTag == null) {
		    amendedTag = new NBTTagCompound();
		} else {
		    amendedTag = (NBTTagCompound) amendedTag.copy();
		}
		amendedTag.setTag("oiled", new NBTTagByte((byte) 0));
		returnStack.setTagCompound(amendedTag);
		return returnStack;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}


	@Override
	public int getRecipeSize() {
		return 9;
	}
	

}
