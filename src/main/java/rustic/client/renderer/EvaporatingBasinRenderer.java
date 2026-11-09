package rustic.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import rustic.common.tileentity.TileEntityEvaporatingBasin;

public class EvaporatingBasinRenderer extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		TileEntityEvaporatingBasin basin = (TileEntityEvaporatingBasin) te;
		TankRenderHelper.drawItemLayers(te, basin.getItemHandler().getStackInSlot(0), x, y, z, 16);
		if (basin.getFluid() != null && basin.getAmount() > 0) {
			TankRenderHelper.drawFluidSurface(te, basin.getFluid(), x, y, z, 0.1875,
					0.0625 + 0.1875 * basin.getAmount() / (double) basin.getCapacity());
		}
	}

}
