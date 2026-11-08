package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Ropes and chains. The axis is stored in the low two bits of metadata: 0 = y (hanging), 1 = x, 2 = z.
 */
public abstract class BlockRopeBase extends BlockBase implements IModelBlock, ISupportShape {

	public static final int AXIS_Y = 0, AXIS_X = 1, AXIS_Z = 2;
	public static final String[] AXIS_NAMES = { "y", "x", "z" };

	public BlockRopeBase(Material mat, String name, boolean register) {
		super(mat, name, register);
	}

	public static int axisOf(ForgeDirection dir) {
		if (dir.offsetX != 0) return AXIS_X;
		if (dir.offsetZ != 0) return AXIS_Z;
		return AXIS_Y;
	}

	public int getAxis(int meta) {
		int axis = meta & 3;
		return axis > 2 ? AXIS_Y : axis;
	}

	public int getAxis(IBlockAccess world, int x, int y, int z) {
		return getAxis(world.getBlockMetadata(x, y, z));
	}

	/** Metadata to use for a block of this type with the given axis. */
	public int metaForAxis(int axis) {
		return axis;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		ItemStack stack = player.getCurrentEquippedItem();
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		if (stack != null && stack.getItem() == Item.getItemFromBlock(this)
				&& !canPlaceBlockOnSide(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, side)) {
			if (!this.isBlockSupported(world, x, y, z)) {
				this.dropBlock(world, x, y, z);
				return true;
			}

			int yOffset = 1;
			while (yOffset < 64 && world.getBlock(x, y - yOffset, z) == this) {
				if (getAxis(world, x, y - yOffset, z) != AXIS_Y) {
					return false;
				}
				yOffset++;
			}
			if (y - yOffset > 0 && canPlaceBlockAt(world, x, y - yOffset, z)) {
				world.setBlock(x, y - yOffset, z, this, metaForAxis(AXIS_Y), 3);
				if (!player.capabilities.isCreativeMode) {
					stack.stackSize--;
				}
				world.playSoundEffect(x + 0.5, y - yOffset + 0.5, z + 0.5, stepSound.func_150496_b(),
						(stepSound.getVolume() + 1.0F) / 2.0F, stepSound.getPitch() * 0.8F);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return true;
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		if (dir == ForgeDirection.UP) {
			return canPlaceBlockOnSide(world, x, y, z, ForgeDirection.DOWN.ordinal());
		}
		ForgeDirection back = dir.getOpposite();
		int ox = x + back.offsetX, oy = y + back.offsetY, oz = z + back.offsetZ;
		Block other = world.getBlock(ox, oy, oz);
		boolean isThis = other == this && getAxis(world, ox, oy, oz) == axisOf(dir);
		boolean isSideSolid = world.isSideSolid(ox, oy, oz, dir, false);
		return isThis || isSideSolid;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		return metaForAxis(axisOf(ForgeDirection.getOrientation(side)));
	}

	protected void dropBlock(World world, int x, int y, int z) {
		this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		world.setBlockToAir(x, y, z);
		world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, stepSound.getBreakSound(),
				(stepSound.getVolume() + 1.0F) / 2.0F, stepSound.getPitch() * 0.8F);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		if (!world.isRemote && !this.isBlockSupported(world, x, y, z)) {
			this.dropBlock(world, x, y, z);
		}
	}

	public boolean isSideSupported(World world, int x, int y, int z, ForgeDirection facing) {
		if (facing == ForgeDirection.DOWN) return false;
		int ox = x + facing.offsetX, oy = y + facing.offsetY, oz = z + facing.offsetZ;
		Block other = world.getBlock(ox, oy, oz);
		int axis = getAxis(world, x, y, z);
		boolean isSame = other == this && ((axis == AXIS_Y && facing.offsetY != 0) || getAxis(world, ox, oy, oz) == axis);
		boolean isSideSolid = world.isSideSolid(ox, oy, oz, facing.getOpposite(), false);
		return isSame || isSideSolid;
	}

	public boolean isBlockSupported(World world, int x, int y, int z) {
		switch (getAxis(world, x, y, z)) {
			case AXIS_X:
				return isSideSupported(world, x, y, z, ForgeDirection.WEST) && isSideSupported(world, x, y, z, ForgeDirection.EAST);
			case AXIS_Z:
				return isSideSupported(world, x, y, z, ForgeDirection.NORTH) && isSideSupported(world, x, y, z, ForgeDirection.SOUTH);
			default:
				return isSideSupported(world, x, y, z, ForgeDirection.UP);
		}
	}

	public boolean isDangling(IBlockAccess world, int x, int y, int z) {
		if (getAxis(world, x, y, z) == AXIS_Y) return false;
		Block below = world.getBlock(x, y - 1, z);
		return below instanceof BlockRopeBase && !(below instanceof rustic.common.blocks.crops.BlockGrapeLeaves)
				&& ((BlockRopeBase) below).getAxis(world, x, y - 1, z) == AXIS_Y;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		boolean dangle = isDangling(world, x, y, z);
		switch (getAxis(world, x, y, z)) {
			case AXIS_X:
				setBlockBounds(0.0F, dangle ? 0.0F : 0.4375F, 0.4375F, 1.0F, 0.5625F, 0.5625F);
				break;
			case AXIS_Z:
				setBlockBounds(0.4375F, dangle ? 0.0F : 0.4375F, 0.0F, 0.5625F, 0.5625F, 1.0F);
				break;
			default:
				setBlockBounds(0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return getAxis(world, x, y, z) == axisOf(side);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public String getBlockStateName() {
		return name;
	}

	@Override
	public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
		Map<String, String> props = new HashMap<>();
		props.put("axis", AXIS_NAMES[getAxis(world, x, y, z)]);
		props.put("dangle", String.valueOf(isDangling(world, x, y, z)));
		return props;
	}

	@Override
	public Map<String, String> getInventoryProperties(int meta) {
		Map<String, String> props = new HashMap<>();
		props.put("axis", "y");
		props.put("dangle", "false");
		return props;
	}

}
