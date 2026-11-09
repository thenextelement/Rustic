package rustic.common.tileentity;

import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import rustic.common.Config;
import rustic.common.blocks.BlockTileBase;
import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.crafting.IBrewingBarrelRecipe;
import rustic.common.crafting.Recipes;
import rustic.common.util.FluidHelper;
import rustic.common.util.ItemStackHandlerRustic;

public class TileEntityBrewingBarrel extends TileEntityRustic implements ISidedInventory, BlockTileBase.IBreakable {

    public static final int INPUT_IN_SLOT = 0;
    public static final int OUTPUT_IN_SLOT = 1;
    public static final int AUX_IN_SLOT = 2;
    public static final int INPUT_OUT_SLOT = 3;
    public static final int OUTPUT_OUT_SLOT = 4;
    public static final int AUX_OUT_SLOT = 5;

    private static final int[] ALL_SLOTS = { 0, 1, 2, 3, 4, 5 };

    protected ItemStackHandlerRustic internalStackHandler = new ItemStackHandlerRustic(6) {

        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    protected FluidTank input = new FluidTank(8000);
    protected FluidTank output = new FluidTank(8000);
    protected FluidTank auxiliary = new FluidTank(1000);
    protected int brewTime;
    protected IBrewingBarrelRecipe recipe = null;

    public ItemStackHandlerRustic getItemHandler() {
        return internalStackHandler;
    }

    public static boolean isStackAllowedInSlot(int slot, ItemStack stack) {
        if (stack == null) return false;
        if (slot == OUTPUT_IN_SLOT) {
            return stack.getItem() == Items.glass_bottle;
        } else if (slot == AUX_IN_SLOT) {
            FluidStack fluid = FluidHelper.getFluidContained(stack);
            if (fluid != null) return fluid.getFluid() instanceof FluidBooze;
            return stack.getItem() == Items.glass_bottle;
        } else if (slot == INPUT_IN_SLOT) {
            return FluidHelper.isFluidContainer(stack);
        }
        return false;
    }

    private void sync() {
        if (worldObj == null) return;
        worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, 0);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("items", internalStackHandler.serializeNBT());
        NBTTagCompound inputTank = new NBTTagCompound();
        input.writeToNBT(inputTank);
        tag.setTag("inputTank", inputTank);
        NBTTagCompound outputTank = new NBTTagCompound();
        output.writeToNBT(outputTank);
        tag.setTag("outputTank", outputTank);
        NBTTagCompound auxTank = new NBTTagCompound();
        auxiliary.writeToNBT(auxTank);
        tag.setTag("auxTank", auxTank);
        tag.setInteger("brewTime", brewTime);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("items")) internalStackHandler.deserializeNBT(tag.getCompoundTag("items"));
        input.setFluid(null);
        output.setFluid(null);
        auxiliary.setFluid(null);
        input.readFromNBT(tag.getCompoundTag("inputTank"));
        output.readFromNBT(tag.getCompoundTag("outputTank"));
        auxiliary.readFromNBT(tag.getCompoundTag("auxTank"));
        brewTime = tag.getInteger("brewTime");
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    public String getName() {
        return "container.rustic.brewing_barrel";
    }

    public int getInputAmount() {
        return input.getFluidAmount();
    }

    public int getOutputAmount() {
        return output.getFluidAmount();
    }

    public int getAuxiliaryAmount() {
        return auxiliary.getFluidAmount();
    }

    public int getInputCapacity() {
        return input.getCapacity();
    }

    public int getOutputCapacity() {
        return output.getCapacity();
    }

    public int getAuxiliaryCapacity() {
        return auxiliary.getCapacity();
    }

    public FluidStack getInputFluid() {
        return input.getFluid() != null ? input.getFluid()
            .copy() : null;
    }

    public FluidStack getOutputFluid() {
        return output.getFluid() != null ? output.getFluid()
            .copy() : null;
    }

    public FluidStack getAuxiliaryFluid() {
        return auxiliary.getFluid() != null ? auxiliary.getFluid()
            .copy() : null;
    }

    /** Client-side setters used to sync tank contents to the open GUI. */
    public void setInputFluid(FluidStack fluid) {
        input.setFluid(fluid);
    }

    public void setOutputFluid(FluidStack fluid) {
        output.setFluid(fluid);
    }

    public void setAuxiliaryFluid(FluidStack fluid) {
        auxiliary.setFluid(fluid);
    }

    public void setBrewTime(int time) {
        brewTime = time;
    }

    public boolean slot0Empty() {
        return internalStackHandler.getStackInSlot(0) == null;
    }

    public boolean slot1Empty() {
        return internalStackHandler.getStackInSlot(1) == null;
    }

    public boolean slot2Empty() {
        return internalStackHandler.getStackInSlot(2) == null;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z) {
        if (!world.isRemote) {
            for (int i = 0; i < internalStackHandler.getSlots(); i++) {
                spawnAsEntity(world, x, y, z, internalStackHandler.getStackInSlot(i));
                internalStackHandler.getStacks()[i] = null;
            }
        }
    }

    private void consumeInput(int slot) {
        ItemStack stack = internalStackHandler.getStackInSlot(slot);
        stack.stackSize -= 1;
        internalStackHandler.setStackInSlot(slot, stack.stackSize <= 0 ? null : stack);
    }

    protected boolean tryTankIO(FluidTank tank, int inSlot, int outSlot, boolean allowFill, boolean onlyBooze) {
        if (worldObj.isRemote) return false;
        ItemStack stack = itemInSlot(inSlot);
        if (stack == null) return false;
        ItemStack in = stack.copy();
        in.stackSize = 1;

        FluidStack fs = FluidHelper.getFluidContained(in);
        boolean fsEmpty = isFluidStackEmpty(fs);
        boolean tankEmpty = tank.getFluidAmount() <= 0;
        boolean fluidsEqual = fsEmpty ? tankEmpty : fs.isFluidEqual(tank.getFluid());

        // try fill tank from item
        if (allowFill && !fsEmpty
            && (tankEmpty || fluidsEqual)
            && (!onlyBooze || fs.getFluid() instanceof FluidBooze)) {
            if (tank.fill(fs, false) == fs.amount) {
                ItemStack out = FluidHelper.getEmptyContainer(in);
                if (out == null || internalStackHandler.insertItem(outSlot, out, true) == null) {
                    tank.fill(fs, true);
                    consumeInput(inSlot);
                    if (out != null) internalStackHandler.insertItem(outSlot, out, false);
                    return true;
                }
            }
        }

        // try drain tank into item
        if (!tankEmpty && fsEmpty) {
            int[] used = new int[1];
            ItemStack out = FluidHelper.fillContainer(
                in,
                tank.getFluid()
                    .copy(),
                used);
            if (out != null && used[0] > 0
                && tank.getFluidAmount() >= used[0]
                && internalStackHandler.insertItem(outSlot, out, true) == null) {
                tank.drain(used[0], true);
                consumeInput(inSlot);
                internalStackHandler.insertItem(outSlot, out, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        boolean fluidChanged = false;
        fluidChanged = tryTankIO(input, INPUT_IN_SLOT, INPUT_OUT_SLOT, true, false) || fluidChanged;
        fluidChanged = tryTankIO(output, OUTPUT_IN_SLOT, OUTPUT_OUT_SLOT, false, false) || fluidChanged;
        fluidChanged = tryTankIO(auxiliary, AUX_IN_SLOT, AUX_OUT_SLOT, true, true) || fluidChanged;
        if (fluidChanged) {
            brewTime = 0;
            if (recipe != null && !recipe.matches(input.getFluid(), auxiliary.getFluid())) {
                recipe = null;
            }
            sync();
        }
        if (recipe == null && input.getFluidAmount() > 0) {
            recipe = getRecipe();
        }
        if (recipe != null && brewTime % 20 == 0) {
            recipe = getRecipe();
        }
        if (recipe != null) {
            brewTime++;
            if (brewTime >= getMaxBrewTime()) {
                brew();
                brewTime = 0;
                recipe = null;
            }
        } else {
            brewTime = 0;
        }
    }

    private void brew() {
        brewTime = 0;
        int amount = output.getCapacity() - output.getFluidAmount();
        amount = Math.min(amount, input.getFluidAmount());
        if (amount > 0 && recipe.matches(input.getFluid(), auxiliary.getFluid())) {
            FluidStack out = recipe.getResult(input.getFluid(), auxiliary.getFluid());
            if (output.getFluidAmount() > 0 && output.getFluid()
                .getFluid() == out.getFluid()) {
                out = output.getFluid()
                    .copy();
            }
            out.amount = amount;
            FluidStack drainable = input.drain(amount, false);
            if (output.fill(out, false) == amount && drainable != null && drainable.amount == amount) {
                output.fill(out, true);
                input.drain(amount, true);
                recipe = null;
            }
        }
        sync();
    }

    private IBrewingBarrelRecipe getRecipe() {
        if (input.getFluidAmount() > 0) {
            for (IBrewingBarrelRecipe recipe : Recipes.brewingRecipes) {
                if (recipe.matches(input.getFluid(), auxiliary.getFluid())) {
                    if (output.getFluid() == null || (recipe.getResult(input.getFluid(), auxiliary.getFluid())
                        .getFluid()
                        == output.getFluid()
                            .getFluid()
                        && output.getFluidAmount() < output.getCapacity())) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    public ItemStack itemInSlot(int slot) {
        return this.internalStackHandler.getStackInSlot(slot);
    }

    public boolean isBrewing() {
        return brewTime > 0;
    }

    public int getBrewTime() {
        return brewTime;
    }

    public int getMaxBrewTime() {
        return Config.MAX_BREW_TIME;
    }

    static boolean isFluidStackEmpty(FluidStack fs) {
        return fs == null || fs.getFluid() == null || fs.amount <= 0;
    }

    /* ISidedInventory */

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isStackAllowedInSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    @Override
    public int getSizeInventory() {
        return 6;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return internalStackHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return internalStackHandler.extractItem(slot, count, false);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = internalStackHandler.getStackInSlot(slot);
        internalStackHandler.getStacks()[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        internalStackHandler.setStackInSlot(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return getName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isStackAllowedInSlot(slot, stack);
    }

}
