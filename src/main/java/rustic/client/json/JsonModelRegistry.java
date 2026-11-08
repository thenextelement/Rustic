package rustic.client.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import rustic.common.blocks.IModelBlock;

/**
 * Keeps the loaded blockstates/models for {@link IModelBlock}s and the icons their textures were stitched to.
 */
public final class JsonModelRegistry {

	private static final Map<String, IIcon> ICONS = new HashMap<>();
	private static final Map<String, List<ModelPart>> PART_CACHE = new HashMap<>();

	private JsonModelRegistry() {}

	/** Convert a 1.12 texture name ("rustic:blocks/vases/vase_side_0") to a 1.7.10 block icon name. */
	public static String iconName(String texture) {
		String domain = "minecraft";
		String path = texture;
		int colon = texture.indexOf(':');
		if (colon >= 0) {
			domain = texture.substring(0, colon);
			path = texture.substring(colon + 1);
		}
		if (path.startsWith("blocks/")) path = path.substring(7);
		else if (path.startsWith("block/")) path = path.substring(6);
		return domain.equals("minecraft") ? path : domain + ":" + path;
	}

	private static IIcon icon(IIconRegister register, String texture) {
		if (texture == null) return null;
		String name = iconName(texture);
		if (register == null) return ICONS.get(name);
		IIcon icon = register.registerIcon(name); // TextureMap returns the already-registered sprite on repeats
		ICONS.put(name, icon);
		return icon;
	}

	/**
	 * Called from {@code registerBlockIcons}: loads the block's blockstate and registers every texture its models can
	 * reference. Returns the icon to use for particles / as a fallback.
	 */
	public static IIcon registerBlock(Block block, IIconRegister register) {
		if (!(block instanceof IModelBlock)) return null;
		checkReset();
		IModelBlock mb = (IModelBlock) block;
		JsonBlockState state = JsonModelLoader.getBlockState(mb.getBlockStateName());
		if (state == null) return register.registerIcon("missingno");
		IIcon particle = null;
		for (JsonBlockState.Variant v : state.allVariants()) {
			JsonModel model = JsonModelLoader.getModel(JsonModelLoader.blockModelName(v.model));
			if (model == null) continue;
			model = model.withTextures(v.textures);
			Set<String> names = new HashSet<>(v.textures.values());
			for (String ref : model.textures.keySet()) names.add(model.resolveTexture("#" + ref));
			for (String name : names) {
				if (name != null && !name.startsWith("#")) icon(register, name);
			}
			if (particle == null) particle = icon(register, model.resolveTexture("#particle"));
		}
		return particle != null ? particle : register.registerIcon("missingno");
	}

	private static boolean needsReset = true;

	private static void checkReset() {
		if (needsReset) {
			needsReset = false;
			ICONS.clear();
			PART_CACHE.clear();
			JsonModelLoader.clearCaches();
		}
	}

	/**
	 * Called after the block atlas is stitched; the next {@link #registerBlock} call (at the start of the next resource
	 * reload) starts from empty caches.
	 */
	public static void onTextureStitchPost() {
		needsReset = true;
		PART_CACHE.clear();
	}

	public static List<ModelPart> getParts(IModelBlock block, Map<String, String> props, boolean inventory) {
		String key = block.getBlockStateName() + (inventory ? "#inv" : "") + new TreeMap<>(props);
		List<ModelPart> parts = PART_CACHE.get(key);
		if (parts != null) return parts;

		JsonBlockState state = JsonModelLoader.getBlockState(block.getBlockStateName());
		if (state == null) {
			parts = Collections.emptyList();
		} else {
			parts = new ArrayList<>();
			for (JsonBlockState.Variant v : state.resolve(props, inventory)) {
				JsonModel model = JsonModelLoader.getModel(JsonModelLoader.blockModelName(v.model));
				if (model == null) continue;
				model = model.withTextures(v.textures);
				IIcon[][] icons = new IIcon[model.elements.size()][6];
				for (int i = 0; i < model.elements.size(); i++) {
					JsonModel.Element el = model.elements.get(i);
					for (int f = 0; f < 6; f++) {
						if (el.faces[f] != null) icons[i][f] = icon(null, model.resolveTexture(el.faces[f].texture));
					}
				}
				parts.add(new ModelPart(model, v.x, v.y, v.uvlock, icons, icon(null, model.resolveTexture("#particle"))));
			}
		}
		PART_CACHE.put(key, parts);
		return parts;
	}


	/**
	 * For plain cube blocks: read the blockstate/model of {@code stateName} for the given properties and register the
	 * six face textures of its first element. Missing faces fall back to the particle texture.
	 */
	public static IIcon[] registerCubeIcons(String stateName, Map<String, String> props, IIconRegister register) {
		checkReset();
		IIcon[] out = new IIcon[6];
		JsonBlockState state = JsonModelLoader.getBlockState(stateName);
		if (state != null) {
			List<JsonBlockState.Variant> variants = state.resolve(props, false);
			if (!variants.isEmpty()) {
				JsonBlockState.Variant v = variants.get(0);
				JsonModel model = JsonModelLoader.getModel(JsonModelLoader.blockModelName(v.model));
				if (model != null) {
					model = model.withTextures(v.textures);
					IIcon particle = icon(register, model.resolveTexture("#particle"));
					if (!model.elements.isEmpty()) {
						JsonModel.Element el = model.elements.get(0);
						for (int f = 0; f < 6; f++) {
							if (el.faces[f] != null) out[f] = icon(register, model.resolveTexture(el.faces[f].texture));
						}
					}
					for (int f = 0; f < 6; f++) {
						if (out[f] == null) out[f] = particle;
					}
				}
			}
		}
		for (int f = 0; f < 6; f++) {
			if (out[f] == null) out[f] = register.registerIcon(rustic.core.Rustic.MODID + ":" + stateName);
		}
		return out;
	}


	/** Register and return the icon a model texture variable (e.g. "cross") resolves to for the given state. */
	public static IIcon registerStateTexture(String stateName, Map<String, String> props, String textureVar,
			IIconRegister register) {
		checkReset();
		JsonBlockState state = JsonModelLoader.getBlockState(stateName);
		if (state != null) {
			List<JsonBlockState.Variant> variants = state.resolve(props, false);
			if (!variants.isEmpty()) {
				JsonBlockState.Variant v = variants.get(0);
				JsonModel model = JsonModelLoader.getModel(JsonModelLoader.blockModelName(v.model));
				if (model != null) {
					model = model.withTextures(v.textures);
					IIcon icon = icon(register, model.resolveTexture("#" + textureVar));
					if (icon != null) return icon;
				}
			}
		}
		return register.registerIcon(rustic.core.Rustic.MODID + ":" + stateName);
	}

}
