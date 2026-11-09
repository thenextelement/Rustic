package rustic.common.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import rustic.common.Config;
import rustic.common.blocks.ModBlocks;

public class WorldGenAllTrees extends WorldGenerator {

	private final WorldGenOliveTree olives = new WorldGenOliveTree(true);
	private final WorldGenIronwoodTree ironwoods = new WorldGenIronwoodTree(true);

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		boolean ret = generateOliveTrees(world, rand, x, z);
		ret |= generateIronwoodTrees(world, rand, x, z);
		return ret;
	}

	private static boolean is(BiomeGenBase biome, Type type) {
		return BiomeDictionary.isBiomeOfType(biome, type);
	}

	private boolean generateOliveTrees(World world, Random rand, int cx, int cz) {
		boolean generated = false;
		BiomeGenBase biome = world.getBiomeGenForCoords(cx, cz);
		boolean validBiome = !is(biome, Type.SNOWY) && (is(biome, Type.FOREST) || is(biome, Type.PLAINS) || is(biome, Type.MOUNTAIN));
		if (validBiome && rand.nextFloat() < Config.OLIVE_GEN_CHANCE) {
			for (int i = 0; i < Config.MAX_OLIVE_GEN_ATTEMPTS; i++) {
				int x = cx + (5 - rand.nextInt(11));
				int z = cz + (5 - rand.nextInt(11));
				int y = world.getHeightValue(x, z);
				if (!world.getBiomeGenForCoords(x, z).getEnableSnow() && ModBlocks.SAPLING.canPlaceBlockAt(world, x, y, z)
						&& olives.generate(world, rand, x, y, z)) {
					generated = true;
				}
			}
		}
		return generated;
	}

	private boolean generateIronwoodTrees(World world, Random rand, int cx, int cz) {
		boolean generated = false;
		BiomeGenBase biome = world.getBiomeGenForCoords(cx, cz);
		boolean validBiome = !is(biome, Type.DRY) && (is(biome, Type.FOREST) || is(biome, Type.PLAINS)
				|| is(biome, Type.MOUNTAIN) || is(biome, Type.SWAMP) || is(biome, Type.JUNGLE));
		if (validBiome && rand.nextFloat() < Config.IRONWOOD_GEN_CHANCE) {
			for (int i = 0; i < Config.MAX_IRONWOOD_GEN_ATTEMPTS; i++) {
				int x = cx + (5 - rand.nextInt(11));
				int z = cz + (5 - rand.nextInt(11));
				int y = world.getHeightValue(x, z);
				if (ModBlocks.SAPLING.canPlaceBlockAt(world, x, y, z) && ironwoods.generate(world, rand, x, y, z)) {
					generated = true;
				}
			}
		}
		return generated;
	}

}
