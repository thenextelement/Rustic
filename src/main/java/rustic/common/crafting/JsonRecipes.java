package rustic.common.crafting;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import rustic.core.Rustic;

/**
 * Registers the crafting recipes in {@code assets/rustic/recipes/*.json} (1.12's JSON recipe format) as 1.7.10 ore
 * recipes. Supports {@code minecraft:crafting_shaped/shapeless} and {@code forge:ore_shaped/shapeless}, item and
 * {@code forge:ore_dict} ingredients, and ingredient alternatives (registered as a synthetic ore name).
 */
public final class JsonRecipes {

    private static final String PATH = "assets/" + Rustic.MODID + "/recipes/";

    /** Items renamed between 1.12 Rustic and the 1.7.10 port. */
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("rustic:olive_slab_item", "rustic:olive_slab");
        ALIASES.put("rustic:ironwood_slab_item", "rustic:ironwood_slab");
        ALIASES.put("rustic:slate_roof_slab_item", "rustic:slate_roof_slab");
        ALIASES.put("rustic:slate_brick_slab_item", "rustic:slate_brick_slab");
    }

    private static int altCounter = 0;

    private JsonRecipes() {}

    public static void load() {
        Map<String, String> files = readRecipeFiles();
        int loaded = 0;
        for (Map.Entry<String, String> file : files.entrySet()) {
            try {
                IRecipe recipe = parse(
                    new JsonParser().parse(file.getValue())
                        .getAsJsonObject());
                if (recipe != null) {
                    GameRegistry.addRecipe(recipe);
                    loaded++;
                }
            } catch (Exception e) {
                Rustic.logger.warn("Skipping recipe {}: {}", file.getKey(), e.getMessage());
            }
        }
        Rustic.logger.info("Loaded {} JSON recipes", loaded);
    }

    private static Map<String, String> readRecipeFiles() {
        Map<String, String> out = new HashMap<>();
        ModContainer mod = Loader.instance()
            .getIndexedModList()
            .get(Rustic.MODID);
        File source = mod != null ? mod.getSource() : null;
        try {
            if (source != null && source.isFile()) {
                try (ZipFile zip = new ZipFile(source)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(PATH) && name.endsWith(".json")
                            && !name.substring(PATH.length())
                                .startsWith("_")) {
                            try (InputStream in = zip.getInputStream(entry)) {
                                out.put(name, readAll(in));
                            }
                        }
                    }
                }
            } else {
                // development environment: resources are on the classpath as a directory
                java.net.URL url = JsonRecipes.class.getResource("/" + PATH);
                if (url != null && "file".equals(url.getProtocol())) {
                    File[] jsons = new File(url.toURI())
                        .listFiles((dir, n) -> n.endsWith(".json") && !n.startsWith("_"));
                    if (jsons != null) {
                        for (File f : jsons) {
                            try (InputStream in = new java.io.FileInputStream(f)) {
                                out.put(f.getName(), readAll(in));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Rustic.logger.error("Could not read Rustic's recipe files", e);
        }
        return out;
    }

    private static String readAll(InputStream in) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) > 0) sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    private static IRecipe parse(JsonObject json) {
        String type = json.get("type")
            .getAsString();
        ItemStack result = parseStack(json.getAsJsonObject("result"), true);
        switch (type) {
            case "minecraft:crafting_shaped":
            case "forge:ore_shaped": {
                List<Object> args = new ArrayList<>();
                for (JsonElement row : json.getAsJsonArray("pattern")) args.add(row.getAsString());
                for (Map.Entry<String, JsonElement> key : json.getAsJsonObject("key")
                    .entrySet()) {
                    args.add(
                        key.getKey()
                            .charAt(0));
                    args.add(parseIngredient(key.getValue()));
                }
                return new ShapedOreRecipe(result, args.toArray());
            }
            case "minecraft:crafting_shapeless":
            case "forge:ore_shapeless": {
                List<Object> args = new ArrayList<>();
                for (JsonElement ing : json.getAsJsonArray("ingredients")) args.add(parseIngredient(ing));
                return new ShapelessOreRecipe(result, args.toArray());
            }
            default:
                throw new IllegalArgumentException("unsupported recipe type " + type);
        }
    }

    /** Returns an ItemStack or an ore dictionary name. */
    private static Object parseIngredient(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray alternatives = json.getAsJsonArray();
            if (alternatives.size() == 1) return parseIngredient(alternatives.get(0));
            String ore = "rusticRecipeAlternative" + (altCounter++);
            for (JsonElement alt : alternatives) {
                Object o = parseIngredient(alt);
                if (o instanceof ItemStack) {
                    OreDictionary.registerOre(ore, (ItemStack) o);
                } else {
                    for (ItemStack s : OreDictionary.getOres((String) o)) OreDictionary.registerOre(ore, s);
                }
            }
            return ore;
        }
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("type") && obj.get("type")
            .getAsString()
            .equals("forge:ore_dict")) {
            return obj.get("ore")
                .getAsString();
        }
        return parseStack(obj, false);
    }

    private static ItemStack parseStack(JsonObject obj, boolean isResult) {
        String name = obj.get("item")
            .getAsString();
        name = ALIASES.getOrDefault(name, name);
        Item item = (Item) GameData.getItemRegistry()
            .getObject(name);
        if (item == null) throw new IllegalArgumentException("unknown item " + name);
        int count = obj.has("count") ? obj.get("count")
            .getAsInt() : 1;
        int meta = obj.has("data") ? obj.get("data")
            .getAsInt() : (isResult ? 0 : OreDictionary.WILDCARD_VALUE);
        if (!isResult && !item.getHasSubtypes() && !obj.has("data")) meta = OreDictionary.WILDCARD_VALUE;
        if (!isResult && item.getHasSubtypes() && !obj.has("data")) meta = 0;
        return new ItemStack(item, count, meta);
    }

}
