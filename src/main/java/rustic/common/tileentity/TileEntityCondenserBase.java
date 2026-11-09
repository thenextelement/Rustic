package rustic.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import rustic.common.blocks.BlockCondenserBase;
import rustic.common.blocks.BlockTileBase;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.util.Facing;
import rustic.common.util.FluidHelper;
import rustic.common.util.ItemStackHandlerRustic;
import rustic.core.Rustic;

public abstract class TileEntityCondenserBase extends TileEntityFluidBase implements ISidedInventory, BlockTileBase.IBreakable {

	public static final int SLOT_RESULT = 0;
	public static final int SLOT_FUEL = 1;
	public static final int SLOT_BOTTLE = 2;
	public static final int SLOT_INGREDIENTS_START = 3;

	public static final int CAPACITY = FluidContainerRegistry.BUCKET_VOLUME * 8;

	protected abstract int getInternalSize();

	protected ItemStackHandlerRustic internalStackHandler = new ItemStackHandlerRustic(getInternalSize()) {
		@Override
		protected void onContentsChanged(int slot) {
			hasContentChanged = true;
			sync();
		}
	};

	protected ICondenserRecipe currentRecipe;
	protected boolean hasContentChanged;
	public int condenserBurnTime;
	public int currentItemBurnTime;
	public int brewTime;
	public int totalBrewTime;

	public TileEntityCondenserBase() {
		super(CAPACITY);
		this.currentRecipe = null;
		this.hasContentChanged = true;
	}

	@Override
	protected void onTankChanged() {
		hasContentChanged = true;
		super.onTankChanged();
	}

	public ItemStackHandlerRustic getItemHandler() {
		return internalStackHandler;
	}

	public abstract String getName();

	/** The facing of the condenser block (towards the front). */
	public ForgeDirection getFacing() {
		return BlockCondenserBase.getFacing(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
	}

	@Override
	public void updateEntity() {
		boolean isBrewing = this.canBrew();
		boolean hasRecipe = isBrewing && this.hasValidRecipe();
		if (this.burnFuel(isBrewing && hasRecipe) && isBrewing && hasRecipe) {
			++this.brewTime;
			this.renderParticles();
			if (this.brewTime >= this.currentRecipe.getTime()) {
				this.brewTime = 0;
				if (!worldObj.isRemote) {
					this.brew();
				}
			}
		} else if (this.brewTime > 0) {
			this.brewTime = MathHelper.clamp_int(this.brewTime - 2, 0, this.totalBrewTime);
		}
	}

	protected abstract boolean canBrew();

	protected boolean hasValidRecipe() {
		// short-cut recipe matching if there is no fuel or no retorts
		if (this.condenserBurnTime <= 0 && this.internalStackHandler.getStackInSlot(SLOT_FUEL) == null) {
			return false;
		}
		if (this.hasContentChanged) {
			this.refreshCurrentRecipe();
		}
		if (this.currentRecipe == null) {
			return false;
		}
		if (this.getAmount() < this.currentRecipe.getFluid().amount) {
			return false;
		}
		return this.internalStackHandler.insertItem(SLOT_RESULT, this.currentRecipe.getResult(), true) == null;
	}

	protected abstract void refreshCurrentRecipe();

	private boolean burnFuel(boolean consumeNewFuel) {
		if (this.condenserBurnTime > 0) {
			--this.condenserBurnTime;
			return true;
		}
		ItemStack fuelStack = internalStackHandler.getStackInSlot(SLOT_FUEL);
		if (consumeNewFuel && fuelStack != null) {
			this.condenserBurnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
			this.currentItemBurnTime = this.condenserBurnTime;
			if (!worldObj.isRemote) {
				Item fuelItem = fuelStack.getItem();
				fuelStack.stackSize -= 1;
				if (fuelStack.stackSize <= 0) {
					internalStackHandler.setStackInSlot(SLOT_FUEL, fuelItem.getContainerItem(fuelStack));
				} else {
					internalStackHandler.setStackInSlot(SLOT_FUEL, fuelStack);
				}
			}
			return true;
		}
		return false;
	}

	public boolean isBurning() {
		return this.condenserBurnTime > 0;
	}

	/** Positions (relative to this block) of the retorts that smoke while brewing. */
	protected abstract ForgeDirection[] getRetortDirections(ForgeDirection facing);

	protected void renderParticles() {
		if (!worldObj.isRemote) return;
		if (!(worldObj.getBlock(xCoord, yCoord, zCoord) instanceof BlockCondenserBase)) return;
		for (ForgeDirection dir : getRetortDirections(getFacing())) {
			Rustic.proxy.spawnAlchemySmokeFX(worldObj, this.brewTime, xCoord + 0.5D + dir.offsetX, yCoord + 1.0625D,
					zCoord + 0.5D + dir.offsetZ, 0, 0.125, 0);
		}
	}

	protected abstract void brew();

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("items")) {
			internalStackHandler.deserializeNBT(tag.getCompoundTag("items"));
		}
		this.condenserBurnTime = tag.getInteger("BurnTime");
		this.brewTime = tag.getInteger("BrewTime");
		this.totalBrewTime = tag.getInteger("BrewTimeTotal");
		this.currentItemBurnTime = tag.getInteger("ItemBurnTime");
		this.hasContentChanged = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setTag("items", internalStackHandler.serializeNBT());
		tag.setInteger("BurnTime", (short) this.condenserBurnTime);
		tag.setInteger("BrewTime", (short) this.brewTime);
		tag.setInteger("BrewTimeTotal", (short) this.totalBrewTime);
		tag.setInteger("ItemBurnTime", this.currentItemBurnTime);
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

	public boolean activate(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
			float hitZ) {
		ItemStack heldItem = player.getCurrentEquippedItem();
		if (heldItem != null) {
			if (FluidHelper.isFluidContainer(heldItem)) {
				if (world.isRemote) return FluidHelper.getFluidContained(heldItem) != null || getAmount() > 0;
				if (FluidHelper.interactWithTank(player, tank)) {
					sync();
					return true;
				}
			}
		} else if (player.isSneaking() && this.tank.getFluidAmount() > 0) {
			if (!world.isRemote) {
				this.tank.drainInternal(CAPACITY, true);
				world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "game.neutral.swim.splash", 1F, 1F);
				sync();
			}
			return true;
		}
		return false;
	}

	/* ISidedInventory - external access, like 1.12's ExternalItemHandler + RangedWrappers */

	protected boolean isFormed() {
		return worldObj.getBlock(xCoord, yCoord, zCoord) instanceof BlockCondenserBase
				&& ((BlockCondenserBase) worldObj.getBlock(xCoord, yCoord, zCoord)).hasRetorts(worldObj, xCoord, yCoord, zCoord);
	}

	/** Slots reachable from the given side. */
	protected abstract int[] getSlotsForSide(ForgeDirection side, ForgeDirection facing);

	protected static int[] range(int from, int toExclusive) {
		int[] out = new int[toExclusive - from];
		for (int i = 0; i < out.length; i++) out[i] = from + i;
		return out;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if (!isFormed()) return new int[0];
		return getSlotsForSide(ForgeDirection.getOrientation(side), getFacing());
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == SLOT_RESULT) return false;
		if (slot == SLOT_FUEL) return TileEntityFurnace.isItemFuel(stack);
		if (slot == SLOT_BOTTLE) return !TileEntityFurnace.isItemFuel(stack);
		return true;
	}

	@Override
	public int getSizeInventory() {
		return internalStackHandler.getSlots();
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

	protected static ForgeDirection[] sideRetorts(ForgeDirection facing) {
		return new ForgeDirection[] { Facing.rotateYCCW(facing), Facing.rotateY(facing) };
	}

}
