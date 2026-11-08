package rustic.common.items;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IColoredItem {
	
	@SideOnly(Side.CLIENT)
    public IItemColor getItemColor();

}
