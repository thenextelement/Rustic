package rustic.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class TileEntityVase extends TileEntityInventoryBase {

	public TileEntityVase() {
		super(27);
	}

	@Override
	protected String getDefaultName() {
		return "container.rustic.vase";
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
		return new ContainerVase(playerInventory, this);
	}

}
