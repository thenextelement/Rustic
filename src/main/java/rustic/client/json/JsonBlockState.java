package rustic.client.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A parsed blockstate file. Supports the vanilla "variants" and "multipart" formats and the Forge
 * ("forge_marker": 1) format.
 */
public class JsonBlockState {

    /** A model reference with placement info, before the model is loaded. */
    public static class Variant {

        public String model;
        public Map<String, String> textures = new HashMap<>();
        public int x, y;
        public boolean uvlock;
        /** Forge submodels: name -> alternatives (one is picked per block position). */
        public Map<String, List<JsonObject>> submodels = new HashMap<>();

        Variant copy() {
            Variant v = new Variant();
            v.model = model;
            v.textures = new HashMap<>(textures);
            v.submodels = new HashMap<>(submodels);
            v.x = x;
            v.y = y;
            v.uvlock = uvlock;
            return v;
        }

        void merge(JsonObject obj, boolean forge) {
            if (obj.has("model")) model = obj.get("model")
                .getAsString();
            if (obj.has("x")) x = obj.get("x")
                .getAsInt();
            if (obj.has("y")) y = obj.get("y")
                .getAsInt();
            if (obj.has("uvlock")) uvlock = obj.get("uvlock")
                .getAsBoolean();
            if (forge && obj.has("submodel")) {
                JsonElement sub = obj.get("submodel");
                if (sub.isJsonPrimitive()) {
                    JsonObject single = new JsonObject();
                    single.add("model", sub);
                    submodels.put("#", java.util.Collections.singletonList(single));
                } else {
                    for (Map.Entry<String, JsonElement> e : sub.getAsJsonObject()
                        .entrySet()) {
                        List<JsonObject> options = new ArrayList<>();
                        if (e.getValue()
                            .isJsonArray()) {
                            for (JsonElement o : e.getValue()
                                .getAsJsonArray()) options.add(o.getAsJsonObject());
                        } else {
                            options.add(
                                e.getValue()
                                    .getAsJsonObject());
                        }
                        submodels.put(e.getKey(), options);
                    }
                }
            }
            if (forge && obj.has("textures")) {
                for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("textures")
                    .entrySet()) {
                    textures.put(
                        e.getKey(),
                        e.getValue()
                            .getAsString());
                }
            }
        }
    }

    private static class Condition {

        // list of alternatives (OR), each a map of property -> allowed values
        final List<Map<String, String[]>> alternatives = new ArrayList<>();

        boolean test(Map<String, String> props) {
            if (alternatives.isEmpty()) return true;
            outer: for (Map<String, String[]> alt : alternatives) {
                for (Map.Entry<String, String[]> e : alt.entrySet()) {
                    String actual = props.get(e.getKey());
                    boolean ok = false;
                    for (String allowed : e.getValue()) {
                        if (allowed.equals(actual)) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) continue outer;
                }
                return true;
            }
            return false;
        }

        static Condition parse(JsonObject when) {
            Condition c = new Condition();
            if (when == null) return c;
            if (when.has("OR")) {
                for (JsonElement e : when.getAsJsonArray("OR")) {
                    c.alternatives.add(parseAnd(e.getAsJsonObject()));
                }
            } else {
                c.alternatives.add(parseAnd(when));
            }
            return c;
        }

        static Map<String, String[]> parseAnd(JsonObject obj) {
            Map<String, String[]> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                map.put(
                    e.getKey(),
                    e.getValue()
                        .getAsString()
                        .split("\\|"));
            }
            return map;
        }
    }

    private static class MultipartCase {

        Condition when;
        Variant apply;
    }

    // vanilla variants: key -> (parsed key, variant)
    private final List<Map<String, String>> variantKeys = new ArrayList<>();
    private final List<Variant> variantValues = new ArrayList<>();
    private final List<MultipartCase> multipart = new ArrayList<>();

    // forge format
    private boolean forge;
    private Variant forgeDefaults;
    private final Map<String, Map<String, JsonObject>> forgeProperties = new HashMap<>();

    public static JsonBlockState parse(JsonObject obj) {
        JsonBlockState state = new JsonBlockState();
        if (obj.has("forge_marker")) {
            state.forge = true;
            state.forgeDefaults = new Variant();
            if (obj.has("defaults")) state.forgeDefaults.merge(obj.getAsJsonObject("defaults"), true);
        }
        if (obj.has("variants")) {
            for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("variants")
                .entrySet()) {
                String key = e.getKey();
                JsonElement value = e.getValue();
                if (state.forge && !key.contains("=")
                    && value.isJsonObject()
                    && isPropertyMap(value.getAsJsonObject())) {
                    Map<String, JsonObject> values = new HashMap<>();
                    for (Map.Entry<String, JsonElement> v : value.getAsJsonObject()
                        .entrySet()) {
                        values.put(
                            v.getKey(),
                            v.getValue()
                                .getAsJsonObject());
                    }
                    state.forgeProperties.put(key, values);
                    continue;
                }
                JsonObject spec = first(value);
                if (spec == null) continue;
                Variant variant = state.forge ? state.forgeDefaults.copy() : new Variant();
                variant.merge(spec, state.forge);
                state.variantKeys.add(parseKey(key));
                state.variantValues.add(variant);
            }
        }
        if (obj.has("multipart")) {
            for (JsonElement e : obj.getAsJsonArray("multipart")) {
                JsonObject c = e.getAsJsonObject();
                JsonObject spec = first(c.get("apply"));
                if (spec == null) continue;
                MultipartCase mc = new MultipartCase();
                mc.when = Condition.parse(c.has("when") ? c.getAsJsonObject("when") : null);
                mc.apply = new Variant();
                mc.apply.merge(spec, false);
                state.multipart.add(mc);
            }
        }
        return state;
    }

    private static boolean isPropertyMap(JsonObject obj) {
        if (obj.entrySet()
            .isEmpty()) return false;
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            if (!e.getValue()
                .isJsonObject()) return false;
            // a variant spec has keys like "model"/"textures"; a property map has value names
            if (e.getKey()
                .equals("model")
                || e.getKey()
                    .equals("textures")
                || e.getKey()
                    .equals("transform"))
                return false;
        }
        return true;
    }

    private static JsonObject first(JsonElement e) {
        if (e == null) return null;
        if (e.isJsonObject()) return e.getAsJsonObject();
        if (e.isJsonArray()) {
            JsonArray arr = e.getAsJsonArray();
            if (arr.size() > 0 && arr.get(0)
                .isJsonObject())
                return arr.get(0)
                    .getAsJsonObject();
            return new JsonObject(); // forge "inventory": [] means "use defaults"
        }
        return null;
    }

    private static Map<String, String> parseKey(String key) {
        Map<String, String> map = new HashMap<>();
        if (key.equals("normal") || key.equals("inventory") || key.isEmpty()) {
            map.put("#" + key, "");
            return map;
        }
        for (String pair : key.split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0) map.put(
                pair.substring(0, eq)
                    .trim(),
                pair.substring(eq + 1)
                    .trim());
        }
        return map;
    }

    /**
     * Property key a block can set to choose between random alternatives (Forge submodel arrays); its value is an
     * integer, usually a hash of the block position.
     */
    public static final String RANDOM_KEY = "#random";

    /** Every model reference in the file, used to register textures up front. */
    public List<Variant> allVariants() {
        List<Variant> out = new ArrayList<>(variantValues);
        for (MultipartCase mc : multipart) out.add(mc.apply);
        if (forge) {
            out.add(forgeDefaults);
            for (Map<String, JsonObject> values : forgeProperties.values()) {
                for (JsonObject spec : values.values()) {
                    Variant v = forgeDefaults.copy();
                    v.merge(spec, true);
                    out.add(v);
                }
            }
        }
        List<Variant> subs = new ArrayList<>();
        for (Variant v : out) {
            for (List<JsonObject> options : v.submodels.values()) {
                for (JsonObject o : options) {
                    Variant sv = new Variant();
                    sv.merge(o, true);
                    subs.add(sv);
                }
            }
        }
        out.addAll(subs);
        out.removeIf(v -> v.model == null);
        return out;
    }

    /** Resolve the models to draw for the given properties. */
    public List<Variant> resolve(Map<String, String> props, boolean inventory) {
        List<Variant> out = new ArrayList<>();
        if (!multipart.isEmpty()) {
            for (MultipartCase mc : multipart) {
                if (mc.when.test(props)) out.add(mc.apply);
            }
            return out;
        }

        Variant best = null;
        int bestScore = -1;
        for (int i = 0; i < variantKeys.size(); i++) {
            Map<String, String> key = variantKeys.get(i);
            int score;
            if (key.containsKey("#inventory")) {
                score = inventory ? 1000 : -1;
            } else if (key.containsKey("#normal") || key.containsKey("#")) {
                score = 0;
            } else {
                score = 0;
                for (Map.Entry<String, String> e : key.entrySet()) {
                    if (!e.getValue()
                        .equals(props.get(e.getKey()))) {
                        score = -1;
                        break;
                    }
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = variantValues.get(i);
            }
        }

        if (forge) {
            Variant v = best != null ? best.copy() : forgeDefaults.copy();
            if (best == null || bestScore < 1000) {
                for (Map.Entry<String, Map<String, JsonObject>> prop : forgeProperties.entrySet()) {
                    String value = props.get(prop.getKey());
                    if (value == null) continue;
                    JsonObject spec = prop.getValue()
                        .get(value);
                    if (spec != null) v.merge(spec, true);
                }
            }
            if (v.model != null) out.add(v);
            int random = 0;
            try {
                random = Integer.parseInt(props.getOrDefault(RANDOM_KEY, "0"));
            } catch (NumberFormatException ignored) {}
            for (List<JsonObject> options : v.submodels.values()) {
                if (options.isEmpty()) continue;
                Variant sv = new Variant();
                sv.merge(options.get(Math.abs(random) % options.size()), true);
                if (sv.model != null) out.add(sv);
            }
        } else if (best != null) {
            out.add(best);
        }
        return out;
    }

}
