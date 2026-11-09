package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import rustic.common.tileentity.TileEntityCondenser;

public class BlockCondenser extends BlockCondenserBase {

    public static final int GUI_ID = 4;

    public BlockCondenser() {
        super("condenser");
    }

    @Override
    protected int getGuiId() {
        return GUI_ID;
    }

    @Override
    public Block getRetort() {
        return ModBlocks.RETORT;
    }

    @Override
    protected boolean needsBackRetort() {
        return false;
    }

    @Override
    public boolean hasTileEntity(int meta) {
        return isBottom(meta);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return isBottom(meta) ? new TileEntityCondenser() : null;
    }

}
