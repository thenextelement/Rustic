package rustic.common.potions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class PotionBase extends Potion {

	public static ResourceLocation POTION_ICONS = new ResourceLocation("rustic:textures/gui/potion_icons.png");

	public PotionBase(int id, boolean isBadEffect, int color, String name) {
		super(id, isBadEffect, color);
		setPotionName("effect." + name);
	}

	@Override
	public PotionBase setIconIndex(int x, int y) {
		super.setIconIndex(x, y);
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		Minecraft.getMinecraft().getTextureManager().bindTexture(POTION_ICONS);
		return super.getStatusIconIndex();
	}

	/** Like 1.12's isReady: whether performEffect should run this tick. */
	@Override
	public boolean isReady(int duration, int amplifier) {
		return false;
	}

}
