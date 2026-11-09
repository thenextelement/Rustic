package rustic.compat.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import rustic.common.Config;
import rustic.common.blocks.ModBlocks;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.crafting.RecipeVantaOil;
import rustic.common.items.ModItems;
import rustic.common.util.ElixirUtils;
import rustic.common.util.RusticUtils;

/**
 * Display versions of Rustic's custom crafting-table recipes (cabinets, olive oiling, vanta oiling). They are custom
 * {@link net.minecraft.item.crafting.IRecipe}s that NEI's stock crafting handlers can't read, so
 * {@link RusticShapedRecipeHandler} / {@link RusticShapelessRecipeHandler} add them to the normal crafting tabs.
 */
final class RusticCraftingRecipes {

    private RusticCraftingRecipes() {}

    static final class Recipe {

        /** Shaped: 9 entries, row-major, null = empty. Shapeless: the ingredients. Entries: ItemStack or List. */
        final Object[] inputs;
        final ItemStack result;
        final boolean shapeless;
        /** Vanta oiling: inputs[0] and the result are the same weapon. */
        final boolean vanta;
        /** Lang key of a note shown on the result's tooltip, or null. */
        final String note;

        Recipe(Object[] inputs, ItemStack result, boolean shapeless, boolean vanta, String note) {
            this.inputs = inputs;
            this.result = result;
            this.shapeless = shapeless;
            this.vanta = vanta;
            this.note = note;
        }

        boolean isVanta() {
            return vanta;
        }
    }

    private static List<Recipe> recipes;

    static List<Recipe> all() {
        if (recipes == null) recipes = build();
        return recipes;
    }

    static List<Recipe> of(boolean shapeless) {
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : all()) if (r.shapeless == shapeless) out.add(r);
        return out;
    }

    private static List<Recipe> build() {
        List<Recipe> list = new ArrayList<>();

        // Cabinet: planks around, a trapdoor on one side (RecipeCabinet).
        List<ItemStack> planks = OreDictionary.getOres("plankWood");
        List<ItemStack> trapdoors = new ArrayList<>(OreDictionary.getOres("trapdoorWood"));
        if (trapdoors.isEmpty()) trapdoors.add(new ItemStack(Blocks.trapdoor));
        list.add(
            new Recipe(
                new Object[] { planks, planks, planks, planks, null, trapdoors, planks, planks, planks },
                new ItemStack(ModBlocks.CABINET),
                false,
                false,
                null));

        // Olive oiling (RecipeOliveOil): any allowed food + a bottle of olive oil.
        if (Config.ENABLE_OLIVE_OILING) {
            ItemStack oil = ModItems.FLUID_BOTTLE.getFilledBottle(ModFluids.OLIVE_OIL);
            for (Object o : Item.itemRegistry) {
                if (!(o instanceof ItemFood)) continue;
                Item item = (Item) o;
                String name = RusticUtils.getRegistryName(item);
                if (Config.OLIVE_OIL_USE_WHITELIST != Config.OLIVE_OIL_BLACKLIST.contains(name)) continue;
                ItemStack food = new ItemStack(item);
                ItemStack oiled = food.copy();
                oiled.setTagInfo("oiled", new NBTTagByte((byte) 0));
                list.add(new Recipe(new Object[] { food, oil }, oiled, true, false, null));
            }
        }

        // Vanta oiling (RecipeVantaOil): weapon + vanta oil + a single-effect potion or elixir.
        List<ItemStack> weapons = new ArrayList<>();
        List<ItemStack> potions = new ArrayList<>();
        for (Object o : Item.itemRegistry) {
            for (ItemStack stack : RusticUtils.getSubItems((Item) o)) {
                if (RusticUtils.isVantaOilableWeapon(stack)) weapons.add(stack);
                else if (stack.getItem() == Items.potionitem || stack.getItem() == ModItems.ELIXIR) {
                    if (RecipeVantaOil.getIngredientEffect(stack) != null) potions.add(stack);
                }
            }
        }
        if (!weapons.isEmpty() && !potions.isEmpty()) {
            ItemStack vanta = ModItems.FLUID_BOTTLE.getFilledBottle(ModFluids.VANTA_OIL);
            // one recipe per weapon: NEI groups same-shaped recipes and cycles them as a whole, which keeps each weapon
            // in step with its output (a single recipe with a weapon list would cycle input and output separately)
            for (ItemStack weapon : weapons) {
                list.add(
                    new Recipe(new Object[] { weapon, vanta, potions }, weapon, true, true, "rustic.nei.vanta_oil"));
            }
        }
        return list;
    }

    /** Whether looking up {@code result} should show {@code r}. */
    static boolean produces(Recipe r, ItemStack result) {
        if (result == null) return false;
        if (r.isVanta()) {
            // only an already oiled weapon leads to the vanta recipe
            return ElixirUtils.getVantaOilTag(result) != null && contains(r.inputs[0], result);
        }
        if (r.result.getItem() != result.getItem()) return false;
        // olive-oiled food: only when looking up the oiled variant
        return !r.shapeless || result.hasTagCompound() && result.getTagCompound()
            .hasKey("oiled");
    }

    static boolean uses(Recipe r, ItemStack ingredient) {
        for (Object o : r.inputs) if (contains(o, ingredient)) return true;
        return false;
    }

    static boolean contains(Object slot, ItemStack stack) {
        if (slot == null || stack == null) return false;
        if (slot instanceof ItemStack) return matches((ItemStack) slot, stack);
        for (Object o : (List<?>) slot) if (matches((ItemStack) o, stack)) return true;
        return false;
    }

    private static boolean matches(ItemStack a, ItemStack b) {
        if (a.getItem() != b.getItem()) return false;
        if (a.getItemDamage() != b.getItemDamage() && a.getItemDamage() != OreDictionary.WILDCARD_VALUE
            && !a.getItem()
                .isDamageable())
            return false;
        if (a.getItem() == ModItems.FLUID_BOTTLE || a.getItem() == Items.potionitem || a.getItem() == ModItems.ELIXIR) {
            NBTTagCompound ta = a.getTagCompound(), tb = b.getTagCompound();
            return ta == null ? tb == null : ta.equals(tb);
        }
        return true;
    }

    /** The weapon without its vanta oil, as it goes into the recipe. */
    static ItemStack stripVanta(ItemStack weapon) {
        ItemStack copy = weapon.copy();
        copy.stackSize = 1;
        if (copy.hasTagCompound()) {
            copy.getTagCompound()
                .removeTag("vanta_oil");
            if (copy.getTagCompound()
                .hasNoTags()) copy.setTagCompound(null);
        }
        return copy;
    }
}
