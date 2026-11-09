package rustic.common.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;
import rustic.common.Config;
import rustic.common.blocks.crops.BlockHerbBase;
import rustic.common.blocks.crops.Herbs;

public class WorldGenSurfaceHerbs extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int cx, int cy, int cz) {
		BiomeGenBase biome = world.getBiomeGenForCoords(cx, cz);
		if (biome.getEnableSnow()) return false;
		BlockHerbBase herb = Herbs.getRandomHerbForBiome(biome, rand);
		if (herb == null) return false;
		boolean ret = false;
		for (int i = 0; i < Config.MAX_HERB_ATTEMPTS; i++) {
			int x = cx + rand.nextInt(7) - rand.nextInt(7);
			int z = cz + rand.nextInt(7) - rand.nextInt(7);
			int y = world.getTopSolidOrLiquidBlock(x, z);
			ret |= HerbPlacer.place(world, x, y, z, herb);
		}
		return ret;
	}

}
