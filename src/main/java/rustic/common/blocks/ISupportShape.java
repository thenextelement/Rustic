package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Stand-in for 1.12's {@code BlockFaceShape}: lets non-full blocks say they can hold chains, lanterns, candles, etc.
 * on a given side.
 */
public interface ISupportShape {

    /** Whether the given side of this block (at x, y, z) can hold a small attached block like a chain or lantern. */
    boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side);

    /**
     * Whether the block at x, y, z can hold a small attachment on its given side: a solid face, a fence/wall top or
     * bottom, or any {@link ISupportShape} that says so.
     */
    static boolean canSupport(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        Block block = world.getBlock(x, y, z);
        if (block.isAir(world, x, y, z)) return false;
        if (block instanceof ISupportShape) {
            return ((ISupportShape) block).canSupportOnSide(world, x, y, z, side);
        }
        if (block.isSideSolid(world, x, y, z, side)) return true;
        if (side.offsetY != 0 && (block instanceof BlockFence || block instanceof BlockWall)) return true;
        return false;
    }

}
