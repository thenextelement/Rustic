package rustic.client.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FluidClientUtil {

	public static void drawGuiLiquid(FluidStack fluid, int capacity, int x, int y, int width, int height,
			ResourceLocation ogTexture) {
		if (fluid != null && fluid.getFluid() != null) {
			int fluidHeight = (int) (height * (fluid.amount / (float) capacity));
			drawFluidStack(fluid, x, y + height - fluidHeight, width, fluidHeight);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ogTexture);
			GL11.glColor4f(1, 1, 1, 1);
		}
	}

	public static void drawFluidStack(FluidStack fluid, int x, int y, int width, int height) {
		if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) return;
		IIcon sprite = fluid.getFluid().getStillIcon();
		if (sprite == null) return;
		int color = fluid.getFluid().getColor(fluid) | 0xFF000000;
		float minU = sprite.getMinU(), maxU = sprite.getMaxU(), minV = sprite.getMinV(), maxV = sprite.getMaxV();
		float uDiff = maxU - minU, vDiff = maxV - minV;
		int spriteWidth = sprite.getIconWidth(), spriteHeight = sprite.getIconHeight();
		if (spriteWidth <= 0 || spriteHeight <= 0) return;
		// draw at GUI scale: 16x16 tiles regardless of texture resolution
		spriteWidth = 16;
		spriteHeight = 16;
		int iterationsY = height / spriteHeight;
		float leftOverY = height % spriteHeight;
		float leftOverNormY = leftOverY / spriteHeight;
		int iterationsX = width / spriteWidth;
		float leftOverX = width % spriteWidth;
		float leftOverNormX = leftOverX / spriteWidth;

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		for (int i = 0; i < iterationsY; i++) {
			for (int j = 0; j < iterationsX; j++) {
				ClientUtils.drawTexturedColoredRect(x + (spriteWidth * j), y + (spriteHeight * i), spriteWidth,
						spriteHeight, minU, minV, maxU, maxV, color);
			}
			if (leftOverX > 0) {
				ClientUtils.drawTexturedColoredRect(x + (spriteWidth * iterationsX), y + (spriteHeight * i), leftOverX,
						spriteHeight, minU, minV, minU + (uDiff * leftOverNormX), maxV, color);
			}
		}
		if (leftOverY > 0) {
			for (int j = 0; j < iterationsX; j++) {
				ClientUtils.drawTexturedColoredRect(x + (spriteWidth * j), y + (spriteHeight * iterationsY), spriteWidth,
						leftOverY, minU, minV, maxU, minV + (vDiff * leftOverNormY), color);
			}
			if (leftOverX > 0) {
				ClientUtils.drawTexturedColoredRect(x + (spriteWidth * iterationsX), y + (spriteHeight * iterationsY),
						leftOverX, leftOverY, minU, minV, minU + (uDiff * leftOverNormX), minV + (vDiff * leftOverNormY), color);
			}
		}
	}

}
