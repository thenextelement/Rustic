package rustic.common.world;

import java.util.Random;

import net.minecraft.block.Block;

import rustic.common.blocks.ModBlocks;

public class WorldGenIronwoodTree extends WorldGenRusticTree {

    public WorldGenIronwoodTree(boolean notify) {
        super(notify);
    }

    @Override
    protected int getHeight(Random rand) {
        return rand.nextInt(6) + 7;
    }

    @Override
    protected Block getLog() {
        return ModBlocks.LOG;
    }

    @Override
    protected int getLogMeta() {
        return 1;
    }

    @Override
    protected Block getLeaves() {
        return ModBlocks.LEAVES;
    }

    @Override
    protected int getLeavesMeta() {
        return 1;
    }

}
