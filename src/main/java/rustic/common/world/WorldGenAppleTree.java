package rustic.common.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import rustic.common.blocks.ModBlocks;

/** Oak logs with apple leaves. */
public class WorldGenAppleTree extends WorldGenRusticTree {

    public WorldGenAppleTree(boolean notify) {
        super(notify);
    }

    @Override
    protected int getHeight(Random rand) {
        return rand.nextInt(2) + 5;
    }

    @Override
    protected Block getLog() {
        return Blocks.log;
    }

    @Override
    protected int getLogMeta() {
        return 0;
    }

    @Override
    protected Block getLeaves() {
        return ModBlocks.APPLE_LEAVES;
    }

    @Override
    protected int getLeavesMeta() {
        return 0;
    }

}
