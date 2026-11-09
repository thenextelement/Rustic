package rustic.compat.nei;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.item.ItemFluidDisplay;
import codechicken.nei.recipe.StackInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import rustic.core.Rustic;

/**
 * Base for Rustic's NEI handlers (the 1.7.10 replacement for the JEI categories). Draws the JEI background textures
 * centred in NEI's 166px wide recipe area, and shows fluids as NEI fluid display stacks so R/U lookups work on them.
 */
public abstract class RusticRecipeHandler<R> extends TemplateRecipeHandler {

    public static final int AREA_WIDTH = 166;

    protected final String id;
    protected final String texture;
    protected final int bgWidth;
    protected final int bgHeight;
    protected final int offX;

    protected RusticRecipeHandler(String id, String texture, int bgWidth, int bgHeight) {
        this.id = id;
        this.texture = Rustic.MODID + ":textures/gui/" + texture + ".png";
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
        this.offX = (AREA_WIDTH - bgWidth) / 2;
        addTransferRect();
    }

    /** Unique recipe id (also the NEI overlay identifier and the JEI category uid it replaces). */
    /**
     * NEI de-duplicates handlers by this id, which defaults to the class name; the simple and advanced alchemy
     * handlers share a class, so without this the advanced one was silently dropped.
     */
    @Override
    public String getHandlerId() {
        return id;
    }

    public String getId() {
        return id;
    }

    public int getHeight() {
        return bgHeight + 4;
    }

    protected abstract String getNameKey();

    protected abstract Collection<? extends R> getRecipes();

    protected abstract CachedRecipe createCached(R recipe);

    protected abstract boolean produces(R recipe, ItemStack result, FluidStack resultFluid);

    protected abstract boolean uses(R recipe, ItemStack ingredient, FluidStack ingredientFluid);

    /** Optional click area (in background coordinates) that lists all of this handler's recipes. */
    protected Rectangle getTransferArea() {
        return null;
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal(getNameKey());
    }

    @Override
    public String getGuiTexture() {
        return texture;
    }

    @Override
    public String getOverlayIdentifier() {
        return id;
    }

    @Override
    public int getRecipeHeight(int recipe) {
        return getHeight();
    }

    @Override
    public void loadTransferRects() {
        // called from the superclass constructor, before id/offX are set; see addTransferRect()
    }

    /** Must be called at the end of each constructor, once {@link #id} is set. */
    protected void addTransferRect() {
        // NEI only treats a catalyst (U on the machine) as such when the handler has a transfer rect, so handlers
        // without a clickable arrow get an empty one
        Rectangle r = getTransferArea();
        if (r == null) r = new Rectangle(0, 0, 0, 0);
        transferRects.add(new RecipeTransferRect(new Rectangle(r.x + offX, r.y, r.width, r.height), id));
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GuiDraw.changeTexture(texture);
        GuiDraw.drawTexturedModalRect(offX, 0, 0, 0, bgWidth, bgHeight);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (id.equals(outputId)) {
            for (R recipe : getRecipes()) add(recipe);
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack fluid = StackInfo.getFluid(result);
        boolean display = StackInfo.isFluidDisplayItem(result);
        for (R recipe : getRecipes()) {
            if (produces(recipe, display ? null : result, fluid)) add(recipe);
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (id.equals(inputId)) {
            for (R recipe : getRecipes()) add(recipe);
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        FluidStack fluid = StackInfo.getFluid(ingredient);
        boolean display = StackInfo.isFluidDisplayItem(ingredient);
        for (R recipe : getRecipes()) {
            if (uses(recipe, display ? null : ingredient, fluid)) add(recipe);
        }
    }

    private void add(R recipe) {
        try {
            CachedRecipe cached = createCached(recipe);
            if (cached != null) arecipes.add(cached);
        } catch (RuntimeException e) {
            Rustic.logger.warn("Skipping a Rustic recipe NEI could not display", e);
        }
    }

    /* Helpers for subclasses; positions are in background coordinates (JEI slot origin, 1px border included). */

    protected PositionedStack slot(Object stack, int x, int y) {
        if (stack == null) return null;
        if (stack instanceof List && ((List<?>) stack).isEmpty()) return null;
        return new PositionedStack(stack, x + 1 + offX, y + 1, true);
    }

    /**
     * A fluid drawn filling the tank frame whose inner (grey) area starts at (x, y) in the background texture. Unlike
     * {@link #slot}, no border offset is added: NEI draws the fluid over the whole w x h area.
     */
    protected PositionedStack tank(FluidStack fluid, int x, int y, int w, int h) {
        if (fluid == null || fluid.getFluid() == null) return null;
        PositionedStack ps = new PositionedStack(ItemFluidDisplay.createStack(fluid), x + offX, y, false);
        ps.width = w;
        ps.height = h;
        return ps;
    }

    protected static boolean matches(ItemStack wanted, ItemStack candidate) {
        if (wanted == null || candidate == null) return false;
        return NEIServerUtils.areStacksSameTypeCrafting(wanted, candidate)
            || OreDictionary.itemMatches(candidate, wanted, false);
    }

    protected static boolean matchesAny(ItemStack wanted, Collection<ItemStack> candidates) {
        if (wanted == null || candidates == null) return false;
        for (ItemStack c : candidates) if (matches(wanted, c)) return true;
        return false;
    }

    protected static boolean sameFluid(FluidStack a, FluidStack b) {
        return a != null && b != null && a.getFluid() != null && a.getFluid() == b.getFluid();
    }

    /** Draws a progress sprite from the handler texture, in background coordinates. */
    protected void drawSprite(int x, int y, int u, int v, int w, int h, int ticks, int direction) {
        drawProgressBar(x + offX, y, u, v, w, h, ticks, direction);
    }

}
