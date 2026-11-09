package rustic.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.authlib.GameProfile;

import rustic.core.Rustic;

public class ClientUtils {

    private static final Map<String, Integer> FLUID_COLORS = new HashMap<>();

    public static void drawTexturedColoredRect(float x, float y, float width, float height, float minU, float minV,
        float maxU, float maxV, int color) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
        t.addVertexWithUV(x, y + height, 0, minU, maxV);
        t.addVertexWithUV(x + width, y + height, 0, maxU, maxV);
        t.addVertexWithUV(x + width, y, 0, maxU, minV);
        t.addVertexWithUV(x, y, 0, minU, minV);
        t.draw();
    }

    /** The icon an item stack renders with (block icon for item blocks). */
    public static IIcon getItemTexture(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        if (stack.getItem() instanceof ItemBlock && stack.getItem()
            .getSpriteNumber() == 0) {
            Block block = ((ItemBlock) stack.getItem()).field_150939_a;
            return block.getIcon(2, stack.getItemDamage());
        }
        return stack.getItem()
            .getIconIndex(stack);
    }

    public static int getItemColor(ItemStack stack) {
        return getTextureColor(getItemTexture(stack));
    }

    /** Average colour of the fluid's still texture (Rustic's fluids have no colour of their own). */
    public static int getFluidColor(FluidStack fluid) {
        Fluid f = fluid.getFluid();
        if (f.getColor(fluid) != 0xFFFFFF) return f.getColor(fluid);
        Integer cached = FLUID_COLORS.get(f.getName());
        if (cached == null) {
            cached = getTextureColor(f.getStillIcon()) & 0xFFFFFF;
            FLUID_COLORS.put(f.getName(), cached);
        }
        return cached;
    }

    public static void clearColorCaches() {
        FLUID_COLORS.clear();
    }

    /** Returns the average colour of a texture. */
    public static int getTextureColor(IIcon icon) {
        if (!(icon instanceof TextureAtlasSprite)) return 0xFFFF00FF;
        TextureAtlasSprite sprite = (TextureAtlasSprite) icon;
        if (sprite.getFrameCount() <= 0) return 0xFFFF00FF;
        int[] data = sprite.getFrameTextureData(0)[0];
        int numPixels = 0, r = 0, g = 0, b = 0;
        for (int c : data) {
            if (((c >> 24) & 0xFF) <= 0) continue;
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
            numPixels++;
        }
        if (numPixels == 0) return 0xFFFFFFFF;
        r = MathHelper.clamp_int(Math.round((float) r / numPixels), 0, 255);
        g = MathHelper.clamp_int(Math.round((float) g / numPixels), 0, 255);
        b = MathHelper.clamp_int(Math.round((float) b / numPixels), 0, 255);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public static String getQualityTooltip(float quality) {
        int qualityVariant = 0;
        Minecraft mc = Minecraft.getMinecraft();
        UUID playerId = null;
        if (mc.thePlayer != null) {
            playerId = mc.thePlayer.getUniqueID();
        } else if (mc.getSession() != null) {
            GameProfile profile = mc.getSession()
                .func_148256_e();
            if (profile != null && profile.getId() != null) {
                playerId = profile.getId();
            }
        }
        if (playerId != null) {
            qualityVariant = playerId.equals(Rustic.ERIS_UUID) ? 7 : playerId.hashCode() & 7;
        }
        String tier;
        EnumChatFormatting qualityColor;
        if (quality >= 0.89999F) {
            tier = "highest";
            qualityColor = EnumChatFormatting.GOLD;
        } else if (quality >= 0.69999F) {
            tier = "high";
            qualityColor = EnumChatFormatting.LIGHT_PURPLE;
        } else if (quality >= 0.5F) {
            tier = "highish";
            qualityColor = EnumChatFormatting.AQUA;
        } else if (quality >= 0.35F) {
            tier = "lowish";
            qualityColor = EnumChatFormatting.YELLOW;
        } else if (quality >= 0.2F) {
            tier = "low";
            qualityColor = EnumChatFormatting.DARK_PURPLE;
        } else {
            tier = "lowest";
            qualityColor = EnumChatFormatting.DARK_RED;
        }
        return EnumChatFormatting.GRAY + I18n.format(
            "tooltip.rustic.quality.desc",
            qualityColor + I18n.format("tooltip.rustic.quality." + tier + "." + qualityVariant),
            String.format("%.0f%%", quality * 100));
    }

    public static int getQualityLabelColor(float quality) {
        if (quality >= 0.89999F) {
            return 0xC29311; // highest tier, gold
        } else if (quality >= 0.69999F) {
            return 0x4A7ABD; // high tier, blue
        } else if (quality >= 0.5F) {
            return 0xDBD3BD; // high-ish tier, parchment
        } else if (quality >= 0.35F) {
            return 0xDBD3BD; // low-ish tier, parchment
        } else if (quality >= 0.2F) {
            return 0xBAA911; // low tier, yellow
        } else {
            return 0x222222; // lowest tier, black
        }
    }

}
