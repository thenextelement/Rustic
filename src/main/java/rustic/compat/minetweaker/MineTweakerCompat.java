package rustic.compat.minetweaker;

import minetweaker.MineTweakerAPI;

/** Registers Rustic's ZenScript classes. Only call when MineTweaker3 is loaded. */
public final class MineTweakerCompat {

    private MineTweakerCompat() {}

    public static void init() {
        MineTweakerAPI.registerClass(CrushingTub.class);
        MineTweakerAPI.registerClass(EvaporatingBasin.class);
        MineTweakerAPI.registerClass(Condenser.class);
    }

}
