package rustic.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import rustic.common.Config;
import rustic.common.blocks.ModBlocks;

public class WorldGenWildberries extends WorldGenerator {

	public static List<Type> biomeBlacklist = new ArrayList<>();

	static {
		biomeBlacklist.add(Type.COLD);
		biomeBlacklist.add(Type.SNOWY);
		biomeBlacklist.add(Type.SANDY);
		biomeBlacklist.add(Type.SAVANNA);
		biomeBlacklist.add(Type.MESA);
		biomeBlacklist.add(Type.MUSHROOM);
		biomeBlacklist.add(Type.NETHER);
		biomeBlacklist.add(Type.END);
		biomeBlacklist.add(Type.DEAD);
		biomeBlacklist.add(Type.WASTELAND);
	}

	@Override
	public boolean generate(World world, Random rand, int cx, int cy, int cz) {
		BiomeGenBase biome = world.getBiomeGenForCoords(cx, cz);
		for (Type type : biomeBlacklist) {
			if (BiomeDictionary.isBiomeOfType(biome, type)) return false;
		}
		boolean ret = false;
		for (int i = 0; i < Config.MAX_WILDBERRY_ATTEMPTS; i++) {
			int x = cx + rand.nextInt(7) - rand.nextInt(7);
			int z = cz + rand.nextInt(7) - rand.nextInt(7);
			int y = world.getTopSolidOrLiquidBlock(x, z);
			if (world.getBlock(x, y, z).getMaterial().isLiquid()) continue;
			if (ModBlocks.WILDBERRY_BUSH.canPlaceBlockAt(world, x, y, z)) {
				world.setBlock(x, y, z, ModBlocks.WILDBERRY_BUSH, rand.nextInt(6), 2);
				ret = true;
			}
		}
		return ret;
	}

}
