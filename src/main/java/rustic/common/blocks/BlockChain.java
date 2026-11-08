package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.core.Rustic;

public class BlockChain extends BlockRopeBase {

	public BlockChain() {
		this("chain");
	}

	public BlockChain(String name) {
		this(Material.iron, name, true);
	}

	public BlockChain(Material mat, String name, boolean register) {
		super(mat, name, register);
		this.setHardness(1F);
		this.setCreativeTab(Rustic.decorTab);
		setStepSound(soundTypeMetal);
	}

	@Override
	public boolean isSideSupported(World world, int x, int y, int z, ForgeDirection facing) {
		if (facing == ForgeDirection.DOWN) {
			return false; // A surface below a chain can never be used as support
		}
		int ox = x + facing.offsetX, oy = y + facing.offsetY, oz = z + facing.offsetZ;
		Block other = world.getBlock(ox, oy, oz);
		int axis = getAxis(world, x, y, z);
		if (other == this && ((axis == AXIS_Y && facing.offsetY != 0) || getAxis(world, ox, oy, oz) == axis)) {
			return true;
		}
		if (other instanceof BlockLattice) {
			return true; // Lattice blocks always support
		}
		return ISupportShape.canSupport(world, ox, oy, oz, facing.getOpposite());
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		if (dir == ForgeDirection.UP) {
			// When attempting to place chain on ground see if there's a block above it to hang from instead.
			return canPlaceBlockOnSide(world, x, y, z, ForgeDirection.DOWN.ordinal());
		}
		ForgeDirection back = dir.getOpposite();
		int ox = x + back.offsetX, oy = y + back.offsetY, oz = z + back.offsetZ;
		Block other = world.getBlock(ox, oy, oz);
		if (other == this && getAxis(world, ox, oy, oz) == axisOf(dir)) return true;
		if (other instanceof BlockLattice) return true;
		return ISupportShape.canSupport(world, ox, oy, oz, dir);
	}

}
