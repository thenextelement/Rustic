package rustic.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** A container slot backed by an {@link ItemStackHandlerRustic} (like Forge's 1.12 SlotItemHandler). */
public class SlotHandler extends Slot {

    private static final IInventory EMPTY = new InventoryBasic("[Null]", true, 0);

    protected final ItemStackHandlerRustic handler;
    protected final int index;

    public SlotHandler(ItemStackHandlerRustic handler, int index, int x, int y) {
        super(EMPTY, index, x, y);
        this.handler = handler;
        this.index = index;
    }

    public ItemStackHandlerRustic getItemHandler() {
        return handler;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return false;
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        return handler.isItemValid(index, copy);
    }

    @Override
    public ItemStack getStack() {
        return handler.getStackInSlot(index);
    }

    @Override
    public void putStack(ItemStack stack) {
        handler.setStackInSlot(index, stack);
        onSlotChanged();
    }

    @Override
    public void onSlotChange(ItemStack a, ItemStack b) {}

    @Override
    public int getSlotStackLimit() {
        return handler.getSlotLimit(index);
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        ItemStack existing = handler.getStackInSlot(index);
        if (existing == null) return null;
        int take = Math.min(amount, existing.stackSize);
        ItemStack out = existing.splitStack(take);
        if (existing.stackSize <= 0) handler.setStackInSlot(index, null);
        else handler.setStackInSlot(index, existing);
        return out;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isSlotInInventory(IInventory inv, int slot) {
        return false;
    }

}
