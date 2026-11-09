package rustic.client;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import rustic.client.gui.GuiApiary;
import rustic.client.gui.GuiBarrel;
import rustic.client.gui.GuiBrewingBarrel;
import rustic.client.gui.GuiCabinet;
import rustic.client.gui.GuiCabinetDouble;
import rustic.client.gui.GuiCondenser;
import rustic.client.gui.GuiCondenserAdvanced;
import rustic.client.gui.GuiVase;
import rustic.client.gui.book.GuiBook;
import rustic.common.items.ItemBook;
import rustic.common.tileentity.ContainerApiary;
import rustic.common.tileentity.ContainerBarrel;
import rustic.common.tileentity.ContainerBrewingBarrel;
import rustic.common.tileentity.ContainerCabinet;
import rustic.common.tileentity.ContainerCabinetDouble;
import rustic.common.tileentity.ContainerCondenser;
import rustic.common.tileentity.ContainerCondenserAdvanced;
import rustic.common.tileentity.ContainerVase;
import rustic.common.tileentity.TileEntityApiary;
import rustic.common.tileentity.TileEntityBarrel;
import rustic.common.tileentity.TileEntityBrewingBarrel;
import rustic.common.tileentity.TileEntityCabinet;
import rustic.common.tileentity.TileEntityCondenser;
import rustic.common.tileentity.TileEntityCondenserAdvancedBottom;
import rustic.common.tileentity.TileEntityVase;

/** Server and client GUIs are chosen by tile entity type; the book uses its own id. */
public class GuiProxy implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityApiary) {
			return new ContainerApiary(player.inventory, (TileEntityApiary) te);
		} else if (te instanceof TileEntityVase) {
			return ((TileEntityVase) te).createContainer(player.inventory, player);
		} else if (te instanceof TileEntityBarrel) {
			return ((TileEntityBarrel) te).createContainer(player.inventory, player);
		} else if (te instanceof TileEntityCabinet) {
			return ((TileEntityCabinet) te).createContainer(player.inventory, player);
		} else if (te instanceof TileEntityCondenser) {
			return new ContainerCondenser(player.inventory, (TileEntityCondenser) te);
		} else if (te instanceof TileEntityCondenserAdvancedBottom) {
			return new ContainerCondenserAdvanced(player.inventory, (TileEntityCondenserAdvancedBottom) te);
		} else if (te instanceof TileEntityBrewingBarrel) {
			return new ContainerBrewingBarrel(player.inventory, (TileEntityBrewingBarrel) te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == ItemBook.GUI_ID) {
			return new GuiBook();
		}
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityApiary) {
			return new GuiApiary(new ContainerApiary(player.inventory, (TileEntityApiary) te), player.inventory);
		} else if (te instanceof TileEntityVase) {
			return new GuiVase((ContainerVase) ((TileEntityVase) te).createContainer(player.inventory, player), player.inventory);
		} else if (te instanceof TileEntityBarrel) {
			return new GuiBarrel((ContainerBarrel) ((TileEntityBarrel) te).createContainer(player.inventory, player), player.inventory);
		} else if (te instanceof TileEntityCabinet) {
			Object container = ((TileEntityCabinet) te).createContainer(player.inventory, player);
			if (container instanceof ContainerCabinetDouble) {
				return new GuiCabinetDouble((ContainerCabinetDouble) container, player.inventory);
			}
			return new GuiCabinet((ContainerCabinet) container, player.inventory);
		} else if (te instanceof TileEntityCondenser) {
			return new GuiCondenser(new ContainerCondenser(player.inventory, (TileEntityCondenser) te), player.inventory);
		} else if (te instanceof TileEntityCondenserAdvancedBottom) {
			return new GuiCondenserAdvanced(new ContainerCondenserAdvanced(player.inventory, (TileEntityCondenserAdvancedBottom) te), player.inventory);
		} else if (te instanceof TileEntityBrewingBarrel) {
			return new GuiBrewingBarrel(new ContainerBrewingBarrel(player.inventory, (TileEntityBrewingBarrel) te), player.inventory);
		}
		return null;
	}

}
