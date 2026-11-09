package rustic.common.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import rustic.client.json.JsonModelLoader;
import rustic.core.Rustic;

public class ItemBase extends Item {

	protected final String name;

	public ItemBase(String name) {
		super();
		this.name = name;
		setUnlocalizedName(Rustic.MODID + "." + name);
		setCreativeTab(Rustic.farmingTab);
		GameRegistry.registerItem(this, name);
	}

	public String getRegistryName() {
		return name;
	}

	/** Icon name from the item's 1.12 model JSON (layer0), falling back to "rustic:{name}". */
	@SideOnly(Side.CLIENT)
	public static String iconFor(String name) {
		String icon = JsonModelLoader.itemIcon(name);
		return icon != null ? icon : Rustic.MODID + ":" + name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(iconFor(name));
	}

}
