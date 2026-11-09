package rustic.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class TileEntityBarrel extends TileEntityInventoryBase {

    public TileEntityBarrel() {
        super(27);
    }

    @Override
    protected String getDefaultName() {
        return "container.rustic.barrel";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
        return new ContainerBarrel(playerInventory, this);
    }

}
