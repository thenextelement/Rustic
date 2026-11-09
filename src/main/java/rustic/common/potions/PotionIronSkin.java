package rustic.common.potions;

/**
 * Iron skin. 1.7.10 has no armor attribute, so the protection (3 armor + 2 toughness per level in 1.12) is applied as
 * damage reduction in {@link EventHandlerPotions}.
 */
public class PotionIronSkin extends PotionBase {

	protected PotionIronSkin(int id) {
		super(id, false, 16777148, "ironskin");
		setIconIndex(0, 0);
	}

}
