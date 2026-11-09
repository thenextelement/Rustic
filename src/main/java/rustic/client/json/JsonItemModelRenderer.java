package rustic.client.json;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import rustic.core.Rustic;

/**
 * Renders an item from the elements of its 1.12 item model (e.g. the almanac's 3D book) instead of as a flat sprite.
 * Every face whose texture is the model's particle texture is drawn with the item's own icon; other textures (like the
 * font sheet used for 1.12's baked-on title) are skipped.
 */
public class JsonItemModelRenderer implements IItemRenderer {

    private final String modelName;
    private ModelPart part;
    private IIcon partIcon;

    /** @param itemName the item model name under rustic:item/ */
    public JsonItemModelRenderer(String itemName) {
        this.modelName = Rustic.MODID + ":item/" + itemName;
    }

    private ModelPart getPart(IIcon icon) {
        if (part != null && partIcon == icon) return part;
        JsonModel model = JsonModelLoader.getModel(modelName);
        if (model == null || model.elements.isEmpty()) return null;
        String main = model.resolveTexture("#particle");
        IIcon[][] icons = new IIcon[model.elements.size()][6];
        for (int i = 0; i < model.elements.size(); i++) {
            JsonModel.Element el = model.elements.get(i);
            for (int f = 0; f < 6; f++) {
                if (el.faces[f] == null) continue;
                String tex = model.resolveTexture(el.faces[f].texture);
                if (tex != null && tex.equals(main)) icons[i][f] = icon;
            }
        }
        partIcon = icon;
        part = new ModelPart(model, 0, 0, false, icons, icon);
        return part;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        // held: use the block frame (cube centred where a held block is), which matches 1.12's hand frame closely
        return helper == ItemRendererHelper.EQUIPPED_BLOCK;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        IIcon icon = item.getIconIndex();
        ModelPart p = icon == null ? null : getPart(icon);
        if (p == null) return;
        java.util.Map<String, float[][]> display = p.model.display;

        GL11.glPushMatrix();
        boolean rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_CULL_FACE);
        switch (type) {
            case INVENTORY: {
                // same setup as 1.12's RenderItem.renderItemModelIntoGUI
                boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glTranslatef(8F, 8F, 0F);
                GL11.glScalef(16F, -16F, 16F);
                applyTransform(display.get("gui"));
                draw(p);
                if (!lighting) GL11.glDisable(GL11.GL_LIGHTING);
                break;
            }
            case EQUIPPED_FIRST_PERSON:
                // undo the helper's -0.5 shift, then 1.7.10's held-block frame (rotate 45 Y, scale 0.4 on 1.12's hand
                // frame) back to the plain hand frame 1.12's firstperson transform is relative to
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(2.5F, 2.5F, 2.5F);
                GL11.glRotatef(-45F, 0F, 1F, 0F);
                applyTransform(display.get("firstperson_righthand"));
                draw(p);
                break;
            case EQUIPPED:
                // undo 1.7.10's third-person item transform (RenderBiped / RenderPlayer, non-block items), then apply
                // 1.12's LayerHeldItem right-hand transform
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glRotatef(-20F, 0F, 0F, 1F);
                GL11.glRotatef(90F, 1F, 0F, 0F);
                GL11.glRotatef(-60F, 0F, 0F, 1F);
                GL11.glScalef(1F / 0.375F, 1F / 0.375F, 1F / 0.375F);
                GL11.glTranslatef(-0.25F, -0.1875F, 0.1875F);
                GL11.glRotatef(-90F, 1F, 0F, 0F);
                GL11.glRotatef(180F, 0F, 1F, 0F);
                GL11.glTranslatef(1F / 16F, 0.125F, -0.625F);
                applyTransform(display.get("thirdperson_righthand"));
                draw(p);
                break;
            case ENTITY:
            default:
                applyTransform(display.get("ground"));
                draw(p);
                break;
        }
        if (cull) GL11.glEnable(GL11.GL_CULL_FACE);
        if (!rescale) GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    /**
     * Apply a 1.12 item display transform (Forge order: translate, rotate X then Y then Z, scale) to a model centred on
     * the origin (units: 1 = one block).
     */
    private static void applyTransform(float[][] t) {
        if (t == null) return;
        GL11.glTranslatef(t[1][0] / 16F, t[1][1] / 16F, t[1][2] / 16F);
        GL11.glRotatef(t[0][0], 1F, 0F, 0F);
        GL11.glRotatef(t[0][1], 0F, 1F, 0F);
        GL11.glRotatef(t[0][2], 0F, 0F, 1F);
        GL11.glScalef(t[2][0], t[2][1], t[2][2]);
    }

    /** Draws the model centred on the origin. */
    private static void draw(ModelPart p) {
        Tessellator t = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        JsonModelRenderer.renderPart(p, null, null, 0, 0, 0, 0xFFFFFF, t, null, true);
        t.draw();
    }

}
