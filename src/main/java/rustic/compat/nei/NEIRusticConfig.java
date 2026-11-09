package rustic.compat.nei;

import java.awt.Rectangle;
import java.util.Collections;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.RecipeTransferRectHandler;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import rustic.client.gui.GuiBrewingBarrel;
import rustic.client.gui.GuiCondenser;
import rustic.client.gui.GuiCondenserAdvanced;
import rustic.common.blocks.ModBlocks;
import rustic.core.Rustic;

/**
 * NEI integration (replaces the 1.12 JEI plugin). NEI discovers this class by name, so it is only ever loaded when NEI
 * is installed.
 */
public class NEIRusticConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        RusticNEIHandlers.CrushingTub crushing = new RusticNEIHandlers.CrushingTub();
        RusticNEIHandlers.Evaporating evaporating = new RusticNEIHandlers.Evaporating();
        RusticNEIHandlers.Brewing brewing = new RusticNEIHandlers.Brewing();
        RusticNEIHandlers.Alchemy simple = new RusticNEIHandlers.Alchemy(false);
        RusticNEIHandlers.Alchemy advanced = new RusticNEIHandlers.Alchemy(true);

        register(crushing);
        register(evaporating);
        register(brewing);
        register(simple);
        register(advanced);
        replaceCraftingHandlers();

        API.addRecipeCatalyst(new ItemStack(ModBlocks.CRUSHING_TUB), crushing);
        API.addRecipeCatalyst(new ItemStack(ModBlocks.EVAPORATING_BASIN), evaporating);
        API.addRecipeCatalyst(new ItemStack(ModBlocks.BREWING_BARREL), brewing);
        API.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSER), simple);
        API.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSER_ADVANCED), simple);
        API.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSER_ADVANCED), advanced);

        // Clicking the progress arrow in the machine GUIs opens the matching recipes (JEI click areas in 1.12).
        // NEI offsets GUI rects by the default recipe-area origin (5, 11).
        guiClickArea(GuiBrewingBarrel.class, 85, 35, 24, 16, brewing.getId());
        guiClickArea(GuiCondenser.class, 44, 29, 50, 28, simple.getId());
        guiClickArea(GuiCondenserAdvanced.class, 44, 25, 50, 35, advanced.getId());
    }

    private static void guiClickArea(Class<? extends GuiContainer> gui, int x, int y, int w, int h, String id) {
        RecipeTransferRectHandler.registerRectsToGuis(
            Collections.<Class<? extends GuiContainer>>singletonList(gui),
            Collections
                .singletonList(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(x - 5, y - 11, w, h), id)));
    }

    /**
     * Swaps NEI's stock shaped / shapeless crafting handlers for subclasses that also show Rustic's custom crafting
     * recipes. They keep the stock handler ids, so the recipes land in the normal crafting tabs.
     */
    private static void replaceCraftingHandlers() {
        boolean shaped = false, shapeless = false;
        for (java.util.List<codechicken.nei.recipe.ICraftingHandler> list : java.util.Arrays
            .asList(GuiCraftingRecipe.craftinghandlers, GuiCraftingRecipe.serialCraftingHandlers)) {
            synchronized (list) {
                for (int i = 0; i < list.size(); i++) {
                    Class<?> c = list.get(i)
                        .getClass();
                    if (c == ShapedRecipeHandler.class) {
                        list.set(i, new RusticShapedRecipeHandler());
                        shaped = true;
                    } else if (c == ShapelessRecipeHandler.class) {
                        list.set(i, new RusticShapelessRecipeHandler());
                        shapeless = true;
                    }
                }
            }
        }
        for (java.util.List<codechicken.nei.recipe.IUsageHandler> list : java.util.Arrays
            .asList(GuiUsageRecipe.usagehandlers, GuiUsageRecipe.serialUsageHandlers)) {
            synchronized (list) {
                for (int i = 0; i < list.size(); i++) {
                    Class<?> c = list.get(i)
                        .getClass();
                    if (c == ShapedRecipeHandler.class) list.set(i, new RusticShapedRecipeHandler());
                    else if (c == ShapelessRecipeHandler.class) list.set(i, new RusticShapelessRecipeHandler());
                }
            }
        }
        if (!shaped || !shapeless) {
            Rustic.logger
                .warn("NEI's stock crafting handlers were not found; Rustic's crafting recipes may be missing");
        }
    }

    private static void register(TemplateRecipeHandler handler) {
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }

    @Override
    public String getName() {
        return Rustic.NAME;
    }

    @Override
    public String getVersion() {
        return Rustic.VERSION;
    }

}
