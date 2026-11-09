package rustic.common.world;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.blocks.crops.BlockHerbBase;

final class HerbPlacer {

    private HerbPlacer() {}

    /** Places a fully grown herb at x, y, z if the block below can sustain it. */
    static boolean place(World world, int x, int y, int z, BlockHerbBase herb) {
        if (y <= 0 || !world.isAirBlock(x, y, z)) return false;
        if (world.getBlock(x, y - 1, z)
            .canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, herb)) {
            world.setBlock(x, y, z, herb, 3, 2);
            return true;
        }
        return false;
    }

    /** Drops down from an air block to the floor below (at most maxDrop blocks) and places the herb there. */
    static boolean placeOnFloor(World world, int x, int y, int z, BlockHerbBase herb, int minY, int maxDrop) {
        if (!world.isAirBlock(x, y, z)) return false;
        int i = 0;
        while (world.isAirBlock(x, y - i, z) && y - i >= minY && i < maxDrop) i++;
        return place(world, x, y - i + 1, z, herb);
    }

}
