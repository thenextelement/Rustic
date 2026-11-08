package rustic.common.blocks;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.util.Facing;

/**
 * Metadata: 0 = standing or hanging ("up"), 1 = east, 2 = west, 3 = south, 4 = north. A standing lantern may also
 * hang from the block above.
 */
public class BlockWoodLantern extends BlockCandle {

	public BlockWoodLantern() {
		this("lantern_wood");
	}

	public BlockWoodLantern(String name) {
		this(Material.wood, name, true);
	}

	public BlockWoodLantern(Material mat, String name, boolean register) {
		super(mat, name, register);
		this.setHardness(1F);
		this.setLightLevel(1.0F);
		setStepSound(soundTypeWood);
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

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
			if (canPlaceAt(world, x, y, z, facing)) return true;
		}
		return false;
	}

	@Override
	protected boolean canPlaceAt(World world, int x, int y, int z, ForgeDirection facing) {
		ForgeDirection back = facing.getOpposite();
		int bx = x + back.offsetX, by = y + back.offsetY, bz = z + back.offsetZ;
		if (facing.offsetY == 0) return world.isSideSolid(bx, by, bz, facing, true);
		if (ISupportShape.canSupport(world, bx, by, bz, facing)) return true;
		if (facing == ForgeDirection.UP) {
			Block block = world.getBlock(bx, by, bz);
			return block.canPlaceTorchOnTop(world, bx, by, bz);
		}
		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		ForgeDirection facing = ForgeDirection.getOrientation(side);
		if (canPlaceAt(world, x, y, z, facing)) {
			return facing.offsetY != 0 ? 0 : metaForFacing(facing);
		}
		for (ForgeDirection dir : Facing.HORIZONTALS) {
			if (world.isSideSolid(x - dir.offsetX, y, z - dir.offsetZ, dir, true)) return metaForFacing(dir);
		}
		return 0;
	}

	@Override
	protected boolean checkForDrop(World world, int x, int y, int z) {
		if (world.getBlock(x, y, z) != this) return false;
		int meta = world.getBlockMetadata(x, y, z);
		ForgeDirection facing = facingOf(meta);
		if (canPlaceAt(world, x, y, z, facing)) return true;
		if (facing == ForgeDirection.UP && canPlaceAt(world, x, y, z, ForgeDirection.DOWN)) return true;
		dropBlockAsItem(world, x, y, z, meta, 0);
		world.setBlockToAir(x, y, z);
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch (facingOf(world.getBlockMetadata(x, y, z))) {
			case EAST: setBlockBounds(0.0F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F); break;
			case WEST: setBlockBounds(0.25F, 0.0F, 0.25F, 1.0F, 1.0F, 0.75F); break;
			case SOUTH: setBlockBounds(0.25F, 0.0F, 0.0F, 0.75F, 1.0F, 0.75F); break;
			case NORTH: setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 1.0F); break;
			default: setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return side.offsetY != 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		double d0 = x + 0.5D;
		double d1 = y + 0.5D + (rand.nextFloat() * (6.0D / 16.0D));
		double d2 = z + 0.5D;
		double rx = (rand.nextFloat() * 0.6F) - 0.3F;
		double rz = (rand.nextFloat() * 0.6F) - 0.3F;
		world.spawnParticle("smoke", d0 + rx, d1, d2 + rz, 0, 0, 0);
		world.spawnParticle("flame", d0 + rx, d1, d2 + rz, 0, 0, 0);
	}

}
