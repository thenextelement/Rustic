package rustic.common.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.blocks.ModBlocks;
import rustic.common.util.FluidHelper;

public class TileEntityLiquidBarrel extends TileEntityFluidBase {

	public static int capacity = FluidContainerRegistry.BUCKET_VOLUME * 16;
	public static final int MAX_TEMP = 573;

	public TileEntityLiquidBarrel() {
		super(capacity);
	}

	public boolean activate(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
			float hitZ) {
		ItemStack heldItem = player.getCurrentEquippedItem();
		if (heldItem != null && FluidHelper.isFluidContainer(heldItem)) {
			FluidStack f = FluidHelper.getFluidContained(heldItem);
			if (f == null || (!f.getFluid().isGaseous(f) && f.getFluid().getTemperature(f) <= MAX_TEMP)) {
				if (!world.isRemote && FluidHelper.interactWithTank(player, tank)) {
					sync();
				}
			}
			return true;
		}
		return false;
	}

	public void breakBlock(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote && !player.capabilities.isCreativeMode) {
			ItemStack toDrop = new ItemStack(ModBlocks.LIQUID_BARREL, 1);
			if (getTank().getFluidAmount() > 0) {
				NBTTagCompound tag = new NBTTagCompound();
				getTank().writeToNBT(tag);
				toDrop.setTagCompound(tag);
			}
			world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, toDrop));
		}
		tank.setFluid(null);
	}

}
