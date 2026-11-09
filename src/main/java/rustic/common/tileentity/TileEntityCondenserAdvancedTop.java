package rustic.common.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/** The top half of the advanced condenser: forwards inventory and fluid access to the bottom half. */
public class TileEntityCondenserAdvancedTop extends TileEntity implements ISidedInventory, IFluidHandler {

    private TileEntityCondenserAdvancedBottom bottom() {
        if (worldObj == null) return null;
        TileEntity tile = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        return tile instanceof TileEntityCondenserAdvancedBottom ? (TileEntityCondenserAdvancedBottom) tile : null;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? new int[0] : b.getAccessibleSlotsFromSide(side);
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b != null && b.canInsertItem(slot, stack, side);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b != null && b.canExtractItem(slot, stack, side);
    }

    @Override
    public int getSizeInventory() {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? 0 : b.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? null : b.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? null : b.decrStackSize(slot, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        TileEntityCondenserAdvancedBottom b = bottom();
        if (b != null) b.setInventorySlotContents(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return "container.rustic.condenser_advanced";
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
    public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b != null && b.isItemValidForSlot(slot, stack);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? 0 : b.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? null : b.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? null : b.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b != null && b.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b != null && b.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        TileEntityCondenserAdvancedBottom b = bottom();
        return b == null ? new FluidTankInfo[0] : b.getTankInfo(from);
    }

}
