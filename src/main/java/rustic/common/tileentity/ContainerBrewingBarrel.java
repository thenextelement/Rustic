package rustic.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.util.FluidHelper;
import rustic.common.util.ItemStackHandlerRustic;
import rustic.common.util.SlotHandler;

public class ContainerBrewingBarrel extends Container {

    private TileEntityBrewingBarrel te;

    public ContainerBrewingBarrel(IInventory playerInventory, TileEntityBrewingBarrel te) {
        this.te = te;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public TileEntityBrewingBarrel getTile() {
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
        addSlotToContainer(new SlotHandler(itemHandler, 0, 62, 7) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                if (!super.isItemValid(stack)) {
                    return false;
                }
                return TileEntityBrewingBarrel.isStackAllowedInSlot(0, stack);
            }
        });
        addSlotToContainer(new SlotHandler(itemHandler, 1, 116, 7) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                if (!super.isItemValid(stack)) {
                    return false;
                }
                return stack.getItem() == Items.glass_bottle;
            }
        });
        addSlotToContainer(new SlotHandler(itemHandler, 2, 26, 15) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                if (!super.isItemValid(stack)) {
                    return false;
                }
                return TileEntityBrewingBarrel.isStackAllowedInSlot(2, stack);
            }
        });
        addSlotToContainer(new OutputSlot(itemHandler, 3, 62, 63));
        addSlotToContainer(new OutputSlot(itemHandler, 4, 116, 63));
        addSlotToContainer(new OutputSlot(itemHandler, 5, 26, 55));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 6) {
                if (!this.mergeItemStack(itemstack1, 6, 42, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                if (itemstack1.getItem()
                    .equals(Items.glass_bottle)) {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                        if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                            if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                                return null;
                            }
                        }
                    } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                            return null;
                        }
                    } else if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                        return null;
                    }
                } else if (FluidHelper.isFluidContainer(itemstack1)) {
                    FluidStack fluid = FluidHelper.getFluidContained(itemstack1);
                    if (fluid == null || fluid.getFluid() == null) {
                        if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                            if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                                if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                                    return null;
                                }
                            }
                        } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                            if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                                return null;
                            }
                        } else if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                            return null;
                        }
                    } else if (fluid.getFluid() instanceof FluidBooze) {
                        if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                            return null;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                            return null;
                        }
                    }
                } else if (index > 5 && index < 33) {
                    if (!this.mergeItemStack(itemstack1, 33, 42, false)) {
                        return null;
                    }
                } else if (index > 32 && index < 42 && !this.mergeItemStack(itemstack1, 6, 33, false)) {
                    return null;
                }
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

    public class OutputSlot extends SlotHandler {

        public OutputSlot(ItemStackHandlerRustic itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
