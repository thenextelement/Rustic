package rustic.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import rustic.common.blocks.BlockCondenserBase;
import rustic.common.blocks.ModBlocks;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.crafting.Recipes;

public class TileEntityCondenserAdvancedBottom extends TileEntityCondenserBase {

    public static final int SLOT_NUM = 7;

    @Override
    protected int getInternalSize() {
        return SLOT_NUM;
    }

    @Override
    public String getName() {
        return "container.rustic.condenser_advanced";
    }

    @Override
    protected int[] getSlotsForSide(ForgeDirection side, ForgeDirection facing) {
        if (side == ForgeDirection.DOWN) return range(SLOT_RESULT, SLOT_RESULT + 1); // result
        if (side == ForgeDirection.UP) return range(SLOT_INGREDIENTS_START, SLOT_INGREDIENTS_START + 1); // modifier
        if (side == facing.getOpposite()) return range(SLOT_FUEL, SLOT_BOTTLE + 1); // fuel + bottles
        return range(SLOT_INGREDIENTS_START + 1, SLOT_INGREDIENTS_START + 4); // ingredients
    }

    @Override
    protected ForgeDirection[] getRetortDirections(ForgeDirection facing) {
        ForgeDirection[] sides = sideRetorts(facing);
        return new ForgeDirection[] { sides[0], sides[1], facing.getOpposite() };
    }

    private ItemStack[] inputs() {
        return new ItemStack[] { internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 1),
            internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 2),
            internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 3) };
    }

    @Override
    protected boolean canBrew() {
        if (worldObj.getBlock(xCoord, yCoord, zCoord) != ModBlocks.CONDENSER_ADVANCED
            || !BlockCondenserBase.isBottom(worldObj.getBlockMetadata(xCoord, yCoord, zCoord))) {
            return false;
        }
        if (internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 1) == null
            && internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 2) == null
            && internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START + 3) == null) {
            return false;
        }
        return ModBlocks.CONDENSER_ADVANCED.hasRetorts(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    protected void refreshCurrentRecipe() {
        if (!hasContentChanged) return;
        hasContentChanged = false;
        Fluid fluid = getFluid();
        ItemStack modifier = internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START);
        ItemStack bottle = internalStackHandler.getStackInSlot(SLOT_BOTTLE);
        ItemStack[] inputs = inputs();
        if (currentRecipe != null && currentRecipe.matches(fluid, modifier, bottle, inputs)) return;
        brewTime = 0;
        currentRecipe = null;
        for (ICondenserRecipe recipe : Recipes.condenserRecipes) {
            if (!recipe.isBasic() && !recipe.isAdvanced()) continue;
            if (recipe.matches(fluid, modifier, bottle, inputs)) {
                currentRecipe = recipe;
                totalBrewTime = recipe.getTime();
                return;
            }
        }
    }

    @Override
    protected void brew() {
        if (!canBrew()) return;
        Fluid fluid = getFluid();
        ItemStack modifier = internalStackHandler.getStackInSlot(SLOT_INGREDIENTS_START);
        ItemStack bottle = internalStackHandler.getStackInSlot(SLOT_BOTTLE);
        ItemStack[] inputs = inputs();
        ICondenserRecipe recipe = currentRecipe;
        if (recipe.matches(fluid, modifier, bottle, inputs) && getAmount() >= recipe.getFluid().amount) {
            internalStackHandler.insertItem(SLOT_RESULT, recipe.getResult(), false);
            internalStackHandler.extractItem(SLOT_INGREDIENTS_START, recipe.getModifierConsumption(modifier), false);
            int[] consume = recipe.getInputConsumption(inputs);
            internalStackHandler.extractItem(SLOT_INGREDIENTS_START + 1, consume[0], false);
            internalStackHandler.extractItem(SLOT_INGREDIENTS_START + 2, consume[1], false);
            internalStackHandler.extractItem(SLOT_INGREDIENTS_START + 3, consume[2], false);
            internalStackHandler.extractItem(SLOT_BOTTLE, recipe.getBottleConsumption(bottle), false);
            tank.drainInternal(recipe.getFluid().amount, true);
        }
    }

}
