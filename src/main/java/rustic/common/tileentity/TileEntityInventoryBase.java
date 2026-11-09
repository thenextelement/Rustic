package rustic.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import rustic.common.blocks.BlockTileBase;
import rustic.common.util.ItemStackHandlerRustic;

/**
 * A tile entity with an item inventory, exposed both as an {@link ItemStackHandlerRustic} (used by Rustic's own
 * containers) and as an {@link ISidedInventory} (for hoppers and pipes).
 */
public abstract class TileEntityInventoryBase extends TileEntityRustic
		implements ISidedInventory, BlockTileBase.INameable, BlockTileBase.IBreakable {

	protected String customName;
	protected final ItemStackHandlerRustic itemStackHandler;
	private int[] allSlots;

	public TileEntityInventoryBase(int size) {
		itemStackHandler = new ItemStackHandlerRustic(size) {
			@Override
			protected void onContentsChanged(int slot) {
				TileEntityInventoryBase.this.onContentsChanged(slot);
			}

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return TileEntityInventoryBase.this.isItemValidForSlot(slot, stack);
			}
		};
	}

	public ItemStackHandlerRustic getItemHandler() {
		return itemStackHandler;
	}

	protected void onContentsChanged(int slot) {
		markDirty();
	}

	/** Name of the inventory for an unnamed instance, e.g. "container.rustic.vase". */
	protected abstract String getDefaultName();

	@Override
	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("items")) {
			itemStackHandler.deserializeNBT(compound.getCompoundTag("items"));
		}
		if (compound.hasKey("CustomName", 8)) {
			customName = compound.getString("CustomName");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("items", itemStackHandler.serializeNBT());
		if (hasCustomInventoryName()) {
			compound.setString("CustomName", customName);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z) {
		if (!world.isRemote) {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				spawnAsEntity(world, x, y, z, itemStackHandler.getStackInSlot(i));
				itemStackHandler.getStacks()[i] = null;
			}
		}
	}

	/* IInventory */

	@Override
	public int getSizeInventory() {
		return itemStackHandler.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return itemStackHandler.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return itemStackHandler.extractItem(index, count, false);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		ItemStack stack = itemStackHandler.getStackInSlot(index);
		itemStackHandler.getStacks()[index] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		itemStackHandler.setStackInSlot(index, stack);
	}

	@Override
	public String getInventoryName() {
		return hasCustomInventoryName() ? customName : getDefaultName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return customName != null && !customName.isEmpty();
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
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	/* ISidedInventory */

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if (allSlots == null || allSlots.length != getSizeInventory()) {
			allSlots = new int[getSizeInventory()];
			for (int i = 0; i < allSlots.length; i++) allSlots[i] = i;
		}
		return allSlots;
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
	public boolean isUseableByPlayer(EntityPlayer player) {
		return super.isUseableByPlayer(player);
	}

}
