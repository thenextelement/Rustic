package rustic.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import rustic.client.EventHandlerClient;
import rustic.client.json.JsonModelRenderer;
import rustic.client.renderer.CabinetRenderer;
import rustic.client.renderer.CrushingTubRenderer;
import rustic.client.renderer.EvaporatingBasinRenderer;
import rustic.client.renderer.LayerIronSkin;
import rustic.client.renderer.LiquidBarrelRenderer;
import rustic.client.renderer.RenderNothing;
import rustic.client.renderer.RenderTomato;
import rustic.client.util.ItemColorCache;
import rustic.common.Config;
import rustic.common.blocks.BlockChair;
import rustic.common.blocks.ModBlocks;
import rustic.common.entities.EntityTomato;
import rustic.common.tileentity.TileEntityCabinet;
import rustic.common.tileentity.TileEntityCrushingTub;
import rustic.common.tileentity.TileEntityEvaporatingBasin;
import rustic.common.tileentity.TileEntityLiquidBarrel;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        JsonModelRenderer.register();
        super.preInit(event);

        EventHandlerClient client = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(client);
        cpw.mods.fml.common.FMLCommonHandler.instance()
            .bus()
            .register(client);
        MinecraftForge.EVENT_BUS.register(new LayerIronSkin.Handler());

        RenderingRegistry.registerEntityRenderingHandler(EntityTomato.class, new RenderTomato());
        RenderingRegistry.registerEntityRenderingHandler(BlockChair.EntityChair.class, new RenderNothing());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCabinet.class, new CabinetRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrushingTub.class, new CrushingTubRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEvaporatingBasin.class, new EvaporatingBasinRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiquidBarrel.class, new LiquidBarrelRenderer());
        MinecraftForgeClient
            .registerItemRenderer(Item.getItemFromBlock(ModBlocks.CABINET), new CabinetRenderer.CabinetItemRenderer());
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.LIQUID_BARREL),
            new rustic.client.renderer.LiquidBarrelItemRenderer());
        MinecraftForgeClient.registerItemRenderer(
            rustic.common.items.ModItems.BOOK,
            new rustic.client.json.JsonItemModelRenderer("book"));
        rustic.dev.SelfTest.initIfRequested();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // the almanac's pages use client rendering classes, so it only exists on the client (as in 1.12)
        rustic.common.book.BookManager.init();
        for (String entityName : Config.IRONSKIN_RENDER_ENTITY_BLACKLIST) {
            disableIronSkinRenderer(entityName);
        }
        if (Minecraft.getMinecraft()
            .getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getMinecraft()
                .getResourceManager()).registerReloadListener(ItemColorCache.INSTANCE);
        }
    }

    @Override
    public void spawnAlchemySmokeFX(World world, int brewTime, double x, double y, double z, double xVel, double yVel,
        double zVel) {
        EntityFX smoke = new EntitySmokeFX(world, x, y, z, xVel, yVel, zVel);
        float colorScale = 0.4F;
        float r = colorScale * ((MathHelper.sin(30 + brewTime / 16F) * 0.5F) + 0.5F);
        float g = colorScale * ((MathHelper.sin(brewTime / 16F) * 0.5F) + 0.5F);
        float b = colorScale * ((MathHelper.sin(60 + brewTime / 16F) * 0.5F) + 0.5F);
        smoke.setRBGColorF(r, g, b);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
    }

    @Override
    public void disableIronSkinRenderer(String entityName) {
        LayerIronSkin.disableFor(entityName);
    }

}
