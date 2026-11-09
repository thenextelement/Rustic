package rustic.client.renderer;

import java.util.HashSet;
import java.util.Set;

/**
 * The iron skin / fullmetal visual overlay. 1.12 drew it as a render layer, which 1.7.10 doesn't have; the overlay
 * is not ported yet (TODO: re-render the main model with the iron texture in RenderLivingEvent.Post). The potion
 * effects themselves work. The 1.12 implementation is in git history (branch master).
 */
public final class LayerIronSkin {

	public static final Set<String> DISABLED_ENTITIES = new HashSet<>();

	private LayerIronSkin() {}

	public static void disableFor(String entityName) {
		if (entityName != null && !entityName.isEmpty()) DISABLED_ENTITIES.add(entityName);
	}

}
