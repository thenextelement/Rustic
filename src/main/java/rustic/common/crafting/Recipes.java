package rustic.common.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import rustic.common.Config;
import rustic.common.blocks.BlockPaintedWood;
import rustic.common.blocks.BlockPlanksRustic;
import rustic.common.blocks.ModBlocks;
import rustic.common.blocks.crops.Herbs;
import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.items.ModItems;
import rustic.common.potions.PotionsRustic;

public class Recipes {

    // Do no add recipes directly to those data structures, use the public methods below
    // Do not read directly those data structures, use the public methods below
    // Those are not made private to prevent breaking changes
    public static List<ICrushingTubRecipe> crushingTubRecipes = new ArrayList<ICrushingTubRecipe>();
    public static List<IEvaporatingBasinRecipe> evaporatingRecipes = new ArrayList<IEvaporatingBasinRecipe>();
    public static HashMap<Fluid, IEvaporatingBasinRecipe> evaporatingRecipesMap = new HashMap<Fluid, IEvaporatingBasinRecipe>();
    public static List<ICondenserRecipe> condenserRecipes = new ArrayList<ICondenserRecipe>();
    public static List<IBrewingBarrelRecipe> brewingRecipes = new ArrayList<IBrewingBarrelRecipe>();

    public static void initOres() {
        addOreDictEntries();
    }

    public static void init() {
        JsonRecipes.load();
        addCraftingRecipes();
        addSmeltingRecipes();
        addFuels();
        addCrushingTubRecipes();
        addEvaporatingRecipes();
        addCondenserRecipes();
        addBrewingRecipes();
    }

    // Fix for Rustic Thaumaturgy adding recipes directly in the data structure
    public static void injectEvaporatingRecipes() {
        for (IEvaporatingBasinRecipe recipe : evaporatingRecipes) {
            evaporatingRecipesMap.putIfAbsent(recipe.getFluid(), recipe);
        }
    }

    public static void add(ICrushingTubRecipe recipe) {
        crushingTubRecipes.add(recipe);
    }

    public static void add(IEvaporatingBasinRecipe recipe) {
        evaporatingRecipesMap.put(recipe.getFluid(), recipe);
    }

    public static void add(ICondenserRecipe recipe) {
        condenserRecipes.add(recipe);
    }

    public static void add(IBrewingBarrelRecipe recipe) {
        brewingRecipes.add(recipe);
    }

    public static Collection<ICrushingTubRecipe> getCrushingRecipes() {
        return Collections.unmodifiableList(crushingTubRecipes);
    }

    public static Collection<IEvaporatingBasinRecipe> getEvaporatingRecipes() {
        return Collections.unmodifiableCollection(evaporatingRecipesMap.values());
    }

    public static Collection<ICondenserRecipe> getCondenserRecipe() {
        return Collections.unmodifiableList(condenserRecipes);
    }

    public static Collection<IBrewingBarrelRecipe> getBrewingRecipes() {
        return Collections.unmodifiableList(brewingRecipes);
    }

    public static int removeCrushingRecipe(ItemStack input) {
        int removed = 0;
        Iterator<ICrushingTubRecipe> it = Recipes.crushingTubRecipes.iterator();
        ICrushingTubRecipe r;
        while (it.hasNext()) {
            r = it.next();
            if (r != null && r.getInput()
                .isItemEqual(input)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    public static int removeCrushingRecipe(FluidStack output, ItemStack input) {
        int removed = 0;
        Iterator<ICrushingTubRecipe> it = Recipes.crushingTubRecipes.iterator();
        ICrushingTubRecipe r;
        while (it.hasNext()) {
            r = it.next();
            if (r != null && r.getResult() != null
                && r.getResult()
                    .isFluidEqual(output)) {
                if (input == null || (r.getInput()
                    .isItemEqual(input))) {
                    it.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    public static int removeEvaporatingRecipe(ItemStack output) {
        int removed = 0;
        Iterator<Map.Entry<Fluid, IEvaporatingBasinRecipe>> it = evaporatingRecipesMap.entrySet()
            .iterator();
        IEvaporatingBasinRecipe r;
        while (it.hasNext()) {
            r = it.next()
                .getValue();
            if (output.isItemEqual(r.getOutput())) {
                removed++;
                it.remove();
            }
        }
        return removed;
    }

    public static boolean removeEvaporatingRecipe(FluidStack input) {
        return (evaporatingRecipesMap.remove(input.getFluid()) != null);
    }

    public static int removeCondenserRecipe(ItemStack output) {
        int removed = 0;
        Iterator<ICondenserRecipe> it = Recipes.condenserRecipes.iterator();
        ICondenserRecipe r;
        while (it.hasNext()) {
            r = it.next();
            if (r != null && r.getResult() != null && ItemStack.areItemStacksEqual(r.getResult(), output)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    public static void postInit() {
        addSilverCraftingRecipes();
    }

    private static void addSmeltingRecipes() {
        GameRegistry.addSmelting(new ItemStack(ModItems.HONEYCOMB), new ItemStack(ModItems.BEESWAX), 0.3F);
        for (int i = 0; i < BlockPlanksRustic.EnumType.values().length; i++) {
            int meta = i;
            GameRegistry.addSmelting(new ItemStack(ModBlocks.LOG, 1, meta), new ItemStack(Items.coal, 1, 1), 0.15F);
        }
        GameRegistry.addSmelting(new ItemStack(ModItems.IRON_DUST_TINY), new ItemStack(Items.gold_nugget), 0.15F);
        GameRegistry.addSmelting(new ItemStack(ModItems.GOLD_DUST), new ItemStack(Items.gold_ingot), 0.5F);
        if (Config.FLESH_SMELTING) {
            GameRegistry.addSmelting(new ItemStack(Items.rotten_flesh), new ItemStack(ModItems.TALLOW), 0.3F);
        }
    }

    private static void addFuels() {
        IFuelHandler rusticFuels = new IFuelHandler() {

            @Override
            public int getBurnTime(ItemStack fuel) {
                Item item = fuel.getItem();
                if ((item == (Item.getItemFromBlock(ModBlocks.SAPLING))
                    || item == (Item.getItemFromBlock(ModBlocks.APPLE_SAPLING)))) {
                    return 100;
                }
                if (item == Item.getItemFromBlock(ModBlocks.WILDBERRY_BUSH)) {
                    return 200;
                }
                return 0;
            }
        };
        GameRegistry.registerFuelHandler(rusticFuels);
    }

    private static void addOreDictEntries() {
        OreDictionary.registerOre("cropOlive", new ItemStack(ModItems.OLIVES));
        OreDictionary.registerOre("cropIronberry", new ItemStack(ModItems.IRONBERRIES));
        OreDictionary.registerOre("listAllfruit", new ItemStack(ModItems.OLIVES));
        OreDictionary.registerOre("listAllfruit", new ItemStack(ModItems.IRONBERRIES));
        OreDictionary.registerOre("listAllfruit", new ItemStack(ModItems.GRAPES));
        OreDictionary.registerOre("listAllberry", new ItemStack(ModItems.GRAPES));
        OreDictionary.registerOre("listAllberry", new ItemStack(ModItems.IRONBERRIES));
        OreDictionary.registerOre("listAllberry", new ItemStack(ModItems.WILDBERRIES));
        OreDictionary.registerOre("listAllfruit", new ItemStack(ModItems.WILDBERRIES));
        OreDictionary.registerOre("cropWildberry", new ItemStack(ModItems.WILDBERRIES));
        OreDictionary.registerOre("cropGrape", new ItemStack(ModItems.GRAPES));
        OreDictionary.registerOre("cropChilipepper", new ItemStack(ModItems.CHILI_PEPPER));
        OreDictionary.registerOre("listAllpepper", new ItemStack(ModItems.CHILI_PEPPER));
        OreDictionary.registerOre("cropTomato", new ItemStack(ModItems.TOMATO));
        OreDictionary.registerOre("listAllveggie", new ItemStack(ModItems.TOMATO));

        for (int i = 0; i < BlockPlanksRustic.EnumType.values().length; i++) {
            int meta = i;
            OreDictionary.registerOre("treeWood", new ItemStack(ModBlocks.LOG, 1, meta));
            OreDictionary.registerOre("logWood", new ItemStack(ModBlocks.LOG, 1, meta));
            meta = i;
            OreDictionary.registerOre("treeSapling", new ItemStack(ModBlocks.SAPLING, 1, meta));
            meta = i;
            OreDictionary.registerOre("plankWood", new ItemStack(ModBlocks.PLANKS, 1, meta));
        }
        if (Config.ENABLE_PAINTED_WOOD) {
            OreDictionary
                .registerOre("plankWood", new ItemStack(ModBlocks.PAINTED_WOOD, 1, OreDictionary.WILDCARD_VALUE));
        }

        OreDictionary.registerOre("slabWood", new ItemStack(ModBlocks.OLIVE_SLAB));
        OreDictionary.registerOre("slabWood", new ItemStack(ModBlocks.IRONWOOD_SLAB));
        OreDictionary.registerOre("stairWood", new ItemStack(ModBlocks.OLIVE_STAIRS));
        OreDictionary.registerOre("stairWood", new ItemStack(ModBlocks.IRONWOOD_STAIRS));

        OreDictionary.registerOre("treeSapling", new ItemStack(ModBlocks.APPLE_SAPLING));
        OreDictionary.registerOre("treeLeaves", new ItemStack(ModBlocks.APPLE_LEAVES, 1, 0));
        for (int i = 0; i < 2; i++) OreDictionary.registerOre("treeLeaves", new ItemStack(ModBlocks.LEAVES, 1, i));

        if (Config.ENABLE_SLATE) {
            OreDictionary.registerOre("stoneSlate", new ItemStack(ModBlocks.SLATE));
            OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE));
            OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE_BRICK));
            OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE_TILE));
            if (Config.ENABLE_PILLARS) {
                OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE_PILLAR));
            }
            OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE_ROOF));
            OreDictionary.registerOre("slate", new ItemStack(ModBlocks.SLATE_CHISELED));
        }

        OreDictionary.registerOre("wax", new ItemStack(ModItems.BEESWAX));
        OreDictionary.registerOre("wax", new ItemStack(ModItems.TALLOW));
        OreDictionary.registerOre("tallow", new ItemStack(ModItems.BEESWAX));
        OreDictionary.registerOre("tallow", new ItemStack(ModItems.TALLOW));

        OreDictionary.registerOre("materialWaxcomb", new ItemStack(ModItems.HONEYCOMB));
        OreDictionary.registerOre("materialHoneycomb", new ItemStack(ModItems.HONEYCOMB));

        OreDictionary.registerOre("dustTinyIron", new ItemStack(ModItems.IRON_DUST_TINY));
        OreDictionary.registerOre("dustGold", new ItemStack(ModItems.GOLD_DUST));

        OreDictionary.registerOre("dyeRed", new ItemStack(ModItems.WILDBERRIES));
        OreDictionary.registerOre("dyePurple", new ItemStack(ModItems.GRAPES));
        OreDictionary.registerOre("dyeLightGray", new ItemStack(ModItems.IRONBERRIES));

        OreDictionary.registerOre("dyeBlack", new ItemStack(Herbs.VANTA_LILY));
    }

    private static void addCraftingRecipes() {
        if (Config.ENABLE_PILLARS) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.STONE_PILLAR, 6),
                    "SS ",
                    "SS ",
                    "SS ",
                    'S',
                    new ItemStack(Blocks.stone, 1, 0)));
            if (Config.ENABLE_SLATE) {
                GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                        new ItemStack(ModBlocks.SLATE_PILLAR, 6),
                        "SS",
                        "SS",
                        "SS",
                        'S',
                        new ItemStack(ModBlocks.SLATE)));
            }
        }
        if (Config.ENABLE_SLATE) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_ROOF, 4),
                    "SS",
                    "SS",
                    'S',
                    new ItemStack(ModBlocks.SLATE)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_ROOF_STAIRS, 4),
                    "S  ",
                    "SS ",
                    "SSS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_ROOF)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_ROOF_SLAB, 6),
                    "SSS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_ROOF)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_BRICK_STAIRS, 4),
                    "S  ",
                    "SS ",
                    "SSS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_BRICK)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_BRICK_SLAB, 6),
                    "SSS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_BRICK)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.SLATE_TILE), "S", 'S', new ItemStack(ModBlocks.SLATE)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_TILE),
                    "S",
                    'S',
                    new ItemStack(ModBlocks.SLATE_BRICK)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_BRICK, 4),
                    "SS",
                    "SS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_TILE)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SLATE_CHISELED, 4),
                    "SS",
                    "SS",
                    'S',
                    new ItemStack(ModBlocks.SLATE_BRICK)));
        }
        if (Config.ENABLE_CLAY_WALLS) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CLAY_WALL, 8),
                    " P ",
                    "PCP",
                    " P ",
                    'P',
                    "plankWood",
                    'C',
                    Blocks.clay));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CLAY_WALL_CROSS),
                    "P P",
                    " C ",
                    "P P",
                    'P',
                    "plankWood",
                    'C',
                    ModBlocks.CLAY_WALL));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CLAY_WALL_DIAG),
                    "P  ",
                    " C ",
                    "  P",
                    'P',
                    "plankWood",
                    'C',
                    ModBlocks.CLAY_WALL));
        }
        if (Config.ENABLE_CHAIRS) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_ACACIA, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 4),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_BIG_OAK, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 5),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_BIRCH, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 2),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_JUNGLE, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 3),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_OAK, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 0),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_SPRUCE, 4),
                    "P  ",
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 1),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_OLIVE, 4),
                    "p  ",
                    "ppp",
                    "s s",
                    'p',
                    new ItemStack(ModBlocks.PLANKS, 1, BlockPlanksRustic.EnumType.OLIVE.getMetadata()),
                    's',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHAIR_IRONWOOD, 4),
                    "p  ",
                    "ppp",
                    "s s",
                    'p',
                    new ItemStack(ModBlocks.PLANKS, 1, BlockPlanksRustic.EnumType.IRONWOOD.getMetadata()),
                    's',
                    new ItemStack(Items.stick)));
        }
        if (Config.ENABLE_TABLES) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_ACACIA, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 4),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_BIG_OAK, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 5),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_BIRCH, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 2),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_JUNGLE, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 3),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_OAK, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 0),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_SPRUCE, 2),
                    "PPP",
                    "S S",
                    'P',
                    new ItemStack(Blocks.planks, 1, 1),
                    'S',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_OLIVE, 2),
                    "ppp",
                    "s s",
                    'p',
                    new ItemStack(ModBlocks.PLANKS, 1, BlockPlanksRustic.EnumType.OLIVE.getMetadata()),
                    's',
                    new ItemStack(Items.stick)));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.TABLE_IRONWOOD, 2),
                    "ppp",
                    "s s",
                    'p',
                    new ItemStack(ModBlocks.PLANKS, 1, BlockPlanksRustic.EnumType.IRONWOOD.getMetadata()),
                    's',
                    new ItemStack(Items.stick)));
        }
        if (Config.ENABLE_LATTICE) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.IRON_LATTICE, 16),
                    " I ",
                    "III",
                    " I ",
                    'I',
                    new ItemStack(Items.iron_ingot)));
        }
        if (Config.ENABLE_PAINTED_WOOD) {
            for (int meta = 0; meta < BlockPaintedWood.DYES.length; meta++) {
                GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                        new ItemStack(ModBlocks.PAINTED_WOOD, 8, meta),
                        "PPP",
                        "PDP",
                        "PPP",
                        'P',
                        "plankWood",
                        'D',
                        BlockPaintedWood.DYES[meta]));
            }
        }

        RecipeSorter.register(
            "rustic:shapeless_nonreturn",
            RecipeNonIngredientReturn.class,
            RecipeSorter.Category.SHAPELESS,
            "after:minecraft:shapeless");
        // 1.7.10 has no universal bucket: ale wort is crafted into a fluid bottle (same 1000mb); the bucket is returned
        ItemStack aleWortBottle = ModItems.FLUID_BOTTLE.getFilledBottle(ModFluids.ALE_WORT);
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                aleWortBottle,
                new ItemStack(Items.bread),
                new ItemStack(Items.sugar),
                new ItemStack(Items.water_bucket)));

        RecipeSorter.register(
            "rustic:cabinet_recipe",
            RecipeCabinet.class,
            RecipeSorter.Category.SHAPED,
            "after:minecraft:shapeless");
        GameRegistry.addRecipe(new RecipeCabinet());

        if (Config.ENABLE_BOTTLE_EMPTYING) {
            GameRegistry.addRecipe(
                new RecipeNonIngredientReturn(new ItemStack(Items.glass_bottle), new ItemStack(ModItems.FLUID_BOTTLE)));
        }
        GameRegistry.addRecipe(
            new RecipeNonIngredientReturn(
                new ItemStack(ModBlocks.LIQUID_BARREL),
                new ItemStack(ModBlocks.LIQUID_BARREL)));

        if (Config.ENABLE_OLIVE_OILING) {
            RecipeSorter.register(
                "rustic:olive_oil",
                RecipeOliveOil.class,
                RecipeSorter.Category.SHAPELESS,
                "after:minecraft:shapeless");
            GameRegistry.addRecipe(new RecipeOliveOil());
        }

        RecipeSorter.register(
            "rustic:vanta_oil",
            RecipeVantaOil.class,
            RecipeSorter.Category.SHAPELESS,
            "after:minecraft:shapeless");
        GameRegistry.addRecipe(new RecipeVantaOil());

    }

    private static void addSilverCraftingRecipes() {
        if (Config.ENABLE_SILVER_DECOR && OreDictionary.doesOreNameExist("ingotSilver")) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CANDLE_SILVER, 4),
                    "S",
                    "W",
                    "I",
                    'S',
                    new ItemStack(Items.string),
                    'W',
                    "wax",
                    'I',
                    "ingotSilver"));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CANDLE_DOUBLE_SILVER, 4),
                    "S S",
                    "W W",
                    " I ",
                    'S',
                    new ItemStack(Items.string),
                    'W',
                    "wax",
                    'I',
                    "ingotSilver"));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CANDLE_LEVER_SILVER, 1),
                    "C",
                    "R",
                    'C',
                    new ItemStack(ModBlocks.CANDLE_SILVER),
                    'R',
                    "dustRedstone"));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.CHAIN_SILVER, 12), "I", "I", "I", 'I', "ingotSilver"));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.CHANDELIER_SILVER, 2),
                    " I ",
                    "C C",
                    "III",
                    'I',
                    "ingotSilver",
                    'C',
                    ModBlocks.CHAIN_SILVER));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModBlocks.SILVER_LANTERN, 4),
                    "I",
                    "C",
                    "I",
                    'I',
                    "ingotSilver",
                    'C',
                    new ItemStack(Items.coal, 1, OreDictionary.WILDCARD_VALUE))); // coal or charcoal
        }
    }

    private static void addCrushingTubRecipes() {
        crushingTubRecipes
            .add(new CrushingTubRecipe(new FluidStack(ModFluids.OLIVE_OIL, 250), new ItemStack(ModItems.OLIVES)));
        crushingTubRecipes.add(
            new CrushingTubRecipe(new FluidStack(ModFluids.IRONBERRY_JUICE, 250), new ItemStack(ModItems.IRONBERRIES)));
        crushingTubRecipes.add(
            new CrushingTubRecipe(
                new FluidStack(FluidRegistry.WATER, 250),
                new ItemStack(Items.reeds),
                new ItemStack(Items.sugar, 2)));
        crushingTubRecipes.add(
            new CrushingTubRecipe(new FluidStack(ModFluids.WILDBERRY_JUICE, 250), new ItemStack(ModItems.WILDBERRIES)));
        crushingTubRecipes
            .add(new CrushingTubRecipe(new FluidStack(ModFluids.GRAPE_JUICE, 250), new ItemStack(ModItems.GRAPES)));
        {
            crushingTubRecipes.add(
                new CrushingTubRecipe(
                    new FluidStack(ModFluids.APPLE_JUICE, 250),
                    new ItemStack(Items.apple),
                    new ItemStack(ModBlocks.APPLE_SEEDS)));
        }
        crushingTubRecipes
            .add(new CrushingTubRecipe(new FluidStack(ModFluids.HONEY, 250), new ItemStack(ModItems.HONEYCOMB)));
        {
            crushingTubRecipes.add(
                new CrushingTubRecipe(
                    new FluidStack(ModFluids.GOLDEN_APPLE_JUICE, 100),
                    new ItemStack(Items.golden_apple, 1, 0),
                    new ItemStack(ModBlocks.APPLE_SEEDS)));
            crushingTubRecipes.add(
                new CrushingTubRecipe(
                    new FluidStack(ModFluids.GOLDEN_APPLE_JUICE, 1000),
                    new ItemStack(Items.golden_apple, 1, 1),
                    new ItemStack(ModBlocks.APPLE_SEEDS)));
        }
        crushingTubRecipes
            .add(new CrushingTubRecipe(new FluidStack(ModFluids.VANTA_OIL, 250), new ItemStack(Herbs.VANTA_LILY)));
    }

    private static void addEvaporatingRecipes() {
        evaporatingRecipesMap.put(
            ModFluids.IRONBERRY_JUICE,
            new EvaporatingBasinRecipe(
                new ItemStack(ModItems.IRON_DUST_TINY, 1),
                new FluidStack(ModFluids.IRONBERRY_JUICE, 500)));
        evaporatingRecipesMap.put(
            ModFluids.GOLDEN_APPLE_JUICE,
            new EvaporatingBasinRecipe(
                new ItemStack(ModItems.GOLD_DUST, 1),
                new FluidStack(ModFluids.GOLDEN_APPLE_JUICE, 100)));
    }

    private static void addCondenserRecipes() {
        condenserRecipes.add(
            new BasicCondenserRecipe(
                new PotionEffect(Potion.heal.id, 1),
                new ItemStack(Herbs.CHAMOMILE),
                new ItemStack(Items.beef)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.heal.id, 1, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.CHAMOMILE),
                new ItemStack(Items.beef)));
        condenserRecipes.add(
            new BasicCondenserRecipe(
                new PotionEffect(Potion.regeneration.id, 900),
                new ItemStack(Herbs.COHOSH),
                new ItemStack(ModItems.HONEYCOMB)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.regeneration.id, 1800),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.COHOSH),
                new ItemStack(ModItems.HONEYCOMB)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.regeneration.id, 450, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.COHOSH),
                new ItemStack(ModItems.HONEYCOMB)));
        condenserRecipes.add(
            new BasicCondenserRecipe(
                new PotionEffect(Potion.wither.id, 900),
                new ItemStack(Herbs.DEATHSTALK),
                new ItemStack(Blocks.soul_sand)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.wither.id, 1800),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.DEATHSTALK),
                new ItemStack(Blocks.soul_sand)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.wither.id, 450, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.DEATHSTALK),
                new ItemStack(Blocks.soul_sand)));
        condenserRecipes.add(
            new BasicCondenserRecipe(
                new PotionEffect(Potion.nightVision.id, 3600),
                new ItemStack(Herbs.MOONCAP),
                new ItemStack(Items.spider_eye)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.nightVision.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.MOONCAP),
                new ItemStack(Items.spider_eye)));
        condenserRecipes.add(
            new BasicCondenserRecipe(
                new PotionEffect(Potion.moveSpeed.id, 3600),
                new ItemStack(Herbs.WIND_THISTLE),
                new ItemStack(Items.sugar)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.moveSpeed.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.WIND_THISTLE),
                new ItemStack(Items.sugar)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.moveSpeed.id, 1800, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.WIND_THISTLE),
                new ItemStack(Items.sugar)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.fireResistance.id, 3600),
                null,
                new ItemStack(Herbs.ALOE_VERA),
                new ItemStack(Items.brick),
                new ItemStack(Items.coal)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.fireResistance.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.ALOE_VERA),
                new ItemStack(Items.brick),
                new ItemStack(Items.coal)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.field_76434_w.id, 3600),
                null,
                new ItemStack(Herbs.BLOOD_ORCHID),
                new ItemStack(Items.rotten_flesh),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.field_76434_w.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.BLOOD_ORCHID),
                new ItemStack(Items.rotten_flesh),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.field_76434_w.id, 1800, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.BLOOD_ORCHID),
                new ItemStack(Items.rotten_flesh),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.digSpeed.id, 3600),
                null,
                new ItemStack(Herbs.CORE_ROOT),
                new ItemStack(Items.gold_nugget),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.digSpeed.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.CORE_ROOT),
                new ItemStack(Items.gold_nugget),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.digSpeed.id, 1800, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.CORE_ROOT),
                new ItemStack(Items.gold_nugget),
                new ItemStack(Items.redstone)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.damageBoost.id, 3600),
                null,
                new ItemStack(Herbs.GINSENG),
                new ItemStack(Items.bone),
                new ItemStack(Items.gunpowder)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.damageBoost.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.GINSENG),
                new ItemStack(Items.bone),
                new ItemStack(Items.gunpowder)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(Potion.damageBoost.id, 1800, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(Herbs.GINSENG),
                new ItemStack(Items.bone),
                new ItemStack(Items.gunpowder)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.IRON_SKIN_POTION.id, 3600),
                null,
                new ItemStack(ModItems.IRONBERRIES),
                new ItemStack(Items.leather),
                new ItemStack(Items.clay_ball)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.IRON_SKIN_POTION.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(ModItems.IRONBERRIES),
                new ItemStack(Items.leather),
                new ItemStack(Items.clay_ball)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.IRON_SKIN_POTION.id, 1800, 1),
                new ItemStack(Herbs.MARSH_MALLOW),
                new ItemStack(ModItems.IRONBERRIES),
                new ItemStack(Items.leather),
                new ItemStack(Items.clay_ball)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.FEATHER_POTION.id, 3600),
                null,
                new ItemStack(Herbs.CLOUDSBLUFF),
                new ItemStack(Items.feather),
                new ItemStack(Items.paper)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.FEATHER_POTION.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(Herbs.CLOUDSBLUFF),
                new ItemStack(Items.feather),
                new ItemStack(Items.paper)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.BLAZING_TRAIL_POTION.id, 3600),
                null,
                new ItemStack(ModItems.CHILI_PEPPER),
                new ItemStack(Items.blaze_powder),
                new ItemStack(Blocks.netherrack)));
        condenserRecipes.add(
            new AdvancedCondenserRecipe(
                new PotionEffect(PotionsRustic.BLAZING_TRAIL_POTION.id, 9600),
                new ItemStack(Herbs.HORSETAIL),
                new ItemStack(ModItems.CHILI_PEPPER),
                new ItemStack(Items.blaze_powder),
                new ItemStack(Blocks.netherrack)));
    }

    private static void addBrewingRecipes() {
        brewingRecipes
            .add(new BrewingBarrelRecipe(new FluidStack(ModFluids.ALE, 1), new FluidStack(ModFluids.ALE_WORT, 1)));
        brewingRecipes
            .add(new BrewingBarrelRecipe(new FluidStack(ModFluids.CIDER, 1), new FluidStack(ModFluids.APPLE_JUICE, 1)));
        brewingRecipes.add(
            new BrewingBarrelRecipe(
                new FluidStack(ModFluids.IRON_WINE, 1),
                new FluidStack(ModFluids.IRONBERRY_JUICE, 1)));
        brewingRecipes
            .add(new BrewingBarrelRecipe(new FluidStack(ModFluids.MEAD, 1), new FluidStack(ModFluids.HONEY, 1)));
        if (FluidRegistry.isFluidRegistered("for.honey")) {
            brewingRecipes.add(
                new BrewingBarrelRecipe(
                    new FluidStack(ModFluids.MEAD, 1),
                    new FluidStack(FluidRegistry.getFluid("for.honey"), 1)));
        }
        brewingRecipes.add(
            new BrewingBarrelRecipe(
                new FluidStack(ModFluids.WILDBERRY_WINE, 1),
                new FluidStack(ModFluids.WILDBERRY_JUICE, 1)));
        brewingRecipes
            .add(new BrewingBarrelRecipe(new FluidStack(ModFluids.WINE, 1), new FluidStack(ModFluids.GRAPE_JUICE, 1)));
        brewingRecipes.add(
            new BrewingBarrelRecipe(
                new FluidStack(ModFluids.AMBROSIA, 1),
                new FluidStack(ModFluids.GOLDEN_APPLE_JUICE, 1)) {

                @Override
                public FluidStack getResult(FluidStack in) {
                    if (matches(in) && output != null && output.getFluid() != null) {
                        FluidStack out = output.copy();
                        if (output.getFluid() instanceof FluidBooze) {
                            if (out.tag == null) {
                                out.tag = new NBTTagCompound();
                            }
                            int r;
                            if (rand.nextInt(4) == 0) {
                                r = rand.nextInt(26);
                            } else {
                                r = rand.nextInt(12) + 14;
                            }
                            out.tag.setFloat(FluidBooze.QUALITY_NBT_KEY, ((49 + r) / 100F));
                        }
                        return out;
                    }
                    return null;
                }

                @Override
                public FluidStack getResult(FluidStack in, FluidStack aux) {
                    if (aux == null || aux.getFluid() == null
                        || !matches(in, aux)
                        || !(aux.getFluid() instanceof FluidBooze)) {
                        return getResult(in);
                    }
                    if (matches(in, aux) && output != null && output.getFluid() != null) {
                        FluidStack out = output.copy();
                        if (output.getFluid() instanceof FluidBooze && aux.tag != null
                            && aux.tag.hasKey(FluidBooze.QUALITY_NBT_KEY)) {
                            float auxQuality = aux.tag.getFloat(FluidBooze.QUALITY_NBT_KEY);
                            if (Config.MAX_BREW_QUALITY_CHANGE < Config.MIN_BREW_QUALITY_CHANGE) {
                                Config.MAX_BREW_QUALITY_CHANGE = Config.MIN_BREW_QUALITY_CHANGE;
                            }
                            int minChange = Math.max(0, Config.MIN_BREW_QUALITY_CHANGE);
                            int maxChange = Math.max(7, Config.MAX_BREW_QUALITY_CHANGE);

                            int brewQualityChange = rand.nextInt((maxChange - minChange) + 1) + minChange;
                            float quality = Math
                                .max(Math.min(((brewQualityChange + (int) (100 * auxQuality)) / 100F), 1), 0);
                            if (out.tag == null) {
                                out.tag = new NBTTagCompound();
                            }
                            out.tag.setFloat(FluidBooze.QUALITY_NBT_KEY, quality);
                        }
                        return out;
                    }
                    return null;
                }
            });
    }

}
