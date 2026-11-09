package rustic.common.potions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.IntFunction;

import net.minecraft.potion.Potion;
import net.minecraftforge.common.config.Configuration;
import rustic.core.CommonProxy;
import rustic.core.Rustic;

/**
 * Rustic's potion effects. 1.7.10 potions are identified by a numeric id; ids are read from the config (category
 * "all.potions"), and -1 (the default) picks the first free slot, extending the potion array if needed.
 */
public class PotionsRustic {

	public static Potion IRON_SKIN_POTION;
	public static Potion FEATHER_POTION;
	public static Potion BLAZING_TRAIL_POTION;
	public static Potion SHAME_POTION;
	public static Potion FULLMETAL_POTION;
	public static Potion FIRE_POWER_POTION;
	public static Potion FULL_POTION;
	public static Potion MAGIC_RESISTANCE_POTION;
	public static Potion WITHER_WARD_POTION;
	public static Potion UNDYING_POTION;
	public static Potion TIPSY;

	private static final String CATEGORY = "all.potions";

	public static void init() {
		Configuration cfg = CommonProxy.config;
		cfg.addCustomCategoryComment(CATEGORY, "Potion ids (1.7.10 only). -1 picks a free id automatically.");
		IRON_SKIN_POTION = create(cfg, "ironskin", PotionIronSkin::new);
		FEATHER_POTION = create(cfg, "feather", PotionFeather::new);
		BLAZING_TRAIL_POTION = create(cfg, "blazing_trail", PotionBlazingTrail::new);
		SHAME_POTION = create(cfg, "shame", PotionShame::new);
		FULLMETAL_POTION = create(cfg, "fullmetal", PotionFullmetal::new);
		FIRE_POWER_POTION = create(cfg, "fire_power", id -> new PotionBase(id, false, 0xFFCE6D, "fire_power").setIconIndex(2, 1));
		FULL_POTION = create(cfg, "full", id -> new PotionBase(id, false, 6563840, "full").setIconIndex(5, 0));
		MAGIC_RESISTANCE_POTION = create(cfg, "magic_resistance", id -> new PotionBase(id, false, 10511560, "magic_resistance").setIconIndex(6, 0));
		WITHER_WARD_POTION = create(cfg, "wither_ward", id -> new PotionBase(id, false, 11842760, "wither_ward").setIconIndex(7, 0));
		UNDYING_POTION = create(cfg, "undying", id -> new PotionBase(id, false, 0xEADB84, "undying").setIconIndex(1, 1));
		TIPSY = create(cfg, "tipsy", PotionTipsy::new);
		if (cfg.hasChanged()) cfg.save();
	}

	private static Potion create(Configuration cfg, String name, IntFunction<Potion> factory) {
		int id = cfg.get(CATEGORY, name, -1).getInt();
		if (id < 0 || id >= Potion.potionTypes.length || Potion.potionTypes[id] != null) {
			if (id >= 0) Rustic.logger.warn("Potion id {} for {} is unavailable, picking another", id, name);
			id = freeId();
		}
		return factory.apply(id);
	}

	private static int freeId() {
		for (int i = 32; i < Potion.potionTypes.length; i++) {
			if (Potion.potionTypes[i] == null) return i;
		}
		int old = Potion.potionTypes.length;
		expandPotionArray(Math.max(old * 2, 64));
		return old;
	}

	private static void expandPotionArray(int size) {
		try {
			Field field = null;
			for (Field f : Potion.class.getDeclaredFields()) {
				if (f.getType() == Potion[].class && Modifier.isStatic(f.getModifiers())) {
					field = f;
					break;
				}
			}
			field.setAccessible(true);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			Potion[] bigger = new Potion[size];
			System.arraycopy(Potion.potionTypes, 0, bigger, 0, Potion.potionTypes.length);
			field.set(null, bigger);
			Rustic.logger.info("Expanded the potion array to {} entries", size);
		} catch (Exception e) {
			throw new RuntimeException("Rustic could not find free potion ids; set them in rustic.cfg", e);
		}
	}

}
