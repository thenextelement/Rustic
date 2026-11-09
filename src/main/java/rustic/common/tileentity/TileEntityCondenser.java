package rustic.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import rustic.common.blocks.BlockCondenserBase;
import rustic.common.blocks.ModBlocks;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.crafting.Recipes;

public class TileEntityCondenser extends TileEntityCondenserBase {

	public static int SLOT_NUM = 5;

	@Override
	protected int getInternalSize() {
		return SLOT_NUM;
	}

	@Override
	public String getName() {
		return "container.rustic.condenser";
	}

	@Override
	protected int[] getSlotsForSide(ForgeDirection side, ForgeDirection facing) {
		if (side == ForgeDirection.DOWN) return range(SLOT_RESULT, SLOT_RESULT + 1); // result
		if (side == facing.getOpposite()) return range(SLOT_FUEL, SLOT_BOTTLE + 1); // fuel + bottle
		if (side.offsetY == 0) return range(SLOT_INGREDIENTS_START, SLOT_INGREDIENTS_START + 2); // ingredients
		return range(0, SLOT_NUM);
	}

	@Override
	protected ForgeDirection[] getRetortDirections(ForgeDirection facing) {
		return sideRetorts(facing);
	}

	private ItemStack[] inputs() {
		return new ItemStack[] { internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START),
				internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 1) };
	}

	@Override
	protected boolean canBrew() {
		if (worldObj.getBlock(xCoord, yCoord, zCoord) != ModBlocks.CONDENSER
				|| !BlockCondenserBase.isBottom(worldObj.getBlockMetadata(xCoord, yCoord, zCoord))) {
			return false;
		}
		if (internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START) == null
				&& internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 1) == null) {
			return false;
		}
		return ModBlocks.CONDENSER.hasRetorts(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	protected void refreshCurrentRecipe() {
		if (!hasContentChanged) return;
		hasContentChanged = false;
		Fluid fluid = getFluid();
		ItemStack bottle = internalStackHandler.getStackInSlot(SLOT_BOTTLE);
		ItemStack[] inputs = inputs();
		if (currentRecipe != null && currentRecipe.matches(fluid, null, bottle, inputs)) return;
		brewTime = 0;
		currentRecipe = null;
		for (ICondenserRecipe recipe : Recipes.condenserRecipes) {
			if (!recipe.isBasic()) continue;
			if (recipe.matches(fluid, null, bottle, inputs)) {
				currentRecipe = recipe;
				totalBrewTime = recipe.getTime();
				return;
			}
		}
	}

	@Override
	protected void brew() {
		if (!canBrew()) return;
		ItemStack[] inputs = inputs();
		ItemStack bottle = internalStackHandler.getStackInSlot(SLOT_BOTTLE);
		ICondenserRecipe recipe = currentRecipe;
		if (recipe.matches(getFluid(), null, bottle, inputs) && getAmount() >= recipe.getFluid().amount) {
			internalStackHandler.insertItem(SLOT_RESULT, recipe.getResult(), false);
			int[] consume = recipe.getInputConsumption(inputs);
			internalStackHandler.extractItem(SLOT_INGREDIENTS_START, consume[0], false);
			internalStackHandler.extractItem(SLOT_INGREDIENTS_START + 1, consume[1], false);
			internalStackHandler.extractItem(SLOT_BOTTLE, recipe.getBottleConsumption(bottle), false);
			tank.drainInternal(recipe.getFluid().amount, true);
		}
	}

}
