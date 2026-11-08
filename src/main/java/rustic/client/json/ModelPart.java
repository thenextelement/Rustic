package rustic.client.json;

import net.minecraft.util.IIcon;

/**
 * A model placed with a blockstate rotation, with its face textures resolved to icons.
 */
public class ModelPart {

	public final JsonModel model;
	public final int x;
	public final int y;
	public final boolean uvlock;
	/** icons[element][face ordinal], null where the face is absent or untextured */
	public final IIcon[][] icons;
	public final IIcon particle;

	public ModelPart(JsonModel model, int x, int y, boolean uvlock, IIcon[][] icons, IIcon particle) {
		this.model = model;
		this.x = ((x % 360) + 360) % 360;
		this.y = ((y % 360) + 360) % 360;
		this.uvlock = uvlock;
		this.icons = icons;
		this.particle = particle;
	}

}
