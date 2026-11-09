package rustic.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import rustic.common.tileentity.TileEntityLiquidBarrel;

/** Renders the liquid barrel item with its stored fluid visible, like the 1.12 filled-barrel item model. */
public class LiquidBarrelItemRenderer implements IItemRenderer {

    private static final double INSET = 0.1875;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        Block block = Block.getBlockFromItem(stack.getItem());
        RenderBlocks renderBlocks = data.length > 0 && data[0] instanceof RenderBlocks ? (RenderBlocks) data[0]
            : RenderBlocks.getInstance();

        GL11.glPushMatrix();
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        renderBlocks.renderBlockAsItem(block, stack.getItemDamage(), 1.0F);

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("FluidName", Constants.NBT.TAG_STRING)
            && tag.hasKey("Amount", Constants.NBT.TAG_INT)) {
            Fluid fluid = FluidRegistry.getFluid(tag.getString("FluidName"));
            int amount = tag.getInteger("Amount");
            if (fluid != null && amount > 0) {
                double height = 0.125 + 0.8125 * Math.min(amount, TileEntityLiquidBarrel.capacity)
                    / (double) TileEntityLiquidBarrel.capacity;
                drawSurface(fluid, height - 0.5);
            }
        }
        GL11.glPopMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static void drawSurface(Fluid fluid, double y) {
        IIcon sprite = fluid.getStillIcon();
        if (sprite == null) return;
        int c = fluid.getColor(new FluidStack(fluid, 1000));
        float du = sprite.getMaxU() - sprite.getMinU(), dv = sprite.getMaxV() - sprite.getMinV();
        double minU = sprite.getMinU() + du * INSET, maxU = sprite.getMaxU() - du * INSET;
        double minV = sprite.getMinV() + dv * INSET, maxV = sprite.getMaxV() - dv * INSET;
        double a = INSET - 0.5, b = 0.5 - INSET;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setNormal(0F, 1F, 0F);
        t.setColorRGBA((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, 255);
        t.addVertexWithUV(a, y, a, minU, minV);
        t.addVertexWithUV(a, y, b, minU, maxV);
        t.addVertexWithUV(b, y, b, maxU, maxV);
        t.addVertexWithUV(b, y, a, maxU, minV);
        t.draw();
        GL11.glPopAttrib();
    }

}
