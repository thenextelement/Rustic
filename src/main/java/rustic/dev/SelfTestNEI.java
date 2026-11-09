package rustic.dev;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;

/** Calls every NEI recipe / usage handler with every Rustic item, the way pressing R / U does. */
final class SelfTestNEI {

    private SelfTestNEI() {}

    /** Draws a stack exactly as NEI's item panel and recipe screens do. */
    static void drawLikeNEI(int x, int y, ItemStack stack) {
        codechicken.nei.guihook.GuiContainerManager.enable3DRender();
        codechicken.nei.guihook.GuiContainerManager.drawItem(x, y, stack);
        codechicken.nei.guihook.GuiContainerManager.enable2DRender();
    }

    /** Rustic's custom crafting recipes must show up in NEI's normal shaped / shapeless tabs. */
    private static void checkCraftingTabs() {
        boolean stockShaped = false, stockShapeless = false;
        codechicken.nei.recipe.ICraftingHandler shaped = null, shapeless = null;
        for (ICraftingHandler h : GuiCraftingRecipe.craftinghandlers) {
            if (h.getClass() == codechicken.nei.recipe.ShapedRecipeHandler.class) stockShaped = true;
            if (h.getClass() == codechicken.nei.recipe.ShapelessRecipeHandler.class) stockShapeless = true;
            if (h instanceof rustic.compat.nei.RusticShapedRecipeHandler) shaped = h;
            if (h instanceof rustic.compat.nei.RusticShapelessRecipeHandler) shapeless = h;
        }
        SelfTestLog.check(shaped != null && !stockShaped, "NEI shaped handler not replaced");
        SelfTestLog.check(shapeless != null && !stockShapeless, "NEI shapeless handler not replaced");
        if (shaped == null || shapeless == null) return;
        SelfTestLog.check(
            "codechicken.nei.recipe.ShapedRecipeHandler".equals(shaped.getHandlerId()),
            "shaped handler id changed: {}",
            shaped.getHandlerId());

        int cabinet = shaped.getRecipeHandler("item", new ItemStack(rustic.common.blocks.ModBlocks.CABINET))
            .numRecipes();
        SelfTestLog.check(cabinet > 0, "cabinet recipe missing from the shaped crafting tab");
        int all = shaped.getRecipeHandler("crafting")
            .numRecipes();
        SelfTestLog.check(all > 100, "shaped 'show all' lists only {} recipes", all);

        ItemStack oiled = new ItemStack(Items.bread);
        oiled.setTagInfo("oiled", new net.minecraft.nbt.NBTTagByte((byte) 0));
        int oil = shapeless.getRecipeHandler("item", oiled)
            .numRecipes();
        SelfTestLog.check(oil > 0, "olive-oiled bread recipe missing from the shapeless crafting tab");
        ItemStack vanta = rustic.common.items.ModItems.FLUID_BOTTLE
            .getFilledBottle(rustic.common.blocks.fluids.ModFluids.VANTA_OIL);
        int vantaUses = ((IUsageHandler) shapeless).getUsageHandler("item", vanta)
            .numRecipes();
        SelfTestLog.check(vantaUses > 0, "vanta oil has no shapeless crafting usage");
        int shapelessAll = shapeless.getRecipeHandler("crafting")
            .numRecipes();
        SelfTestLog.log(
            "NEI crafting tabs: cabinet {}, shaped all {}, oiled bread {}, vanta uses {}, shapeless all {}",
            cabinet,
            all,
            oil,
            vantaUses,
            shapelessAll);
    }

    private static int usages(ItemStack stack) {
        int n = 0;
        for (IUsageHandler h : new ArrayList<>(GuiUsageRecipe.usagehandlers)) {
            if (!(h instanceof rustic.compat.nei.RusticRecipeHandler)) continue;
            try {
                n += h.getUsageAndCatalystHandler("item", stack)
                    .numRecipes();
            } catch (Throwable t) {
                SelfTestLog.fail("usage lookup for " + stack, t);
            }
        }
        return n;
    }

    /** Every ingredient and catalyst of Rustic's machines must lead to its recipes with U. */
    private static void checkMachineUsages() {
        int checked = 0;
        for (rustic.common.crafting.ICondenserRecipe r : rustic.common.crafting.Recipes.condenserRecipes) {
            for (List<ItemStack> input : r.getInputs()) {
                if (input.isEmpty()) continue;
                checked++;
                SelfTestLog.check(
                    usages(input.get(0)) > 0,
                    "condenser ingredient {} shows no uses in NEI",
                    input.get(0)
                        .getDisplayName());
            }
        }
        for (rustic.common.crafting.ICrushingTubRecipe r : rustic.common.crafting.Recipes.crushingTubRecipes) {
            checked++;
            SelfTestLog.check(
                usages(r.getInput()) > 0,
                "crushing tub input {} shows no uses in NEI",
                r.getInput()
                    .getDisplayName());
        }
        for (rustic.common.crafting.IEvaporatingBasinRecipe r : rustic.common.crafting.Recipes.evaporatingRecipesMap
            .values()) {
            checked++;
            ItemStack display = codechicken.nei.item.ItemFluidDisplay.createStack(r.getInput());
            SelfTestLog.check(
                display != null && usages(display) > 0,
                "evaporating fluid {} shows no uses in NEI",
                r.getInput()
                    .getLocalizedName());
        }
        net.minecraft.block.Block[] machines = { rustic.common.blocks.ModBlocks.CRUSHING_TUB,
            rustic.common.blocks.ModBlocks.EVAPORATING_BASIN, rustic.common.blocks.ModBlocks.BREWING_BARREL,
            rustic.common.blocks.ModBlocks.CONDENSER, rustic.common.blocks.ModBlocks.CONDENSER_ADVANCED };
        for (net.minecraft.block.Block b : machines) {
            checked++;
            SelfTestLog.check(
                usages(new ItemStack(b)) > 0,
                "machine {} lists no recipes on U (catalyst)",
                new ItemStack(b).getDisplayName());
        }
        Object[] herbs = { rustic.common.blocks.crops.Herbs.ALOE_VERA, rustic.common.blocks.crops.Herbs.HORSETAIL,
            rustic.common.blocks.crops.Herbs.CLOUDSBLUFF, rustic.common.blocks.crops.Herbs.MARSH_MALLOW,
            rustic.common.blocks.crops.Herbs.CORE_ROOT, rustic.common.blocks.crops.Herbs.GINSENG,
            rustic.common.blocks.crops.Herbs.BLOOD_ORCHID };
        for (Object h : herbs) {
            ItemStack stack = h instanceof net.minecraft.block.Block ? new ItemStack((net.minecraft.block.Block) h)
                : new ItemStack((net.minecraft.item.Item) h);
            checked++;
            int n = usages(stack);
            SelfTestLog.log("herb {}: {} uses", stack.getDisplayName(), n);
            SelfTestLog.check(n > 0, "herb {} shows no uses in NEI", stack.getDisplayName());
        }
        SelfTestLog.log("checked NEI usages of {} machine ingredients / catalysts", checked);
    }

    static void openHandler(String id) {
        GuiCraftingRecipe.openRecipeGui(id);
    }

    static void openRecipes(ItemStack stack) {
        GuiCraftingRecipe.openRecipeGui("item", stack);
    }

    static void openUsages(ItemStack stack) {
        GuiUsageRecipe.openRecipeGui("item", stack);
    }

    static void search(String text) {
        codechicken.nei.NEIClientConfig.setSearchExpression(text);
    }

    static void run(List<ItemStack> rusticItems) {
        List<ItemStack> stacks = new ArrayList<>(rusticItems);
        // vanilla items that feed Rustic recipes (potions with and without effects, bottles, food, weapons)
        stacks.add(new ItemStack(Items.potionitem, 1, 0));
        stacks.add(new ItemStack(Items.potionitem, 1, 8193));
        stacks.add(new ItemStack(Items.glass_bottle));
        stacks.add(new ItemStack(Items.apple));
        stacks.add(new ItemStack(Items.iron_sword));
        stacks.add(new ItemStack(Items.water_bucket));
        int calls = 0;
        for (ItemStack stack : stacks) {
            for (ICraftingHandler h : new ArrayList<>(GuiCraftingRecipe.craftinghandlers)) {
                calls++;
                try {
                    h.getRecipeHandler("item", stack);
                } catch (Throwable t) {
                    SelfTestLog.fail(
                        "NEI recipe lookup " + h.getClass()
                            .getName() + " for " + stack,
                        t);
                }
            }
            for (IUsageHandler h : new ArrayList<>(GuiUsageRecipe.usagehandlers)) {
                calls++;
                try {
                    h.getUsageAndCatalystHandler("item", stack);
                } catch (Throwable t) {
                    SelfTestLog.fail(
                        "NEI usage lookup " + h.getClass()
                            .getName() + " for " + stack,
                        t);
                }
            }
        }
        // the handlers' own "show all" ids (clicking a GUI arrow / tab)
        for (String id : new String[] { "rustic.crushing_tub", "rustic.evaporating", "rustic.brewing",
            "rustic.alchemy_simple", "rustic.alchemy_advanced" }) {
            for (ICraftingHandler h : new ArrayList<>(GuiCraftingRecipe.craftinghandlers)) {
                calls++;
                try {
                    h.getRecipeHandler(id);
                } catch (Throwable t) {
                    SelfTestLog.fail(
                        "NEI id lookup " + id
                            + " in "
                            + h.getClass()
                                .getName(),
                        t);
                }
            }
        }
        SelfTestLog.log("NEI: {} handler calls over {} stacks", calls, stacks.size());
        checkCraftingTabs();
        checkMachineUsages();
    }
}
