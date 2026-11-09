package rustic.common.tileentity;

import rustic.common.util.ItemStackHandlerRustic;
import rustic.common.util.SlotHandler;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBarrel extends Container {

	private TileEntityBarrel te;
	
	public ContainerBarrel(IInventory playerInventory, TileEntityBarrel te) {
		this.te = te;
		addOwnSlots();
		addPlayerSlots(playerInventory);
	}
	
	public TileEntityBarrel getTile() {
		return te;
	}

	private void addPlayerSlots(IInventory playerInventory) {
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				int x = 8 + col * 18;
				int y = row * 18 + 84;
				this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
			}
		}

		for (int row = 0; row < 9; ++row) {
			int x = 8 + row * 18;
			int y = 142;
			this.addSlotToContainer(new Slot(playerInventory, row, x, y));
		}
	}

	private void addOwnSlots() {
		ItemStackHandlerRustic itemHandler = this.te.getItemHandler();
		int slotIndex = 0;
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				int x = 8 + col * 18;
				int y = row * 18 + 17;
				this.addSlotToContainer(new SlotHandler(itemHandler, slotIndex, x, y));
				slotIndex++;
			}
		}

	}

		@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 3 * 9)
            {
                if (!this.mergeItemStack(itemstack1, 3 * 9, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 3 * 9, false))
            {
                return null;
            }

            if ((itemstack1 == null || itemstack1.stackSize <= 0))
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return te.canInteractWith(playerIn);
	}

}
