package rustic.client.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * A resolved 1.12-style block model: parent chain already merged, textures still referenced by name.
 */
public class JsonModel {

    public final Map<String, String> textures = new HashMap<>();
    public final List<Element> elements = new ArrayList<>();
    public boolean ambientOcclusion = true;
    /** Item display transforms by context ("gui", "firstperson_righthand", ...): {rotation, translation, scale}. */
    public final Map<String, float[][]> display = new HashMap<>();

    /** Resolve a texture reference ("#side", "rustic:blocks/x", ...) to a texture name, or null if missing. */
    public String resolveTexture(String ref) {
        int depth = 0;
        while (ref != null && ref.startsWith("#") && depth++ < 16) {
            ref = textures.get(ref.substring(1));
        }
        if (ref == null || ref.startsWith("#")) return null;
        return ref;
    }

    public JsonModel withTextures(Map<String, String> overrides) {
        if (overrides == null || overrides.isEmpty()) return this;
        JsonModel copy = new JsonModel();
        copy.textures.putAll(textures);
        copy.textures.putAll(overrides);
        copy.elements.addAll(elements);
        copy.ambientOcclusion = ambientOcclusion;
        copy.display.putAll(display);
        return copy;
    }

    public static class Element {

        public float[] from = new float[3];
        public float[] to = new float[3];
        public final Face[] faces = new Face[6]; // indexed by ForgeDirection ordinal
        public boolean shade = true;
        // element rotation
        public char rotAxis = 0;
        public float rotAngle;
        public float[] rotOrigin = { 8, 8, 8 };
        public boolean rotRescale;
    }

    public static class Face {

        public float[] uv; // may be null -> computed
        public String texture;
        public int rotation;
        public ForgeDirection cullface;
        public int tintIndex = -1;
    }

}
