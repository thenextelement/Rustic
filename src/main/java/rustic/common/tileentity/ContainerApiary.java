package rustic.common.tileentity;

import rustic.common.util.ItemStackHandlerRustic;
import rustic.common.util.SlotHandler;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import rustic.common.items.ModItems;

public class ContainerApiary extends Container {

	private TileEntityApiary te;

	public ContainerApiary(IInventory playerInventory, TileEntityApiary te) {
		this.te = te;
		addOwnSlots();
		addPlayerSlots(playerInventory);
	}
	
	public TileEntityApiary getTile() {
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
		ItemStackHandlerRustic itemHandler = this.te.getBeeHandler();
		addSlotToContainer(new SlotHandler(itemHandler, 0, 80, 26) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem().equals(ModItems.BEE);
			}
		});
		itemHandler = this.te.getHoneycombHandler();
		addSlotToContainer(new SlotHandler(itemHandler, 0, 80, 44) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem().equals(ModItems.HONEYCOMB);
			}
		});
	}

		@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 1 && index != 0) {
                if (itemstack1.getItem().equals(ModItems.BEE)) {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return null;
                    }
                } else if (itemstack1.getItem().equals(ModItems.HONEYCOMB)) {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                        return null;
                    }
                } else if (index >= 2 && index < 29) {
                    if (!this.mergeItemStack(itemstack1, 29, 38, false)) {
                        return null;
                    }
                } else if (index >= 29 && index < 38 && !this.mergeItemStack(itemstack1, 2, 29, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 2, 38, false)) {
                return null;
            }
            
            if ((itemstack1 == null || itemstack1.stackSize <= 0)) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }
        
		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return te.canInteractWith(playerIn);
	}

}
