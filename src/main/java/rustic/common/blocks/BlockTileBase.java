package rustic.common.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Base for Rustic blocks that carry a tile entity. */
public abstract class BlockTileBase extends BlockBase implements ITileEntityProvider {

	public interface INameable {
		void setCustomName(String name);
	}

	/** Tile entities that need to do something (drop contents etc.) when their block is broken. */
	public interface IBreakable {
		void breakBlock(World world, int x, int y, int z);
	}

	public BlockTileBase(Material mat, String name) {
		super(mat, name);
	}

	public BlockTileBase(Material mat, String name, boolean register) {
		super(mat, name, register);
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public boolean onBlockEventReceived(World world, int x, int y, int z, int id, int param) {
		super.onBlockEventReceived(world, x, y, z, id, param);
		TileEntity tile = world.getTileEntity(x, y, z);
		return tile != null && tile.receiveClientEvent(id, param);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IBreakable) {
			((IBreakable) tile).breakBlock(world, x, y, z);
		}
		super.breakBlock(world, x, y, z, block, meta);
		world.removeTileEntity(x, y, z);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof INameable) {
				((INameable) tile).setCustomName(stack.getDisplayName());
				tile.markDirty();
			}
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

}
