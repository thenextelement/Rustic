package rustic.client.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import rustic.core.Rustic;

/**
 * Loads 1.12-format block model and blockstate JSON files from resource packs.
 */
public final class JsonModelLoader {

    private static final Map<String, JsonModel> MODEL_CACHE = new HashMap<>();
    private static final Map<String, JsonBlockState> STATE_CACHE = new HashMap<>();

    /** Stand-ins for the vanilla 1.12 parent models that have no 1.7.10 equivalent file. */
    private static final Map<String, String> BUILTIN = new HashMap<>();

    static {
        String cube = "{\"elements\":[{\"from\":[0,0,0],\"to\":[16,16,16],\"faces\":{"
            + "\"down\":{\"texture\":\"#down\",\"cullface\":\"down\"},\"up\":{\"texture\":\"#up\",\"cullface\":\"up\"},"
            + "\"north\":{\"texture\":\"#north\",\"cullface\":\"north\"},\"south\":{\"texture\":\"#south\",\"cullface\":\"south\"},"
            + "\"west\":{\"texture\":\"#west\",\"cullface\":\"west\"},\"east\":{\"texture\":\"#east\",\"cullface\":\"east\"}}}]}";
        BUILTIN.put("minecraft:block/cube", cube);
        BUILTIN.put(
            "minecraft:block/cube_all",
            "{\"parent\":\"block/cube\",\"textures\":{\"particle\":\"#all\",\"down\":\"#all\",\"up\":\"#all\",\"north\":\"#all\",\"south\":\"#all\",\"west\":\"#all\",\"east\":\"#all\"}}");
        BUILTIN.put(
            "minecraft:block/leaves",
            "{\"elements\":[{\"from\":[0,0,0],\"to\":[16,16,16],\"faces\":{"
                + "\"down\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"down\"},\"up\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"up\"},"
                + "\"north\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"north\"},\"south\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"south\"},"
                + "\"west\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"west\"},\"east\":{\"texture\":\"#all\",\"tintindex\":0,\"cullface\":\"east\"}}}],"
                + "\"textures\":{\"particle\":\"#all\"}}");
        BUILTIN.put(
            "minecraft:block/oak_leaves",
            "{\"parent\":\"block/leaves\",\"textures\":{\"all\":\"blocks/leaves_oak\"}}");
        BUILTIN.put(
            "minecraft:block/cube_column",
            "{\"parent\":\"block/cube\",\"textures\":{\"particle\":\"#side\",\"down\":\"#end\",\"up\":\"#end\",\"north\":\"#side\",\"south\":\"#side\",\"west\":\"#side\",\"east\":\"#side\"}}");
        BUILTIN.put(
            "minecraft:block/orientable",
            "{\"parent\":\"block/cube\",\"textures\":{\"particle\":\"#front\",\"down\":\"#bottom\",\"up\":\"#top\",\"north\":\"#front\",\"south\":\"#side\",\"west\":\"#side\",\"east\":\"#side\"}}");
        BUILTIN.put(
            "minecraft:block/cross",
            "{\"ambientocclusion\":false,\"textures\":{\"particle\":\"#cross\"},\"elements\":["
                + "{\"from\":[0.8,0,8],\"to\":[15.2,16,8],\"shade\":false,\"rotation\":{\"origin\":[8,8,8],\"axis\":\"y\",\"angle\":45,\"rescale\":true},"
                + "\"faces\":{\"north\":{\"uv\":[0,0,16,16],\"texture\":\"#cross\"},\"south\":{\"uv\":[0,0,16,16],\"texture\":\"#cross\"}}},"
                + "{\"from\":[8,0,0.8],\"to\":[8,16,15.2],\"shade\":false,\"rotation\":{\"origin\":[8,8,8],\"axis\":\"y\",\"angle\":45,\"rescale\":true},"
                + "\"faces\":{\"west\":{\"uv\":[0,0,16,16],\"texture\":\"#cross\"},\"east\":{\"uv\":[0,0,16,16],\"texture\":\"#cross\"}}}]}");
        BUILTIN.put("minecraft:block/block", "{}");
        BUILTIN.put("minecraft:block/tinted_cross", "{\"parent\":\"block/cross\"}");
        // 1.12 vanilla models referenced by Rustic assets that 1.7.10 doesn't ship
        BUILTIN.put(
            "minecraft:block/oak_sapling",
            "{\"parent\":\"block/cross\",\"textures\":{\"cross\":\"blocks/sapling_oak\"}}");
        BUILTIN.put(
            "minecraft:item/oak_sapling",
            "{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"blocks/sapling_oak\"}}");
        BUILTIN.put("minecraft:item/generated", "{}");
        BUILTIN.put("minecraft:item/handheld", "{}");
    }

    private JsonModelLoader() {}

    public static void clearCaches() {
        MODEL_CACHE.clear();
        STATE_CACHE.clear();
    }

    /** Normalise a model name as written in a blockstate ("rustic:chair_oak") to "rustic:block/chair_oak". */
    public static String blockModelName(String name) {
        String domain = "minecraft";
        String path = name;
        int colon = name.indexOf(':');
        if (colon >= 0) {
            domain = name.substring(0, colon);
            path = name.substring(colon + 1);
        }
        if (!path.contains("/")) path = "block/" + path;
        return domain + ":" + path;
    }

    /** Normalise a model reference used as a parent ("block/cube_all", "rustic:block/candle"). */
    private static String parentName(String name) {
        if (name.indexOf(':') < 0) return "minecraft:" + name;
        return name;
    }

    static JsonElement parse(Reader reader) {
        JsonReader json = new JsonReader(reader);
        json.setLenient(true);
        return new JsonParser().parse(json);
    }

    private static JsonElement read(String domain, String path) {
        try {
            IResource res = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(new ResourceLocation(domain, path));
            try (InputStream in = res.getInputStream()) {
                return parse(in);
            }
        } catch (java.io.FileNotFoundException e) {
            // GTNHLib hides 1.12-format blockstate files from resource packs with an old pack_format, so they can't be
            // read through the resource manager; fall back to the copy shipped in the mod jar.
            try (InputStream in = JsonModelLoader.class.getResourceAsStream("/assets/" + domain + "/" + path)) {
                return in == null ? null : parse(in);
            } catch (Exception e2) {
                Rustic.logger.error("Failed to read {}:{}", domain, path, e2);
                return null;
            }
        } catch (Exception e) {
            Rustic.logger.error("Failed to read {}:{}", domain, path, e);
            return null;
        }
    }

    private static JsonElement parse(InputStream in) throws java.io.IOException {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    public static JsonBlockState getBlockState(String name) {
        if (STATE_CACHE.containsKey(name)) return STATE_CACHE.get(name);
        JsonElement json = read(Rustic.MODID, "blockstates/" + name + ".json");
        JsonBlockState state = null;
        if (json != null && json.isJsonObject()) {
            try {
                state = JsonBlockState.parse(json.getAsJsonObject());
            } catch (Exception e) {
                Rustic.logger.error("Failed to parse blockstate {}", name, e);
            }
        } else {
            Rustic.logger.warn("Missing blockstate file for {}", name);
        }
        STATE_CACHE.put(name, state);
        return state;
    }

    /** @param name a fully-qualified model name, e.g. "rustic:block/chair_oak" */
    public static JsonModel getModel(String name) {
        return getModel(name, true);
    }

    /** @param warn whether to log a missing model (false when probing for an optional item model) */
    public static JsonModel getModel(String name, boolean warn) {
        if (MODEL_CACHE.containsKey(name)) return MODEL_CACHE.get(name);
        MODEL_CACHE.put(name, null); // guards against parent cycles
        JsonModel model = null;
        try {
            JsonElement json;
            if (BUILTIN.containsKey(name)) {
                json = parse(new StringReader(BUILTIN.get(name)));
            } else {
                int colon = name.indexOf(':');
                json = read(name.substring(0, colon), "models/" + name.substring(colon + 1) + ".json");
            }
            if (json != null && json.isJsonObject()) {
                model = parseModel(json.getAsJsonObject());
            } else {
                if (warn) Rustic.logger.warn("Missing model {}", name);
            }
        } catch (Exception e) {
            Rustic.logger.error("Failed to parse model {}", name, e);
        }
        MODEL_CACHE.put(name, model);
        return model;
    }

    private static JsonModel parseModel(JsonObject obj) {
        JsonModel model = new JsonModel();
        if (obj.has("parent")) {
            JsonModel parent = getModel(
                parentName(
                    obj.get("parent")
                        .getAsString()));
            if (parent != null) {
                model.textures.putAll(parent.textures);
                model.elements.addAll(parent.elements);
                model.ambientOcclusion = parent.ambientOcclusion;
                model.display.putAll(parent.display);
            }
        }
        if (obj.has("display")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("display")
                .entrySet()) {
                JsonObject t = entry.getValue()
                    .getAsJsonObject();
                model.display.put(
                    entry.getKey(),
                    new float[][] { t.has("rotation") ? vec(t.getAsJsonArray("rotation")) : new float[3],
                        t.has("translation") ? vec(t.getAsJsonArray("translation")) : new float[3],
                        t.has("scale") ? vec(t.getAsJsonArray("scale")) : new float[] { 1, 1, 1 } });
            }
        }
        if (obj.has("ambientocclusion")) {
            model.ambientOcclusion = obj.get("ambientocclusion")
                .getAsBoolean();
        }
        if (obj.has("textures")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("textures")
                .entrySet()) {
                model.textures.put(
                    entry.getKey(),
                    entry.getValue()
                        .getAsString());
            }
        }
        if (obj.has("elements")) {
            model.elements.clear();
            for (JsonElement e : obj.getAsJsonArray("elements")) {
                model.elements.add(parseElement(e.getAsJsonObject()));
            }
        }
        return model;
    }

    private static float[] vec(JsonArray arr) {
        float[] out = new float[arr.size()];
        for (int i = 0; i < out.length; i++) out[i] = arr.get(i)
            .getAsFloat();
        return out;
    }

    private static JsonModel.Element parseElement(JsonObject obj) {
        JsonModel.Element el = new JsonModel.Element();
        el.from = vec(obj.getAsJsonArray("from"));
        el.to = vec(obj.getAsJsonArray("to"));
        if (obj.has("shade")) el.shade = obj.get("shade")
            .getAsBoolean();
        if (obj.has("rotation")) {
            JsonObject rot = obj.getAsJsonObject("rotation");
            el.rotAxis = rot.get("axis")
                .getAsString()
                .charAt(0);
            el.rotAngle = rot.get("angle")
                .getAsFloat();
            if (rot.has("origin")) el.rotOrigin = vec(rot.getAsJsonArray("origin"));
            if (rot.has("rescale")) el.rotRescale = rot.get("rescale")
                .getAsBoolean();
        }
        if (obj.has("faces")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("faces")
                .entrySet()) {
                ForgeDirection dir = direction(entry.getKey());
                if (dir == null) continue;
                JsonObject f = entry.getValue()
                    .getAsJsonObject();
                JsonModel.Face face = new JsonModel.Face();
                if (f.has("uv")) face.uv = vec(f.getAsJsonArray("uv"));
                face.texture = f.has("texture") ? f.get("texture")
                    .getAsString() : null;
                // like 1.12's ModelBlock, a bare face texture name is a variable reference ("0" means "#0"); direct
                // paths are kept as they are
                if (face.texture != null && !face.texture.startsWith("#")
                    && face.texture.indexOf(':') < 0
                    && face.texture.indexOf('/') < 0) face.texture = "#" + face.texture;
                if (f.has("rotation")) face.rotation = f.get("rotation")
                    .getAsInt();
                if (f.has("cullface")) face.cullface = direction(
                    f.get("cullface")
                        .getAsString());
                if (f.has("tintindex")) face.tintIndex = f.get("tintindex")
                    .getAsInt();
                el.faces[dir.ordinal()] = face;
            }
        }
        return el;
    }

    public static ForgeDirection direction(String name) {
        switch (name) {
            case "down":
            case "bottom":
                return ForgeDirection.DOWN;
            case "up":
                return ForgeDirection.UP;
            case "north":
                return ForgeDirection.NORTH;
            case "south":
                return ForgeDirection.SOUTH;
            case "west":
                return ForgeDirection.WEST;
            case "east":
                return ForgeDirection.EAST;
            default:
                return null;
        }
    }

    /**
     * Read an item model ("rustic:item/olives") and return the texture assigned to the given layer (e.g. "layer0"),
     * following parents and "#" references. Returns null if there is no such layer.
     */
    public static String itemModelTexture(String name, String layer) {
        JsonModel model = getModel(name, false);
        if (model == null) return null;
        return model.resolveTexture("#" + layer);
    }

    /**
     * The 1.7.10 icon name for an item texture from a 1.12 item model ("rustic:items/herbs/chamomile" ->
     * "rustic:herbs/chamomile"), or null if the texture isn't an item texture.
     */
    public static String itemIconName(String texture) {
        if (texture == null) return null;
        String domain = "minecraft";
        String path = texture;
        int colon = texture.indexOf(':');
        if (colon >= 0) {
            domain = texture.substring(0, colon);
            path = texture.substring(colon + 1);
        }
        if (path.startsWith("items/")) path = path.substring(6);
        else if (path.startsWith("item/")) path = path.substring(5);
        else return null;
        return domain.equals("minecraft") ? path : domain + ":" + path;
    }

    /** Item icon for layer 0 of rustic:item/{name}, or null. */
    public static String itemIcon(String itemName) {
        return itemIconName(itemModelTexture(Rustic.MODID + ":item/" + itemName, "layer0"));
    }

}
