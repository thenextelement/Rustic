package rustic.common.tileentity;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import rustic.common.blocks.BlockTileBase;
import rustic.common.crafting.ICrushingTubRecipe;
import rustic.common.crafting.Recipes;
import rustic.common.util.FluidHelper;
import rustic.common.util.ItemStackHandlerRustic;

public class TileEntityCrushingTub extends TileEntityFluidBase implements BlockTileBase.IBreakable {

    public static int capacity = FluidContainerRegistry.BUCKET_VOLUME * 8;

    public ItemStackHandlerRustic itemStackHandler = new ItemStackHandlerRustic(1) {

        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    public TileEntityCrushingTub() {
        super(capacity);
        tank.setCanFill(false);
        tank.setCanDrain(true);
    }

    public ItemStackHandlerRustic getItemHandler() {
        return itemStackHandler;
    }

    public void crush(EntityLivingBase entity) {
        ItemStack stack = itemStackHandler.getStackInSlot(0);
        if (stack == null) return;
        for (ICrushingTubRecipe recipe : Recipes.crushingTubRecipes) {
            if (!recipe.matches(stack)) continue;
            FluidStack output = recipe.getResult();
            if (this.getAmount() > this.getCapacity() - output.amount) continue;
            if (!worldObj.isRemote) {
                tank.fillInternal(output, true);
                itemStackHandler.extractItem(0, 1, false);
                ItemStack by = recipe.getByproduct();
                if (by != null) spawnAsEntity(worldObj, xCoord, yCoord, zCoord, by.copy());
                worldObj.playSoundEffect(
                    xCoord + 0.5,
                    yCoord + 0.5,
                    zCoord + 0.5,
                    "mob.slime.small",
                    0.5F,
                    worldObj.rand.nextFloat() * 0.1F + 0.9F);
                sync();
            } else {
                Random rand = worldObj.rand;
                String particle = "iconcrack_" + Item.getIdFromItem(stack.getItem())
                    + (stack.getHasSubtypes() ? "_" + stack.getItemDamage() : "");
                double spread = stack.getHasSubtypes() ? 0.1 : 0.2;
                for (int i = 0; i < rand.nextInt(8) + 8; ++i) {
                    worldObj.spawnParticle(
                        particle,
                        xCoord + 0.5,
                        yCoord + 0.5,
                        zCoord + 0.5,
                        (rand.nextDouble() * spread) - spread / 2,
                        (rand.nextDouble() * spread / 2) + 0.05,
                        (rand.nextDouble() * spread) - spread / 2);
                }
            }
            return;
        }
    }

    public boolean activate(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem != null) {
            if (FluidHelper.isFluidContainer(heldItem) && FluidHelper.getFluidContained(heldItem) == null) {
                if (world.isRemote) return true;
                // containers drain the tub (tank only allows external draining)
                if (FluidHelper.interactWithTank(player, tank)) {
                    sync();
                    return true;
                }
            } else {
                if (!world.isRemote) {
                    player.inventory.setInventorySlotContents(
                        player.inventory.currentItem,
                        itemStackHandler.insertItem(0, heldItem, false));
                    sync();
                }
                return true;
            }
        } else if (player.isSneaking() && this.getAmount() > 0) {
            if (!world.isRemote) {
                tank.drainInternal(capacity, true);
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "game.neutral.swim.splash", 1F, 1F);
                sync();
            }
            return true;
        }
        if (itemStackHandler.getStackInSlot(0) != null) {
            if (!world.isRemote) {
                world.spawnEntityInWorld(
                    new EntityItem(world, player.posX, player.posY, player.posZ, itemStackHandler.getStackInSlot(0)));
                itemStackHandler.setStackInSlot(0, null);
                sync();
            }
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("items")) {
            itemStackHandler.deserializeNBT(tag.getCompoundTag("items"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("items", itemStackHandler.serializeNBT());
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

}
