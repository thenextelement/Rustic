package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.EnumPlantType;
import rustic.common.items.ItemHerbEdible;

public class Herbs {

	public static BlockHerbBase ALOE_VERA;
	public static BlockHerbBase BLOOD_ORCHID;
	public static BlockHerbBase CHAMOMILE;
	public static BlockHerbBase CLOUDSBLUFF_CROP;
	public static BlockHerbBase COHOSH;
	public static BlockHerbBase CORE_ROOT_CROP;
	public static BlockHerbBase DEATHSTALK;
	public static BlockHerbBase GINSENG_CROP;
	public static BlockHerbBase HORSETAIL;
	public static BlockHerbBase MARSH_MALLOW_CROP;
	public static BlockHerbBase MOONCAP;
	public static BlockHerbBase WIND_THISTLE;
	public static BlockHerbBase VANTA_LILY;

	public static ItemHerbEdible CLOUDSBLUFF;
	public static ItemHerbEdible CORE_ROOT;
	public static ItemHerbEdible GINSENG;
	public static ItemHerbEdible MARSH_MALLOW;

	/** A herb whose block drops itself as an item. */
	private static BlockHerbBase herb(String name, EnumPlantType type, boolean flammable) {
		BlockHerbBase herb = new BlockHerbBase(name, false) {
			@Override
			public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
				return type;
			}

			@Override
			public Item getHerb() {
				return Item.getItemFromBlock(this);
			}
		};
		if (flammable) Blocks.fire.setFireInfo(herb, 60, 100);
		return herb;
	}

	/** A herb crop planted from (and dropping) a separate edible item. */
	private static BlockHerbBase crop(String name, EnumPlantType type, java.util.function.Supplier<Item> herbItem) {
		BlockHerbBase herb = new BlockHerbBase(name, true) {
			@Override
			public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
				return type;
			}

			@Override
			public Item getHerb() {
				return herbItem.get();
			}
		};
		Blocks.fire.setFireInfo(herb, 60, 100);
		return herb;
	}

	public static void init() {
		ALOE_VERA = herb("aloe_vera", EnumPlantType.Desert, false);
		BLOOD_ORCHID = herb("blood_orchid", EnumPlantType.Plains, true);
		CHAMOMILE = herb("chamomile", EnumPlantType.Plains, true);
		CLOUDSBLUFF_CROP = crop("cloudsbluff", EnumPlantType.Plains, () -> CLOUDSBLUFF);
		COHOSH = herb("cohosh", EnumPlantType.Plains, true);
		CORE_ROOT_CROP = crop("core_root", EnumPlantType.Cave, () -> CORE_ROOT);
		DEATHSTALK = herb("deathstalk_mushroom", EnumPlantType.Cave, false);
		GINSENG_CROP = crop("ginseng", EnumPlantType.Plains, () -> GINSENG);
		HORSETAIL = herb("horsetail", EnumPlantType.Plains, true);
		MARSH_MALLOW_CROP = crop("marsh_mallow", EnumPlantType.Plains, () -> MARSH_MALLOW);
		MOONCAP = herb("mooncap_mushroom", EnumPlantType.Cave, false);
		MOONCAP.setLightLevel(0.5F); // light value 8
		WIND_THISTLE = herb("wind_thistle", EnumPlantType.Plains, true);
		VANTA_LILY = herb("vanta_lily", EnumPlantType.Plains, true);

		CLOUDSBLUFF = new ItemHerbEdible(CLOUDSBLUFF_CROP, 2, 0.2F) {
			@Override
			public void initFood() {
				this.setAlwaysEdible();
			}

			@Override
			protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
				if (!world.isRemote) {
					// 1.7.10 has no levitation; strong jump boost and feather falling give a similar floaty effect
					player.addPotionEffect(new PotionEffect(Potion.jump.id, 400, 3));
					if (rustic.common.potions.PotionsRustic.FEATHER != null) {
						player.addPotionEffect(new PotionEffect(rustic.common.potions.PotionsRustic.FEATHER.id, 600));
					}
				}
			}
		};
		CORE_ROOT = new ItemHerbEdible(CORE_ROOT_CROP, 2, 0.3F);
		GINSENG = new ItemHerbEdible(GINSENG_CROP, 2, 0.3F);
		MARSH_MALLOW = new ItemHerbEdible(MARSH_MALLOW_CROP, 3, 0.3F);
	}

	private static boolean is(BiomeGenBase biome, BiomeDictionary.Type type) {
		return BiomeDictionary.isBiomeOfType(biome, type);
	}

	public static BlockHerbBase getRandomHerbForBiome(BiomeGenBase biome, Random rand) {
		List<BlockHerbBase> herbs = new ArrayList<>();
		boolean snowy = is(biome, BiomeDictionary.Type.SNOWY);
		if (is(biome, BiomeDictionary.Type.JUNGLE)) {
			herbs.add(BLOOD_ORCHID);
			herbs.add(HORSETAIL);
			herbs.add(MARSH_MALLOW_CROP);
			herbs.add(MOONCAP);
		} else if (is(biome, BiomeDictionary.Type.SANDY) || is(biome, BiomeDictionary.Type.SAVANNA)
				|| is(biome, BiomeDictionary.Type.MESA)) {
			herbs.add(ALOE_VERA);
		} else if (is(biome, BiomeDictionary.Type.MOUNTAIN) && !snowy) {
			herbs.add(WIND_THISTLE);
			herbs.add(CLOUDSBLUFF_CROP);
		} else if (is(biome, BiomeDictionary.Type.SWAMP) && !snowy) {
			herbs.add(CHAMOMILE);
			herbs.add(HORSETAIL);
			herbs.add(MARSH_MALLOW_CROP);
		} else if (is(biome, BiomeDictionary.Type.FOREST) && !snowy) {
			herbs.add(CHAMOMILE);
			herbs.add(COHOSH);
			herbs.add(GINSENG_CROP);
			herbs.add(HORSETAIL);
			herbs.add(VANTA_LILY);
		} else if (is(biome, BiomeDictionary.Type.PLAINS) && !snowy) {
			herbs.add(CHAMOMILE);
			herbs.add(GINSENG_CROP);
			herbs.add(HORSETAIL);
			herbs.add(WIND_THISTLE);
			herbs.add(VANTA_LILY);
		} else {
			return null;
		}
		return herbs.get(rand.nextInt(herbs.size()));
	}

	public static BlockHerbBase getRandomCaveHerb(Random rand) {
		return rand.nextBoolean() ? CORE_ROOT_CROP : MOONCAP;
	}

	public static BlockHerbBase getRandomNetherHerb(Random rand) {
		return DEATHSTALK;
	}

}
