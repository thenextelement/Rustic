package rustic.client.renderer;

import net.minecraft.client.renderer.entity.RenderSnowball;

import rustic.common.items.ModItems;

/** Thrown tomatoes render like snowballs, with the tomato icon. */
public class RenderTomato extends RenderSnowball {

    public RenderTomato() {
        super(ModItems.TOMATO);
    }

}
