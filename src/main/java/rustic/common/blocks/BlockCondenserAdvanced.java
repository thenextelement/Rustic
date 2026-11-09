package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import rustic.common.tileentity.TileEntityCondenserAdvancedBottom;
import rustic.common.tileentity.TileEntityCondenserAdvancedTop;

public class BlockCondenserAdvanced extends BlockCondenserBase {

    public static final int GUI_ID = 6;

    public BlockCondenserAdvanced() {
        super("condenser_advanced");
    }

    @Override
    protected int getGuiId() {
        return GUI_ID;
    }

    @Override
    public Block getRetort() {
        return ModBlocks.RETORT_ADVANCED;
    }

    @Override
    protected boolean needsBackRetort() {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return isBottom(meta) ? new TileEntityCondenserAdvancedBottom() : new TileEntityCondenserAdvancedTop();
    }

}
