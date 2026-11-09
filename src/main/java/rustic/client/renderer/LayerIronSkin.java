package rustic.client.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.potions.PotionsRustic;
import rustic.core.Rustic;

/**
 * The iron skin / fullmetal visual overlay. 1.12 used a render layer; 1.7.10 has none, so while an affected entity is
 * rendered its renderer's {@code mainModel} is temporarily swapped for a wrapper that draws the real model and then
 * draws it again with the metal texture, depth-tested with GL_EQUAL so it only covers the visible skin (the same trick
 * vanilla uses for the enchantment glint).
 */
@SideOnly(Side.CLIENT)
public final class LayerIronSkin {

    public static final Set<String> DISABLED_ENTITIES = new HashSet<>();

    private static final Set<Class<?>> FAILED_RENDERERS = new HashSet<>();
    private static final Map<ModelBase, IronSkinModel> WRAPPERS = new IdentityHashMap<>();

    private static Field mainModelField;
    private static Method bindEntityTextureMethod;
    private static boolean reflectionFailed;

    private LayerIronSkin() {}

    public static void disableFor(String entityName) {
        if (entityName != null && !entityName.trim()
            .isEmpty()) DISABLED_ENTITIES.add(normalize(entityName));
    }

    private static String normalize(String name) {
        name = name.trim()
            .toLowerCase(Locale.ROOT);
        return name.startsWith("minecraft:") ? name.substring("minecraft:".length()) : name;
    }

    private static boolean isDisabled(EntityLivingBase entity) {
        if (DISABLED_ENTITIES.isEmpty()) return false;
        String name = entity instanceof EntityPlayer ? "player" : EntityList.getEntityString(entity);
        return name != null && DISABLED_ENTITIES.contains(normalize(name));
    }

    private static boolean initReflection() {
        if (reflectionFailed) return false;
        if (mainModelField != null) return true;
        try {
            mainModelField = ReflectionHelper.findField(RendererLivingEntity.class, "mainModel", "field_77045_g");
            bindEntityTextureMethod = ReflectionHelper
                .findMethod(Render.class, null, new String[] { "bindEntityTexture", "func_110777_b" }, Entity.class);
            return true;
        } catch (Exception e) {
            Rustic.logger.warn("Iron skin overlay disabled: unable to access entity renderer internals", e);
            reflectionFailed = true;
            return false;
        }
    }

    /** Returns the overlay opacity, or a negative value if the entity has no iron skin effect. */
    private static float getOpacity(EntityLivingBase entity) {
        if (entity.isPotionActive(PotionsRustic.FULLMETAL_POTION)) return 0.92F;
        PotionEffect ironSkin = entity.getActivePotionEffect(PotionsRustic.IRON_SKIN_POTION);
        if (ironSkin == null) return -1F;
        switch (ironSkin.getAmplifier()) {
            case 0:
                return 0.875F;
            case 1:
                return 0.89F;
            case 2:
                return 0.91F;
            default:
                return 0.92F;
        }
    }

    public static class Handler {

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onRenderPre(RenderLivingEvent.Pre event) {
            if (event.isCanceled()) return;
            EntityLivingBase entity = event.entity;
            RendererLivingEntity renderer = event.renderer;
            if (entity.isInvisible() || FAILED_RENDERERS.contains(renderer.getClass())) return;
            float opacity = getOpacity(entity);
            if (opacity < 0 || isDisabled(entity) || !initReflection()) return;
            try {
                ModelBase model = (ModelBase) mainModelField.get(renderer);
                if (model == null || model instanceof IronSkinModel) return;
                IronSkinModel wrapper = WRAPPERS.get(model);
                if (wrapper == null) {
                    wrapper = new IronSkinModel(model);
                    WRAPPERS.put(model, wrapper);
                }
                wrapper.renderer = renderer;
                wrapper.opacity = opacity;
                mainModelField.set(renderer, wrapper);
            } catch (Exception e) {
                FAILED_RENDERERS.add(renderer.getClass());
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void onRenderPost(RenderLivingEvent.Post event) {
            if (mainModelField == null) return;
            try {
                Object model = mainModelField.get(event.renderer);
                if (model instanceof IronSkinModel) {
                    IronSkinModel wrapper = (IronSkinModel) model;
                    mainModelField.set(event.renderer, wrapper.base);
                    wrapper.renderer = null;
                }
            } catch (Exception ignored) {}
        }
    }

    private static class IronSkinModel extends ModelBase {

        final ModelBase base;
        final ResourceLocation metalTexture;
        RendererLivingEntity renderer;
        float opacity;

        IronSkinModel(ModelBase base) {
            this.base = base;
            this.textureWidth = base.textureWidth;
            this.textureHeight = base.textureHeight;
            this.boxList = base.boxList;
            this.metalTexture = new ResourceLocation(
                Rustic.MODID,
                "textures/entity/layer/fullmetal_" + sizeBucket(base.textureWidth)
                    + "_"
                    + sizeBucket(base.textureHeight)
                    + ".png");
        }

        private static int sizeBucket(int size) {
            if (size >= 128) return 128;
            if (size >= 64) return 64;
            if (size >= 32) return 32;
            return 16;
        }

        private void syncState() {
            base.onGround = onGround;
            base.isRiding = isRiding;
            base.isChild = isChild;
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
            syncState();
            base.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            // The hurt flash re-renders the model untextured; only overlay the normal textured pass.
            if (renderer == null || !GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) return;

            boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(metalTexture);
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1F, 1F, 1F, opacity);

            base.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            GL11.glColor4f(1F, 1F, 1F, 1F);
            if (!blend) GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            try {
                bindEntityTextureMethod.invoke(renderer, entity);
            } catch (Exception ignored) {}
        }

        @Override
        public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale, Entity entity) {
            syncState();
            base.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        }

        @Override
        public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks) {
            syncState();
            base.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        }

        @Override
        public ModelRenderer getRandomModelBox(Random rand) {
            return base.getRandomModelBox(rand);
        }

        @Override
        public TextureOffset getTextureOffset(String name) {
            return base.getTextureOffset(name);
        }
    }

}
