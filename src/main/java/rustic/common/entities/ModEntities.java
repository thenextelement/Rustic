package rustic.common.entities;

import cpw.mods.fml.common.registry.EntityRegistry;
import rustic.common.blocks.BlockChair;
import rustic.core.Rustic;

public class ModEntities {

    public static void init() {
        int id = 0;
        EntityRegistry.registerModEntity(EntityTomato.class, "tomato", id++, Rustic.instance, 64, 1, true);
        EntityRegistry.registerModEntity(BlockChair.EntityChair.class, "chair", id++, Rustic.instance, 64, 128, false);
    }

}
