package rustic.common.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.ForgeDirection;

/** A small oak-shaped tree (the shape of 1.12 Rustic's olive, ironwood and apple trees). */
public abstract class WorldGenRusticTree extends WorldGenAbstractTree {

    public WorldGenRusticTree(boolean notify) {
        super(notify);
    }

    protected abstract int getHeight(Random rand);

    protected abstract Block getLog();

    protected abstract int getLogMeta();

    protected abstract Block getLeaves();

    protected abstract int getLeavesMeta();

    @Override
    public boolean generate(World world, Random rand, int x, int y, int z) {
        int height = getHeight(rand);
        if (y < 1 || y + height + 1 > 256) return false;

        for (int j = y; j <= y + 1 + height; ++j) {
            int k = 1;
            if (j == y) k = 0;
            if (j >= y + 1 + height - 2) k = 2;
            for (int l = x - k; l <= x + k; ++l) {
                for (int i1 = z - k; i1 <= z + k; ++i1) {
                    if (j < 0 || j >= 256) return false;
                    if (!isReplaceable(world, l, j, i1)) return false;
                }
            }
        }

        Block soil = world.getBlock(x, y - 1, z);
        boolean isSoil = soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (BlockSapling) Blocks.sapling);
        if (!isSoil || y >= 256 - height - 1) return false;
        soil.onPlantGrow(world, x, y - 1, z, x, y, z);

        for (int i2 = y - 3 + height; i2 <= y + height; ++i2) {
            int k2 = i2 - (y + height);
            int l2 = 1 - k2 / 2;
            for (int i3 = x - l2; i3 <= x + l2; ++i3) {
                int j1 = i3 - x;
                for (int k1 = z - l2; k1 <= z + l2; ++k1) {
                    int l1 = k1 - z;
                    if (Math.abs(j1) != l2 || Math.abs(l1) != l2 || rand.nextInt(2) != 0 && k2 != 0) {
                        Block block = world.getBlock(i3, i2, k1);
                        if (block.isAir(world, i3, i2, k1) || block.isLeaves(world, i3, i2, k1)) {
                            setBlockAndNotifyAdequately(world, i3, i2, k1, getLeaves(), getLeavesMeta());
                        }
                    }
                }
            }
        }
        for (int j2 = 0; j2 < height; ++j2) {
            Block block = world.getBlock(x, y + j2, z);
            if (block.isAir(world, x, y + j2, z) || block.isLeaves(world, x, y + j2, z)) {
                setBlockAndNotifyAdequately(world, x, y + j2, z, getLog(), getLogMeta());
            }
        }
        return true;
    }

    protected boolean isReplaceable(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z)
            || block.isWood(world, x, y, z)
            || func_150523_a(block);
    }

}
