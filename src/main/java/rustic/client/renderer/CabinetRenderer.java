package rustic.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import rustic.client.models.ModelCabinet;
import rustic.client.models.ModelCabinetDouble;
import rustic.client.util.ItemColorCache;
import rustic.common.blocks.BlockCabinet;
import rustic.common.blocks.ModBlocks;
import rustic.common.tileentity.TileEntityCabinet;

public class CabinetRenderer extends TileEntitySpecialRenderer {

    protected static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("rustic:textures/models/cabinet.png");
    protected static final ResourceLocation TEXTURE_DOUBLE = new ResourceLocation(
        "rustic:textures/models/cabinet_double.png");
    protected static final ResourceLocation TEXTURE_NORMAL_COLOR = new ResourceLocation(
        "rustic:textures/models/cabinet_color.png");
    protected static final ResourceLocation TEXTURE_DOUBLE_COLOR = new ResourceLocation(
        "rustic:textures/models/cabinet_double_color.png");

    protected final ModelCabinet simpleCabinet = new ModelCabinet(false);
    protected final ModelCabinet simpleCabinetM = new ModelCabinet(true);
    protected final ModelCabinetDouble doubleCabinet = new ModelCabinetDouble(false);
    protected final ModelCabinetDouble doubleCabinetM = new ModelCabinetDouble(true);

    private static void applyMaterialColor(ItemStack material) {
        if (material != null) {
            int color = ItemColorCache.INSTANCE.getColor(material);
            GL11.glColor4f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1F);
        } else {
            GL11.glColor4f(1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        TileEntityCabinet te = (TileEntityCabinet) tile;
        if (te.getWorldObj() == null || te.getWorldObj()
            .getBlock(te.xCoord, te.yCoord, te.zCoord) != ModBlocks.CABINET) return;
        int meta = te.getWorldObj()
            .getBlockMetadata(te.xCoord, te.yCoord, te.zCoord);
        if (BlockCabinet.isTop(meta)) return;

        boolean mirror = BlockCabinet.isMirrored(meta);
        boolean renderDouble = BlockCabinet.isBottom(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
        ItemStack material = te.material;
        boolean colored = material == null;

        if (renderDouble) bindTexture(colored ? TEXTURE_DOUBLE_COLOR : TEXTURE_DOUBLE);
        else bindTexture(colored ? TEXTURE_NORMAL_COLOR : TEXTURE_NORMAL);

        GL11.glPushMatrix();
        // restore afterwards: leaving it off breaks the lighting of every item NEI draws after this one
        boolean rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        applyMaterialColor(material);
        GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);

        ForgeDirection facing = BlockCabinet.getFacing(meta);
        int rotation = facing == ForgeDirection.WEST ? 90
            : facing == ForgeDirection.NORTH ? 180 : facing == ForgeDirection.EAST ? 270 : 0;
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
        f = 1.0F - f;
        f = 1.0F - f * f * f;
        float angle = (mirror ? -1 : 1) * f * ((float) Math.PI / 2F);
        GL11.glTranslatef(0.5F, -0.5F, 0.5F);
        if (renderDouble) {
            ModelCabinetDouble model = mirror ? doubleCabinetM : doubleCabinet;
            model.setRotation(model.door, 0, angle, 0);
            model.renderAll();
        } else {
            ModelCabinet model = mirror ? simpleCabinetM : simpleCabinet;
            model.setRotation(model.door, 0, angle, 0);
            model.renderAll();
        }
        if (!rescale) GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Item form of the cabinet, tinted with its material. */
    public static class CabinetItemRenderer implements IItemRenderer {

        private final ModelCabinet model = new ModelCabinet(false);

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
            ItemStack material = null;
            if (stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("material")) {
                material = ItemStack.loadItemStackFromNBT(
                    stack.getTagCompound()
                        .getCompoundTag("material"));
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(material == null ? TEXTURE_NORMAL_COLOR : TEXTURE_NORMAL);
            GL11.glPushMatrix();
            // restore afterwards: leaving it off breaks the lighting of every item NEI draws after this one
            boolean rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            applyMaterialColor(material);
            // the model below ends up centred on (0, -0.5, 0); INVENTORY and ENTITY frames are block-centred, the
            // EQUIPPED helpers shift the origin by -0.5 on every axis
            GL11.glTranslatef(0F, 0.5F, 0F);
            if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            }
            GL11.glTranslatef(-0.5F, 1F, 0.5F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            model.renderAll();
            if (!rescale) GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

    }

}
