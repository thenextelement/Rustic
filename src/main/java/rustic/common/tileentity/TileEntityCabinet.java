package rustic.common.tileentity;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import rustic.common.blocks.BlockCabinet;
import rustic.common.blocks.ModBlocks;
import rustic.common.util.ItemStackHandlerRustic;

public class TileEntityCabinet extends TileEntityInventoryBase {

	public float lidAngle = 0;
	public float prevLidAngle = 0;
	public int numPlayersUsing = 0;
	private int ticksSinceSync = 0;
	public ItemStack material = null;

	public TileEntityCabinet() {
		super(27);
	}

	@Override
	protected String getDefaultName() {
		return "container.rustic.cabinet";
	}

	public ItemStackHandlerRustic getSingleCabinetHandler() {
		return itemStackHandler;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		int height = (worldObj != null && BlockCabinet.isBottom(worldObj, xCoord, yCoord, zCoord)) ? 2 : 1;
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1D, yCoord + height, zCoord + 1D);
	}

	@Override
	public void updateEntity() {
		int i = xCoord, j = yCoord, k = zCoord;
		++ticksSinceSync;
		if (!worldObj.isRemote && numPlayersUsing != 0 && (ticksSinceSync + i + j + k) % 200 == 0) {
			int recordedUsers = numPlayersUsing;
			numPlayersUsing = 0;
			List<?> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
					AxisAlignedBB.getBoundingBox(i - 5.0F, j - 5.0F, k - 5.0F, i + 1 + 5.0F, j + 1 + 5.0F, k + 1 + 5.0F));
			for (Object o : players) {
				EntityPlayer player = (EntityPlayer) o;
				if (player.openContainer instanceof ContainerCabinet) {
					if (((ContainerCabinet) player.openContainer).getTile() == this) ++numPlayersUsing;
				} else if (player.openContainer instanceof ContainerCabinetDouble) {
					if (((ContainerCabinetDouble) player.openContainer).getTileBottom() == this) ++numPlayersUsing;
				}
			}
			if (numPlayersUsing != recordedUsers) markDirty();
		}

		prevLidAngle = lidAngle;
		if (numPlayersUsing > 0 && lidAngle == 0.0F) {
			worldObj.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, "random.chestopen", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}
		if (numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F) {
			float previous = lidAngle;
			lidAngle += numPlayersUsing > 0 ? 0.1F : -0.1F;
			if (lidAngle > 1.0F) lidAngle = 1.0F;
			if (lidAngle < 0.5F && previous >= 0.5F) {
				worldObj.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, "random.chestclosed", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}
			if (lidAngle < 0.0F) lidAngle = 0.0F;
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			numPlayersUsing = type;
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	public void openInventory(EntityPlayer player) {
		if (numPlayersUsing < 0) numPlayersUsing = 0;
		++numPlayersUsing;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, numPlayersUsing);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		markDirty();
	}

	public void closeInventory(EntityPlayer player) {
		if (getBlockType() instanceof BlockCabinet) {
			--numPlayersUsing;
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, numPlayersUsing);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
			markDirty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		numPlayersUsing = compound.getInteger("numUsers");
		material = compound.hasKey("material") ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag("material")) : null;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("numUsers", numPlayersUsing);
		if (material != null) {
			compound.setTag("material", material.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z) {
		super.breakBlock(world, x, y, z);
		if (!world.isRemote) {
			ItemStack toDrop = new ItemStack(ModBlocks.CABINET);
			NBTTagCompound tag = new NBTTagCompound();
			if (material != null) tag.setTag("material", material.writeToNBT(new NBTTagCompound()));
			toDrop.setTagCompound(tag);
			if (hasCustomInventoryName()) toDrop.setStackDisplayName(customName);
			spawnAsEntity(world, x, y, z, toDrop);
		}
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (BlockCabinet.isTop(meta)) {
			TileEntity below = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
			if (below instanceof TileEntityCabinet) {
				return new ContainerCabinetDouble(playerInventory, this, (TileEntityCabinet) below, player);
			}
		} else if (BlockCabinet.isBottom(worldObj, xCoord, yCoord, zCoord)) {
			TileEntity above = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
			if (above instanceof TileEntityCabinet) {
				return new ContainerCabinetDouble(playerInventory, (TileEntityCabinet) above, this, player);
			}
		}
		return new ContainerCabinet(playerInventory, this, player);
	}

}
