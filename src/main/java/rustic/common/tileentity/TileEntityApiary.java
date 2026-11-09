package rustic.common.tileentity;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import rustic.common.Config;
import rustic.common.blocks.BlockTileBase;
import rustic.common.items.ModItems;
import rustic.common.util.ItemStackHandlerRustic;

/** Slot 0 = bees (insert from any side), slot 1 = honeycomb (extract from the bottom). */
public class TileEntityApiary extends TileEntityRustic implements ISidedInventory, BlockTileBase.IBreakable {

    private final ItemStackHandlerRustic beeItemStackHandler = new ItemStackHandlerRustic(1) {

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack != null && stack.getItem() == ModItems.BEE;
        }
    };

    private final ItemStackHandlerRustic honeyCombItemStackHandler = new ItemStackHandlerRustic(1) {

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack != null && stack.getItem() == ModItems.HONEYCOMB;
        }
    };

    private int reproductionTime = 1200;
    private int reproductionTimer = 0;
    private int productionTime = 600;
    private int productionTimer = 0;
    private final Random random = new Random();

    public ItemStackHandlerRustic getBeeHandler() {
        return beeItemStackHandler;
    }

    public ItemStackHandlerRustic getHoneycombHandler() {
        return honeyCombItemStackHandler;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("bees")) beeItemStackHandler.deserializeNBT(compound.getCompoundTag("bees"));
        if (compound.hasKey("honeyComb"))
            honeyCombItemStackHandler.deserializeNBT(compound.getCompoundTag("honeyComb"));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("bees", beeItemStackHandler.serializeNBT());
        compound.setTag("honeyComb", honeyCombItemStackHandler.serializeNBT());
    }

    @Override
    public void breakBlock(World world, int x, int y, int z) {
        if (!world.isRemote) {
            spawnAsEntity(world, x, y, z, beeItemStackHandler.getStackInSlot(0));
            spawnAsEntity(world, x, y, z, honeyCombItemStackHandler.getStackInSlot(0));
            beeItemStackHandler.getStacks()[0] = null;
            honeyCombItemStackHandler.getStacks()[0] = null;
        }
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        ItemStack bees = beeItemStackHandler.getStackInSlot(0);
        ItemStack combs = honeyCombItemStackHandler.getStackInSlot(0);
        int numBees = bees != null && bees.getItem() == ModItems.BEE ? bees.stackSize : 0;
        int numHoneycomb = combs != null && combs.getItem() == ModItems.HONEYCOMB ? combs.stackSize : 0;
        if (numBees <= 0) return;

        reproductionTime = (int) (Config.BEE_REPRODUCTION_MULTIPLIER * (1600F / ((numBees / 20F) + 1F)));
        productionTime = (int) (Config.BEE_HONEYCOMB_MULTIPLIER * (800F / ((numBees / 20F) + 1F)));
        reproductionTimer++;
        productionTimer++;
        if (reproductionTimer >= reproductionTime) {
            reproductionTimer = 0;
            if (numBees < 64) bees.stackSize += 1;
        }
        if (productionTimer >= productionTime) {
            productionTimer = 0;
            if (numHoneycomb == 0) {
                honeyCombItemStackHandler.setStackInSlot(0, new ItemStack(ModItems.HONEYCOMB, 1));
            } else if (numHoneycomb < 64) {
                combs.stackSize += 1;
            }
        }
        if (Config.BEE_GROWTH_MULTIPLIER != 0
            && random.nextInt((int) Math.ceil(2048F / (numBees * Config.BEE_GROWTH_MULTIPLIER))) == 0) {
            int x = xCoord + random.nextInt(9) - 4;
            int z = zCoord + random.nextInt(9) - 4;
            for (int i = 0; i < 3; i++) {
                int y = yCoord + 1 - i;
                Block block = worldObj.getBlock(x, y, z);
                if (block instanceof IGrowable || block instanceof IPlantable) {
                    block.updateTick(worldObj, x, y, z, worldObj.rand);
                    break;
                }
            }
        }
        markDirty();
    }

    /* ISidedInventory: slot 0 = bees, slot 1 = honeycomb */

    private ItemStackHandlerRustic handlerFor(int slot) {
        return slot == 0 ? beeItemStackHandler : honeyCombItemStackHandler;
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handlerFor(slot).getStackInSlot(0);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return handlerFor(slot).extractItem(0, count, false);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        handlerFor(slot).getStacks()[0] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        handlerFor(slot).setStackInSlot(0, stack);
    }

    @Override
    public String getInventoryName() {
        return "container.rustic.apiary";
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
        return handlerFor(slot).isItemValid(0, stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side == 0 ? new int[] { 1 } : new int[] { 0 };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot == 0 && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == 1 && side == 0;
    }

}
