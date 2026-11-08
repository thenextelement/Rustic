package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.util.Facing;

/**
 * Metadata: 0 = standing along x ("up_x"), 1 = east, 2 = west, 3 = south, 4 = north, 5 = standing along z ("up_z").
 */
public class BlockCandleDouble extends BlockCandle {

	public BlockCandleDouble() {
		this("candle_double");
	}

	public BlockCandleDouble(String name) {
		this(Material.circuits, name, true);
	}

	public BlockCandleDouble(Material mat, String name, boolean register) {
		super(mat, name, register);
	}

	@Override
	public ForgeDirection facingOf(int meta) {
		switch (meta & 7) {
			case 1: return ForgeDirection.EAST;
			case 2: return ForgeDirection.WEST;
			case 3: return ForgeDirection.SOUTH;
			case 4: return ForgeDirection.NORTH;
			default: return ForgeDirection.UP;
		}
	}

	/** Whether the candles are lined up along the x axis. */
	public static boolean isAlongX(int meta) {
		switch (meta & 7) {
			case 1: case 2: case 0: return true;
			default: return false;
		}
	}

	public static String facingName(int meta) {
		switch (meta & 7) {
			case 1: return "east";
			case 2: return "west";
			case 3: return "south";
			case 4: return "north";
			case 5: return "up_z";
			default: return "up_x";
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		ForgeDirection facing = ForgeDirection.getOrientation(side);
		if (canPlaceAt(world, x, y, z, facing)) {
			return facing == ForgeDirection.UP ? 0 : metaForFacing(facing);
		}
		for (ForgeDirection dir : Facing.HORIZONTALS) {
			if (world.isSideSolid(x - dir.offsetX, y, z - dir.offsetZ, dir, true)) return metaForFacing(dir);
		}
		return 0;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		int meta = world.getBlockMetadata(x, y, z);
		if (facingOf(meta) == ForgeDirection.UP && placer != null) {
			ForgeDirection look = Facing.horizontalFacing(placer);
			world.setBlockMetadataWithNotify(x, y, z, look.offsetZ != 0 ? 5 : 0, 2);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch (world.getBlockMetadata(x, y, z) & 7) {
			case 1: setBlockBounds(0.0F, 0.0F, 0.175F, 0.3625F, 0.8F, 0.825F); break;
			case 2: setBlockBounds(0.6375F, 0.0F, 0.175F, 1.0F, 0.8F, 0.825F); break;
			case 3: setBlockBounds(0.175F, 0.0F, 0.0F, 0.825F, 0.8F, 0.3625F); break;
			case 4: setBlockBounds(0.175F, 0.0F, 0.6375F, 0.825F, 0.8F, 1.0F); break;
			case 5: setBlockBounds(0.22F, 0.0F, 0.4F, 0.78F, 0.9375F, 0.6F); break;
			default: setBlockBounds(0.4F, 0.0F, 0.22F, 0.6F, 0.9375F, 0.78F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.4F, 0.0F, 0.22F, 0.6F, 0.9375F, 0.78F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		ForgeDirection facing = facingOf(meta);
		boolean isFacingX = isAlongX(meta);
		double d0 = x + 0.5D;
		double d1 = y + 0.7D;
		double d2 = z + 0.5D;
		double offset;
		if (facing.offsetY == 0) {
			offset = 0.23125;
			ForgeDirection back = facing.getOpposite();
			d0 += 0.2609375D * back.offsetX;
			d1 += 0.25D;
			d2 += 0.2609375D * back.offsetZ;
		} else {
			offset = 0.1875;
			d1 += 0.33D;
		}
		double ox = isFacingX ? 0 : offset, oz = isFacingX ? offset : 0;
		world.spawnParticle("smoke", d0 + ox, d1, d2 + oz, 0, 0, 0);
		world.spawnParticle("flame", d0 + ox, d1, d2 + oz, 0, 0, 0);
		world.spawnParticle("smoke", d0 - ox, d1, d2 - oz, 0, 0, 0);
		world.spawnParticle("flame", d0 - ox, d1, d2 - oz, 0, 0, 0);
	}

	@Override
	public Map<String, String> getInventoryProperties(int meta) {
		Map<String, String> props = new HashMap<>();
		props.put("facing", facingName(meta));
		return props;
	}

}
