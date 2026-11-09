package rustic.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import rustic.core.Rustic;

public class ItemBook extends ItemBase {

	public static final int GUI_ID = 96;

	public ItemBook() {
		super("book");
		setMaxStackSize(1);
		setMaxDamage(0);
		setCreativeTab(Rustic.decorTab);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.openGui(Rustic.instance, GUI_ID, world, MathHelper.floor_double(player.posX),
				MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		return stack;
	}

}
