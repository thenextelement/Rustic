package rustic.common.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Common tile entity helpers: full-NBT client sync and item dropping. */
public abstract class TileEntityRustic extends TileEntity {

    /** NBT sent to clients; defaults to everything that is saved. */
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return tag;
    }

    @Override
    public Packet getDescriptionPacket() {
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    /** Re-send this tile entity to clients and save it. */
    public void markForUpdate() {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return !isInvalid() && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    public boolean canInteractWith(EntityPlayer player) {
        return isUseableByPlayer(player);
    }

    public static void spawnAsEntity(World world, int x, int y, int z, ItemStack stack) {
        if (world.isRemote || stack == null || stack.stackSize <= 0) return;
        float f = 0.5F;
        double dx = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        double dy = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        double dz = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        EntityItem item = new EntityItem(world, x + dx, y + dy, z + dz, stack);
        item.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(item);
    }

}
