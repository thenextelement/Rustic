package rustic.common.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.tileentity.TileEntityLiquidBarrel;

public class BlockLiquidBarrel extends BlockTileBase implements IModelBlock {

	/** Shows the stored fluid in the tooltip. */
	public static class ItemBlockLiquidBarrel extends ItemBlock {
		public ItemBlockLiquidBarrel(net.minecraft.block.Block block) {
			super(block);
		}

		@Override
		@SideOnly(Side.CLIENT)
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null && tag.hasKey("FluidName", Constants.NBT.TAG_STRING) && tag.hasKey("Amount", Constants.NBT.TAG_INT)) {
				Fluid fluid = FluidRegistry.getFluid(tag.getString("FluidName"));
				if (fluid != null) {
					int amount = tag.getInteger("Amount");
					tooltip.add(amount + "mb of " + fluid.getLocalizedName(new FluidStack(fluid, amount)));
				}
			}
		}
	}

	public BlockLiquidBarrel() {
		super(Material.wood, "liquid_barrel", false);
		this.setHardness(1.5F);
		setStepSound(soundTypeWood);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1F, 0.875F);
		register(ItemBlockLiquidBarrel.class);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLiquidBarrel();
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityLiquidBarrel) {
			((TileEntityLiquidBarrel) tile).breakBlock(world, x, y, z, player);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return tile instanceof TileEntityLiquidBarrel
				&& ((TileEntityLiquidBarrel) tile).activate(world, x, y, z, player, side, hitX, hitY, hitZ);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		return new ArrayList<>(); // the tile entity drops the barrel with its contents
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
		if (stack.hasTagCompound()) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityLiquidBarrel) {
				((TileEntityLiquidBarrel) tile).getTank().readFromNBT(stack.getTagCompound());
				tile.markDirty();
			}
		}
	}

	@Override
	public void fillWithRain(World world, int x, int y, int z) {
		float temperature = world.getBiomeGenForCoords(x, z).getFloatTemperature(x, y, z);
		if (temperature >= 0.15F) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityLiquidBarrel) {
				TileEntityLiquidBarrel barrel = (TileEntityLiquidBarrel) tile;
				FluidStack water = new FluidStack(FluidRegistry.WATER, 50);
				if (barrel.getTank().canFillFluidType(water)) {
					barrel.getTank().fill(water, true);
				}
			}
		}
	}

	@Override
	public String getBlockStateName() {
		return name;
	}

	@Override
	public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, String> getInventoryProperties(int meta) {
		return Collections.emptyMap();
	}

}
