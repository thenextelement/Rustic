package rustic.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import rustic.common.tileentity.TileEntityCrushingTub;

public class CrushingTubRenderer extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		TileEntityCrushingTub tub = (TileEntityCrushingTub) te;
		TankRenderHelper.drawItemLayers(te, tub.getItemHandler().getStackInSlot(0), x, y, z, 8);
		if (tub.getFluid() != null && tub.getAmount() > 0) {
			TankRenderHelper.drawFluidSurface(te, tub.getFluid(), x, y, z, 0.0625,
					0.0625 + 0.5 * tub.getAmount() / (double) tub.getCapacity());
		}
	}

}
