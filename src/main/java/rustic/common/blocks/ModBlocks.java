package rustic.common.blocks;

import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import rustic.common.Config;
import rustic.common.blocks.crops.BlockAppleSeeds;
import rustic.common.blocks.crops.BlockBerryBush;
import rustic.common.blocks.crops.BlockCropStake;
import rustic.common.blocks.crops.BlockGrapeLeaves;
import rustic.common.blocks.crops.BlockGrapeStem;
import rustic.common.blocks.crops.BlockLeavesApple;
import rustic.common.blocks.crops.BlockSaplingApple;
import rustic.common.blocks.crops.BlockStakeCrop;
import rustic.common.blocks.crops.BlockStakeTied;
import rustic.common.blocks.crops.Herbs;
import rustic.common.blocks.slab.BlockDoubleSlabBase;
import rustic.common.blocks.slab.BlockSlabBase;
import rustic.common.items.ModItems;
import rustic.common.tileentity.TileEntityApiary;
import rustic.common.tileentity.TileEntityBarrel;
import rustic.common.tileentity.TileEntityBrewingBarrel;
import rustic.common.tileentity.TileEntityCabinet;
import rustic.common.tileentity.TileEntityCondenser;
import rustic.common.tileentity.TileEntityCondenserAdvancedBottom;
import rustic.common.tileentity.TileEntityCondenserAdvancedTop;
import rustic.common.tileentity.TileEntityCrushingTub;
import rustic.common.tileentity.TileEntityEvaporatingBasin;
import rustic.common.tileentity.TileEntityLiquidBarrel;
import rustic.common.tileentity.TileEntityVase;
import rustic.core.Rustic;







public class ModBlocks {

	/** Render ids of the JSON model renderer, set by the client proxy (plain and flat-item variants). */
	public static int MODEL_RENDER_ID = -1;
	public static int MODEL_RENDER_ID_FLAT = -1;

	public static BlockPillar STONE_PILLAR;
	public static BlockPillar SLATE_PILLAR;
	public static BlockBase SLATE;
	public static BlockBase SLATE_ROOF;
	public static BlockBase SLATE_TILE;
	public static BlockBase SLATE_BRICK;
	public static BlockBase SLATE_CHISELED;
	public static BlockStairsBase SLATE_ROOF_STAIRS;
	public static BlockDoubleSlabBase SLATE_ROOF_DOUBLESLAB;
	public static BlockSlabBase SLATE_ROOF_SLAB;
	public static BlockBase CLAY_WALL;
	public static BlockBase CLAY_WALL_CROSS;
	public static BlockClayWallDiag CLAY_WALL_DIAG;
	public static BlockChain CHAIN;
	public static BlockCandle CANDLE;
	public static BlockCandleDouble CANDLE_DOUBLE;
	public static BlockCandle CANDLE_LEVER;
	public static BlockChandelier CHANDELIER;
	public static BlockIronTorch IRON_TORCH;
	public static BlockChain CHAIN_GOLD;
	public static BlockCandle CANDLE_GOLD;
	public static BlockCandleDouble CANDLE_DOUBLE_GOLD;
	public static BlockCandle CANDLE_LEVER_GOLD;
	public static BlockChandelier CHANDELIER_GOLD;
	public static BlockChain CHAIN_SILVER;
	public static BlockCandle CANDLE_SILVER;
	public static BlockCandleDouble CANDLE_DOUBLE_SILVER;
	public static BlockCandle CANDLE_LEVER_SILVER;
	public static BlockChandelier CHANDELIER_SILVER;
	public static BlockBeehive BEEHIVE;
	public static BlockApiary APIARY;
	public static BlockChair CHAIR_OAK;
	public static BlockChair CHAIR_BIG_OAK;
	public static BlockChair CHAIR_BIRCH;
	public static BlockChair CHAIR_SPRUCE;
	public static BlockChair CHAIR_ACACIA;
	public static BlockChair CHAIR_JUNGLE;
	public static BlockTable TABLE_OAK;
	public static BlockTable TABLE_BIG_OAK;
	public static BlockTable TABLE_BIRCH;
	public static BlockTable TABLE_SPRUCE;
	public static BlockTable TABLE_ACACIA;
	public static BlockTable TABLE_JUNGLE;
	public static BlockVase VASE;
	public static BlockBarrel BARREL;
	public static BlockLattice IRON_LATTICE;
	public static BlockLantern IRON_LANTERN;
	public static BlockLantern GOLDEN_LANTERN;
	public static BlockLantern SILVER_LANTERN;
	public static BlockBase PAINTED_WOOD_WHITE;
	public static BlockBase PAINTED_WOOD_ORANGE;
	public static BlockBase PAINTED_WOOD_MAGENTA;
	public static BlockBase PAINTED_WOOD_LIGHT_BLUE;
	public static BlockBase PAINTED_WOOD_YELLOW;
	public static BlockBase PAINTED_WOOD_LIME;
	public static BlockBase PAINTED_WOOD_PINK;
	public static BlockBase PAINTED_WOOD_GRAY;
	public static BlockBase PAINTED_WOOD_SILVER;
	public static BlockBase PAINTED_WOOD_CYAN;
	public static BlockBase PAINTED_WOOD_PURPLE;
	public static BlockBase PAINTED_WOOD_BLUE;
	public static BlockBase PAINTED_WOOD_BROWN;
	public static BlockBase PAINTED_WOOD_GREEN;
	public static BlockBase PAINTED_WOOD_RED;
	public static BlockBase PAINTED_WOOD_BLACK;
	public static BlockDoubleSlabBase SLATE_BRICK_DOUBLESLAB;
	public static BlockSlabBase SLATE_BRICK_SLAB;
	public static BlockStairsBase SLATE_BRICK_STAIRS;
	public static BlockGargoyle GARGOYLE;
	public static BlockCabinet CABINET;
	public static BlockLiquidBarrel LIQUID_BARREL;
	public static BlockFertileSoil FERTILE_SOIL;
	public static BlockPlanksRustic PLANKS;
	public static BlockLogRustic LOG;
	public static BlockLeavesRustic LEAVES;
	public static BlockSaplingRustic SAPLING;
	public static BlockCrushingTub CRUSHING_TUB;
	public static BlockEvaporatingBasin EVAPORATING_BASIN;
	public static BlockFenceRustic OLIVE_FENCE;
	public static BlockFenceRustic IRONWOOD_FENCE;
	public static BlockFenceGateRustic OLIVE_FENCE_GATE;
	public static BlockFenceGateRustic IRONWOOD_FENCE_GATE;
	public static BlockDoubleSlabBase OLIVE_DOUBLESLAB;
	public static BlockSlabBase OLIVE_SLAB;
	public static BlockDoubleSlabBase IRONWOOD_DOUBLESLAB;
	public static BlockSlabBase IRONWOOD_SLAB;
	public static BlockStairsBase OLIVE_STAIRS;
	public static BlockStairsBase IRONWOOD_STAIRS;
	public static BlockChair CHAIR_OLIVE;
	public static BlockChair CHAIR_IRONWOOD;
	public static BlockTable TABLE_OLIVE;
	public static BlockTable TABLE_IRONWOOD;
	public static BlockCondenser CONDENSER;
	public static BlockRetort RETORT;
	public static BlockCondenserAdvanced CONDENSER_ADVANCED;
	public static BlockRetort RETORT_ADVANCED;
	public static BlockCropStake CROP_STAKE;
	public static BlockStakeCrop TOMATO_CROP;
	public static BlockStakeCrop CHILI_CROP;
	public static BlockBerryBush WILDBERRY_BUSH;
	public static BlockRope ROPE;
	public static BlockStakeTied STAKE_TIED;
	public static BlockGrapeStem GRAPE_STEM;
	public static BlockGrapeLeaves GRAPE_LEAVES;
	public static BlockBrewingBarrel BREWING_BARREL;
	public static BlockAppleSeeds APPLE_SEEDS;
	public static BlockSaplingApple APPLE_SAPLING;
	public static BlockLeavesApple APPLE_LEAVES;
	public static BlockDoorRustic DOOR_OLIVE;
	public static BlockDoorRustic DOOR_IRONWOOD;
	public static BlockWoodLantern LANTERN_WOOD;

	public static void init() {
		if (Config.ENABLE_PILLARS) {
			STONE_PILLAR = new BlockPillar("stone");
			if (Config.ENABLE_SLATE) {
				SLATE_PILLAR = new BlockPillar("slate");
			}
		}
		CHAIN = new BlockChain();
		CANDLE = new BlockCandle();
		CANDLE_DOUBLE = new BlockCandleDouble();
		CANDLE_LEVER = new BlockCandleLever();
		CHANDELIER = new BlockChandelier();
		IRON_TORCH = new BlockIronTorch();
		CHAIN_GOLD = new BlockChain("chain_gold");
		CANDLE_GOLD = new BlockCandle("candle_gold");
		CANDLE_DOUBLE_GOLD = new BlockCandleDouble("candle_double_gold");
		CANDLE_LEVER_GOLD = new BlockCandleLever("candle_lever_gold");
		CHANDELIER_GOLD = new BlockChandelier("chandelier_gold");
		if (Config.ENABLE_SILVER_DECOR) {
			CHAIN_SILVER = new BlockChain("chain_silver");
			CANDLE_SILVER = new BlockCandle("candle_silver");
			CANDLE_DOUBLE_SILVER = new BlockCandleDouble("candle_double_silver");
			CANDLE_LEVER_SILVER = new BlockCandleLever("candle_lever_silver");
			CHANDELIER_SILVER = new BlockChandelier("chandelier_silver");
		}
		BEEHIVE = new BlockBeehive();
		APIARY = new BlockApiary();
		if (Config.ENABLE_SLATE) {
			SLATE = (BlockBase) new BlockBase(Material.rock, "slate").setHardness(2.0F);
			SLATE_ROOF = (BlockBase) new BlockBase(Material.rock, "slate_roof").setHardness(2.0F);
			SLATE_TILE = (BlockBase) new BlockBase(Material.rock, "slate_tile").setHardness(2.0F);
			SLATE_BRICK = (BlockBase) new BlockBase(Material.rock, "slate_brick").setHardness(2.0F);
			SLATE_CHISELED = (BlockBase) new BlockBase(Material.rock, "slate_chiseled").setHardness(2.0F);
			SLATE_ROOF_STAIRS = new BlockStairsBase(SLATE_ROOF, "stairs_slate_roof");
			SLATE_ROOF_DOUBLESLAB = new BlockDoubleSlabBase(Material.rock, "slate_roof_doubleslab");
			SLATE_ROOF_SLAB = new BlockSlabBase(Material.rock, "slate_roof_slab", SLATE_ROOF_DOUBLESLAB);
			SLATE_BRICK_STAIRS = new BlockStairsBase(SLATE_BRICK, "stairs_slate_brick");
			SLATE_BRICK_DOUBLESLAB = new BlockDoubleSlabBase(Material.rock, "slate_brick_doubleslab");
			SLATE_BRICK_SLAB = new BlockSlabBase(Material.rock, "slate_brick_slab", SLATE_BRICK_DOUBLESLAB);
		}
		if (Config.ENABLE_CLAY_WALLS) {
			CLAY_WALL = ((BlockBase) new BlockBase(Material.clay, "clay_wall").setHardness(1F))
					.setBlockSoundType(Block.soundTypeGravel);
			CLAY_WALL_CROSS = ((BlockBase) new BlockBase(Material.clay, "clay_wall_cross").setHardness(1F))
					.setBlockSoundType(Block.soundTypeGravel);
			CLAY_WALL_DIAG = (BlockClayWallDiag) new BlockClayWallDiag().setHardness(1F);
		}
		if (Config.ENABLE_CHAIRS) {
			CHAIR_OAK = new BlockChair("oak");
			CHAIR_BIG_OAK = new BlockChair("big_oak");
			CHAIR_BIRCH = new BlockChair("birch");
			CHAIR_SPRUCE = new BlockChair("spruce");
			CHAIR_ACACIA = new BlockChair("acacia");
			CHAIR_JUNGLE = new BlockChair("jungle");
			CHAIR_OLIVE = new BlockChair("olive");
			CHAIR_IRONWOOD = new BlockChair("ironwood");
		}
		if (Config.ENABLE_TABLES) {
			TABLE_OAK = new BlockTable("oak");
			TABLE_BIG_OAK = new BlockTable("big_oak");
			TABLE_BIRCH = new BlockTable("birch");
			TABLE_SPRUCE = new BlockTable("spruce");
			TABLE_ACACIA = new BlockTable("acacia");
			TABLE_JUNGLE = new BlockTable("jungle");
			TABLE_OLIVE = new BlockTable("olive");
			TABLE_IRONWOOD = new BlockTable("ironwood");
		}
		VASE = new BlockVase();
		BARREL = new BlockBarrel();
		if (Config.ENABLE_LATTICE) {
			IRON_LATTICE = new BlockLattice(Material.iron, "iron_lattice");
		}
		IRON_LANTERN = new BlockLantern(Material.iron, "iron_lantern");
		GOLDEN_LANTERN = new BlockLantern(Material.iron, "golden_lantern");
		if (Config.ENABLE_SILVER_DECOR) {
			SILVER_LANTERN = new BlockLantern(Material.iron, "silver_lantern");
		}
		if (Config.ENABLE_PAINTED_WOOD) {
			PAINTED_WOOD_WHITE = ((BlockBase) new BlockBase(Material.wood, "painted_wood_white").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_ORANGE = ((BlockBase) new BlockBase(Material.wood, "painted_wood_orange").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_MAGENTA = ((BlockBase) new BlockBase(Material.wood, "painted_wood_magenta").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_LIGHT_BLUE = ((BlockBase) new BlockBase(Material.wood, "painted_wood_light_blue")
					.setHardness(2.0F)).setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_YELLOW = ((BlockBase) new BlockBase(Material.wood, "painted_wood_yellow").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_LIME = ((BlockBase) new BlockBase(Material.wood, "painted_wood_lime").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_PINK = ((BlockBase) new BlockBase(Material.wood, "painted_wood_pink").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_GRAY = ((BlockBase) new BlockBase(Material.wood, "painted_wood_gray").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_SILVER = ((BlockBase) new BlockBase(Material.wood, "painted_wood_silver").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_CYAN = ((BlockBase) new BlockBase(Material.wood, "painted_wood_cyan").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_PURPLE = ((BlockBase) new BlockBase(Material.wood, "painted_wood_purple").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_BLUE = ((BlockBase) new BlockBase(Material.wood, "painted_wood_blue").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_BROWN = ((BlockBase) new BlockBase(Material.wood, "painted_wood_brown").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_GREEN = ((BlockBase) new BlockBase(Material.wood, "painted_wood_green").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_RED = ((BlockBase) new BlockBase(Material.wood, "painted_wood_red").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			PAINTED_WOOD_BLACK = ((BlockBase) new BlockBase(Material.wood, "painted_wood_black").setHardness(2.0F))
					.setBlockSoundType(Block.soundTypeWood);
			Blocks.fire.setFireInfo(PAINTED_WOOD_WHITE, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_ORANGE, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_MAGENTA, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_LIGHT_BLUE, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_YELLOW, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_LIME, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_PINK, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_GRAY, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_SILVER, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_CYAN, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_PURPLE, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_BLUE, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_BROWN, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_GREEN, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_RED, 5, 20);
			Blocks.fire.setFireInfo(PAINTED_WOOD_BLACK, 5, 20);
		}
		GARGOYLE = new BlockGargoyle();
		CABINET = new BlockCabinet();
		LIQUID_BARREL = new BlockLiquidBarrel();
		FERTILE_SOIL = new BlockFertileSoil();
		PLANKS = new BlockPlanksRustic();
		LOG = new BlockLogRustic();
		LEAVES = new BlockLeavesRustic();
		SAPLING = new BlockSaplingRustic();
		CRUSHING_TUB = new BlockCrushingTub();
		EVAPORATING_BASIN = new BlockEvaporatingBasin();
		OLIVE_FENCE = new BlockFenceRustic(
				Rustic.MODID + ":planks_olive", "fence_olive");
		IRONWOOD_FENCE = new BlockFenceRustic(
				Rustic.MODID + ":planks_ironwood", "fence_ironwood");
		OLIVE_FENCE_GATE = new BlockFenceGateRustic(
				Rustic.MODID + ":planks_olive", "fence_gate_olive");
		IRONWOOD_FENCE_GATE = new BlockFenceGateRustic(
				Rustic.MODID + ":planks_ironwood", "fence_gate_ironwood");
		OLIVE_DOUBLESLAB = new BlockDoubleSlabBase(Material.wood, "olive_doubleslab", Block.soundTypeWood);
		OLIVE_SLAB = new BlockSlabBase(Material.wood, "olive_slab", OLIVE_DOUBLESLAB, Block.soundTypeWood);
		IRONWOOD_DOUBLESLAB = new BlockDoubleSlabBase(Material.wood, "ironwood_doubleslab", Block.soundTypeWood);
		IRONWOOD_SLAB = new BlockSlabBase(Material.wood, "ironwood_slab", IRONWOOD_DOUBLESLAB, Block.soundTypeWood);
		OLIVE_STAIRS = new BlockStairsBase(
				PLANKS, 0, "stairs_olive");
		Blocks.fire.setFireInfo(OLIVE_STAIRS, 5, 20);
		IRONWOOD_STAIRS = new BlockStairsBase(
				PLANKS, 1, "stairs_ironwood");
		Blocks.fire.setFireInfo(IRONWOOD_STAIRS, 5, 20);
		CONDENSER = new BlockCondenser();
		RETORT = new BlockRetort("retort");
		CONDENSER_ADVANCED = new BlockCondenserAdvanced();
		RETORT_ADVANCED = new BlockRetort("retort_advanced");
		CROP_STAKE = new BlockCropStake();
		TOMATO_CROP = new BlockStakeCrop("tomato_crop") {
			@Override
			public Item getCrop() {
				return ModItems.TOMATO;
			}

			@Override
			public Item getSeed() {
				return ModItems.TOMATO_SEEDS;
			}
		};
		CHILI_CROP = new BlockStakeCrop("chili_crop") {
			@Override
			public Item getCrop() {
				return ModItems.CHILI_PEPPER;
			}
			
			@Override
			protected Item getCrop(Random rand) {
				if (rand == null) return this.getCrop();
				return (rand.nextInt(42) == 0) ? ModItems.GHOST_PEPPER : this.getCrop();
			}

			@Override
			public Item getSeed() {
				return ModItems.CHILI_PEPPER_SEEDS;
			}

			@Override
			public int getMaxHeight() {
				return 2;
			}
		};
		WILDBERRY_BUSH = new BlockBerryBush("wildberry_bush") {
			@Override
			public Item getBerries() {
				return ModItems.WILDBERRIES;
			}
		};
		ROPE = new BlockRope("rope");
		STAKE_TIED = new BlockStakeTied();
		GRAPE_STEM = new BlockGrapeStem();
		GRAPE_LEAVES = new BlockGrapeLeaves();
		BREWING_BARREL = new BlockBrewingBarrel();
		APPLE_SEEDS = new BlockAppleSeeds();
		APPLE_SAPLING = new BlockSaplingApple();
		APPLE_LEAVES = new BlockLeavesApple();
		DOOR_OLIVE = new BlockDoorRustic(Material.wood, "olive_door");
		DOOR_IRONWOOD = new BlockDoorRustic(Material.wood, "ironwood_door");
		LANTERN_WOOD = new BlockWoodLantern();

		Herbs.init();
		
		GameRegistry.registerTileEntity(TileEntityApiary.class, Rustic.MODID + ":tileEntityApiary");
		GameRegistry.registerTileEntity(TileEntityVase.class, Rustic.MODID + ":tileEntityVase");
		GameRegistry.registerTileEntity(TileEntityBarrel.class, Rustic.MODID + ":tileEntityBarrel");
		GameRegistry.registerTileEntity(TileEntityCabinet.class, Rustic.MODID + ":tileEntityCabinet");
		GameRegistry.registerTileEntity(TileEntityLiquidBarrel.class, Rustic.MODID + ":tileEntityLiquidBarrel");
		GameRegistry.registerTileEntity(TileEntityCrushingTub.class, Rustic.MODID + ":tileEntityCrushingTub");
		GameRegistry.registerTileEntity(TileEntityEvaporatingBasin.class, Rustic.MODID + ":tileEntityEvaporatingBasin");
		GameRegistry.registerTileEntity(TileEntityCondenser.class, Rustic.MODID + ":tileEntityCondenser");
		GameRegistry.registerTileEntity(TileEntityCondenserAdvancedBottom.class, Rustic.MODID + ":tileEntityCondenserAdvanced");
		GameRegistry.registerTileEntity(TileEntityCondenserAdvancedTop.class, Rustic.MODID + ":tileEntityCondenserAdvancedProxy");
		GameRegistry.registerTileEntity(TileEntityBrewingBarrel.class, Rustic.MODID + ":tileEntityBrewingBarrel");
	}

}
