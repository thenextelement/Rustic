package rustic.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/** A tile entity with a single fluid tank, exposed on all sides. */
public abstract class TileEntityFluidBase extends TileEntityRustic implements IFluidHandler {

	protected RusticTank tank;

	public TileEntityFluidBase(int capacity) {
		tank = new RusticTank(capacity) {
			@Override
			protected void onContentsChanged() {
				TileEntityFluidBase.this.onTankChanged();
			}
		};
	}

	protected void onTankChanged() {
		markDirty();
		if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	/** Full client resync plus a neighbour update (used after interacting). */
	public void sync() {
		if (worldObj == null) return;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, 0);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		markDirty();
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	public RusticTank getTank() {
		return tank;
	}

	public int getCapacity() {
		return tank.getCapacity();
	}

	public int getAmount() {
		return tank.getFluidAmount();
	}

	public Fluid getFluid() {
		return tank.getFluid() != null ? tank.getFluid().getFluid() : null;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		tank.setFluid(null);
		tank.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tank.writeToNBT(tag);
	}

	/* IFluidHandler */

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || !resource.isFluidEqual(tank.getFluid())) return null;
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return tank.canFill() && (tank.getFluid() == null || tank.getFluid().getFluid() == fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return tank.canDrain() && tank.getFluid() != null && (fluid == null || tank.getFluid().getFluid() == fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] { tank.getInfo() };
	}

}
