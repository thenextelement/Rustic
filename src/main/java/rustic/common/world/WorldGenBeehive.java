package rustic.common.world;

import java.util.Random;

import net.minecraft.block.BlockLeaves;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import rustic.common.Config;
import rustic.common.blocks.ModBlocks;

public class WorldGenBeehive extends WorldGenerator {

    @Override
    public boolean generate(World world, Random rand, int cx, int cy, int cz) {
        for (int i = 0; i < Config.MAX_BEEHIVE_ATTEMPTS; ++i) {
            int x = cx + rand.nextInt(8) - rand.nextInt(8);
            int z = cz + rand.nextInt(8) - rand.nextInt(8);
            int y = world.getHeightValue(x, z) - 1;
            if (world.getBlock(x, y, z) instanceof BlockLeaves) {
                while (y > 0 && world.getBlock(x, y, z) instanceof BlockLeaves) y--;
                if (ModBlocks.BEEHIVE.canPlaceBlockAt(world, x, y, z)) {
                    world.setBlock(x, y, z, ModBlocks.BEEHIVE, rand.nextInt(4), 2);
                    return true;
                }
            }
        }
        return false;
    }

}
