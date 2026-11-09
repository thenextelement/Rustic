package rustic.common.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import rustic.core.Rustic;

public class ItemFoodBase extends ItemFood {

	protected final String name;

	public ItemFoodBase(String name, int hunger, float saturation, boolean wolfFood) {
		super(hunger, saturation, wolfFood);
		this.name = name;
		setUnlocalizedName(Rustic.MODID + "." + name);
		setCreativeTab(Rustic.farmingTab);
		GameRegistry.registerItem(this, name);
		initFood();
	}

	public void initFood() {}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ItemBase.iconFor(name));
	}

}
