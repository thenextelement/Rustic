package rustic.common.blocks.fluids;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.fluids.Fluid;

/** A fluid whose still/flowing icons are registered from texture names (1.7.10 fluids hold IIcons). */
public class FluidRustic extends Fluid {

	public static final List<FluidRustic> ALL = new ArrayList<>();

	private final String stillTexture;
	private final String flowTexture;

	/** @param still icon name, e.g. "rustic:fluids/olive_oil_still" */
	public FluidRustic(String fluidName, String still, String flowing) {
		super(fluidName);
		this.stillTexture = still;
		this.flowTexture = flowing;
		ALL.add(this);
	}

	/** Converts 1.12 texture paths ("rustic:blocks/fluids/x") to 1.7.10 icon names ("rustic:fluids/x"). */
	public static String icon(String texture) {
		return texture.replace(":blocks/", ":");
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		setIcons(register.registerIcon(stillTexture), register.registerIcon(flowTexture));
	}

	@Override
	public FluidRustic setDensity(int density) {
		super.setDensity(density);
		return this;
	}

	@Override
	public FluidRustic setViscosity(int viscosity) {
		super.setViscosity(viscosity);
		return this;
	}

	@Override
	public FluidRustic setRarity(EnumRarity rarity) {
		super.setRarity(rarity);
		return this;
	}

}
