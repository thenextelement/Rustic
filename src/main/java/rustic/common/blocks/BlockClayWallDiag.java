package rustic.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import rustic.common.util.Facing;

public class BlockClayWallDiag extends BlockRotatable implements IAdvancedRotationPlacement {

    public BlockClayWallDiag() {
        super(Material.clay, "clay_wall_diag");
        setStepSound(soundTypeGravel);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return Facing.horizontalIndex(getFacingForAdvancedRotationPlacement(side, hitX, hitY, hitZ));
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, net.minecraft.entity.EntityLivingBase placer,
        net.minecraft.item.ItemStack stack) {
        // facing already chosen in onBlockPlaced
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

}
