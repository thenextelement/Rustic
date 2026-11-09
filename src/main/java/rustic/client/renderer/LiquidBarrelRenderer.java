package rustic.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import rustic.common.tileentity.TileEntityLiquidBarrel;

public class LiquidBarrelRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLiquidBarrel barrel = (TileEntityLiquidBarrel) te;
        if (barrel.getFluid() != null && barrel.getAmount() > 0) {
            TankRenderHelper.drawFluidSurface(
                te,
                barrel.getFluid(),
                x,
                y,
                z,
                0.1875,
                0.125 + 0.8125 * barrel.getAmount() / (double) barrel.getCapacity());
        }
    }

}
