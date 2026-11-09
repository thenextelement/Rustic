package rustic.common.world;

import java.util.Random;

import net.minecraft.block.Block;

import rustic.common.blocks.ModBlocks;

public class WorldGenOliveTree extends WorldGenRusticTree {

    public WorldGenOliveTree(boolean notify) {
        super(notify);
    }

    @Override
    protected int getHeight(Random rand) {
        return rand.nextInt(3) + 4;
    }

    @Override
    protected Block getLog() {
        return ModBlocks.LOG;
    }

    @Override
    protected int getLogMeta() {
        return 0;
    }

    @Override
    protected Block getLeaves() {
        return ModBlocks.LEAVES;
    }

    @Override
    protected int getLeavesMeta() {
        return 0;
    }

}
