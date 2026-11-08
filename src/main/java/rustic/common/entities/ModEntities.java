package rustic.common.entities;

import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.renderer.RenderTomato;
import rustic.common.blocks.BlockChair;
import rustic.core.Rustic;

public class ModEntities {
	
	public static void init() {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(Rustic.MODID + ":tomato"), EntityTomato.class, Rustic.MODID + ".tomato", id++, Rustic.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Rustic.MODID + ":chair"), BlockChair.EntityChair.class, Rustic.MODID + ".chair", id++, Rustic.instance, 64, 128, false);
	}
	
	@SideOnly(Side.CLIENT)
	public static void initRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTomato.class, new RenderTomato.Factory());
		//RenderingRegistry.registerEntityRenderingHandler(BlockChair.EntityChair.class, renderFactory);
	}

}
