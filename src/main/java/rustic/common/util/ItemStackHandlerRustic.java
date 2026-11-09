package rustic.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * A 1.7.10 stand-in for Forge's 1.12 {@code ItemStackHandler}, with the same method names so the tile entities could
 * be ported with few changes. Empty slots hold {@code null}.
 */
public class ItemStackHandlerRustic {

    protected ItemStack[] stacks;

    public ItemStackHandlerRustic(int size) {
        stacks = new ItemStack[size];
    }

    public ItemStack[] getStacks() {
        return stacks;
    }

    public int getSlots() {
        return stacks.length;
    }

    public void setSize(int size) {
        stacks = new ItemStack[size];
    }

    public ItemStack getStackInSlot(int slot) {
        return stacks[slot];
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        stacks[slot] = (stack == null || stack.stackSize <= 0) ? null : stack;
        onContentsChanged(slot);
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public static boolean canStack(ItemStack a, ItemStack b) {
        return a != null && b != null
            && a.getItem() == b.getItem()
            && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    /** Returns what could not be inserted (null if everything fit). */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return null;
        if (!isItemValid(slot, stack)) return stack;
        ItemStack existing = stacks[slot];
        int limit = getStackLimit(slot, stack);
        if (existing != null) {
            if (!canStack(stack, existing)) return stack;
            limit -= existing.stackSize;
        }
        if (limit <= 0) return stack;
        boolean reachedLimit = stack.stackSize > limit;
        if (!simulate) {
            if (existing == null) {
                ItemStack copy = stack.copy();
                copy.stackSize = reachedLimit ? limit : stack.stackSize;
                stacks[slot] = copy;
            } else {
                existing.stackSize += reachedLimit ? limit : stack.stackSize;
            }
            onContentsChanged(slot);
        }
        if (!reachedLimit) return null;
        ItemStack rest = stack.copy();
        rest.stackSize = stack.stackSize - limit;
        return rest;
    }

    /** Returns what was extracted (null if nothing). */
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return null;
        ItemStack existing = stacks[slot];
        if (existing == null) return null;
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.stackSize <= toExtract) {
            if (!simulate) {
                stacks[slot] = null;
                onContentsChanged(slot);
            }
            return existing;
        }
        if (!simulate) {
            existing.stackSize -= toExtract;
            onContentsChanged(slot);
        }
        ItemStack out = existing.copy();
        out.stackSize = toExtract;
        return out;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                stacks[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", list);
        nbt.setInteger("Size", stacks.length);
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        setSize(nbt.hasKey("Size", 3) ? nbt.getInteger("Size") : stacks.length);
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (slot >= 0 && slot < stacks.length) {
                stacks[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        onLoad();
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}

}
