package rustic.common.world;

import java.util.Random;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import rustic.common.Config;
import rustic.common.blocks.ModBlocks;

public class WorldGeneratorRustic implements IWorldGenerator {

	private WorldGenMinable slate;
	private final WorldGenBeehive beehives = new WorldGenBeehive();
	private final WorldGenAllTrees trees = new WorldGenAllTrees();
	private final WorldGenSurfaceHerbs surfaceHerbs = new WorldGenSurfaceHerbs();
	private final WorldGenCaveHerbs caveHerbs = new WorldGenCaveHerbs();
	private final WorldGenNetherHerbs netherHerbs = new WorldGenNetherHerbs();
	private final WorldGenWildberries wildberries = new WorldGenWildberries();

	private WorldGenMinable getSlate() {
		if (slate == null && Config.ENABLE_SLATE) {
			slate = Config.NETHER_SLATE
					? new WorldGenMinable(ModBlocks.SLATE, 0, Config.SLATE_VEIN_SIZE, Blocks.netherrack)
					: new WorldGenMinable(ModBlocks.SLATE, Config.SLATE_VEIN_SIZE);
		}
		return slate;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
			IChunkProvider chunkProvider) {
		int cx = chunkX * 16 + 8, cz = chunkZ * 16 + 8;
		int cy = world.getHeightValue(cx, cz);
		int dim = world.provider.dimensionId;
		if (Config.OVERWORLD_GENERATION_WHITELIST.contains(dim) && (world.getWorldInfo().getTerrainType() != WorldType.FLAT
				|| world.getWorldInfo().getGeneratorOptions().contains("decoration"))) {
			if (random.nextFloat() < Config.WILDBERRY_GEN_CHANCE) {
				wildberries.generate(world, random, cx, cy, cz);
			}
			if (random.nextFloat() < Config.HERB_GEN_CHANCE) {
				surfaceHerbs.generate(world, random, cx, cy, cz);
			}
			if (random.nextFloat() < Config.HERB_GEN_CHANCE) {
				caveHerbs.generate(world, random, cx, cy, cz);
			}
			trees.generate(world, random, cx, cy, cz);
			if (!world.getBiomeGenForCoords(cx, cz).getEnableSnow() && random.nextFloat() < Config.BEEHIVE_GEN_CHANCE) {
				beehives.generate(world, random, cx, cy, cz);
			}
			if (Config.ENABLE_SLATE && !Config.NETHER_SLATE) {
				for (int i = 0; i < Config.SLATE_VEINS_PER_CHUNK; i++) {
					getSlate().generate(world, random, chunkX * 16 + random.nextInt(16), random.nextInt(80) + 4,
							chunkZ * 16 + random.nextInt(16));
				}
			}
		} else if (Config.NETHER_GENERATION_WHITELIST.contains(dim)) {
			if (random.nextFloat() < Config.HERB_GEN_CHANCE) {
				netherHerbs.generate(world, random, cx, cy, cz);
			}
			if (Config.ENABLE_SLATE && Config.NETHER_SLATE) {
				for (int i = 0; i < Config.SLATE_VEINS_PER_CHUNK; i++) {
					getSlate().generate(world, random, chunkX * 16 + random.nextInt(16), random.nextInt(112) + 8,
							chunkZ * 16 + random.nextInt(16));
				}
			}
		}
	}

}
