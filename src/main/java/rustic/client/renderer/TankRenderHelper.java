package rustic.client.renderer;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Shared drawing code for Rustic's tank-like tile entity renderers. */
public final class TankRenderHelper {

	private TankRenderHelper() {}

	/** Draws a flat fluid surface inset by {@code inset} on each side, at height {@code height} (block units). */
	public static void drawFluidSurface(TileEntity te, Fluid fluid, double x, double y, double z, double inset, double height) {
		IIcon sprite = fluid.getStillIcon();
		if (sprite == null) return;
		int c = fluid.getColor(new FluidStack(fluid, 1000));
		float du = sprite.getMaxU() - sprite.getMinU(), dv = sprite.getMaxV() - sprite.getMinV();
		double minU = sprite.getMinU() + du * inset, maxU = sprite.getMaxU() - du * inset;
		double minV = sprite.getMinV() + dv * inset, maxV = sprite.getMaxV() - dv * inset;

		int light = te.getWorldObj().getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, fluid.getLuminosity());

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.setColorRGBA((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, 255);
		double yy = y + height, a = inset, b = 1 - inset;
		t.addVertexWithUV(x + a, yy, z + a, minU, minV);
		t.addVertexWithUV(x + b, yy, z + a, maxU, minV);
		t.addVertexWithUV(x + b, yy, z + b, maxU, maxV);
		t.addVertexWithUV(x + a, yy, z + b, minU, maxV);
		t.draw();
		GL11.glPopAttrib();
	}

	private static EntityItem entityItem;

	/** Draws a stack lying flat, layered (one layer per {@code perLayer} items), like the 1.12 renderers. */
	public static void drawItemLayers(TileEntity te, ItemStack stack, double x, double y, double z, int perLayer) {
		if (stack == null || te.getWorldObj() == null) return;
		if (entityItem == null || entityItem.worldObj != te.getWorldObj()) entityItem = new EntityItem(te.getWorldObj());
		ItemStack one = stack.copy();
		one.stackSize = 1;
		entityItem.setEntityItemStack(one);
		entityItem.hoverStart = 0;
		int layers = (int) Math.ceil(stack.stackSize / (double) perLayer);
		Random rand = new Random(te.getWorldObj().getSeed());
		boolean fancy = RenderItem.renderInFrame;
		RenderItem.renderInFrame = true;
		for (int i = 0; i < layers; i++) {
			GL11.glPushMatrix();
			RenderHelper.enableStandardItemLighting();
			GL11.glTranslated(x + 0.5, y + 0.062 + i * 0.0625, z + 0.5);
			GL11.glRotated(rand.nextFloat() * 360.0, 0, 1.0, 0);
			GL11.glRotated(90, 1, 0, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glTranslated(0, -0.2, 0);
			RenderManager.instance.renderEntityWithPosYaw(entityItem, 0, 0, 0, 0, 0);
			GL11.glPopMatrix();
		}
		RenderItem.renderInFrame = fancy;
	}

}
