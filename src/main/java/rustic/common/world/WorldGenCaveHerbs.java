package rustic.common.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import rustic.common.Config;
import rustic.common.blocks.crops.BlockHerbBase;
import rustic.common.blocks.crops.Herbs;

public class WorldGenCaveHerbs extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int cx, int cy, int cz) {
		BlockHerbBase herb = Herbs.getRandomCaveHerb(rand);
		boolean ret = false;
		for (int i = 0; i < Config.MAX_HERB_ATTEMPTS; i++) {
			int x = cx + rand.nextInt(7) - rand.nextInt(7);
			int z = cz + rand.nextInt(7) - rand.nextInt(7);
			int y = rand.nextInt(32) + 10;
			ret |= HerbPlacer.placeOnFloor(world, x, y, z, herb, 4, 256);
		}
		return ret;
	}

}
