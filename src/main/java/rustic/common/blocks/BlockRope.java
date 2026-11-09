package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.blocks.crops.BlockGrapeLeaves;

public class BlockRope extends BlockRopeBase {

    public static class MaterialRope extends Material {

        public MaterialRope() {
            this(MapColor.clothColor);
        }

        public MaterialRope(MapColor mapColor) {
            super(mapColor);
            this.setBurning();
            this.setNoPushMobility();
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    public static final Material MATERIAL_ROPE = new MaterialRope();

    public BlockRope(String name) {
        super(MATERIAL_ROPE, name, true);
        setHardness(0.5F);
        setStepSound(soundTypeCloth);
        Blocks.fire.setFireInfo(this, 20, 60);
    }

    private boolean isAttachable(World world, int ox, int oy, int oz, int axis) {
        Block other = world.getBlock(ox, oy, oz);
        if (other == ModBlocks.STAKE_TIED) return true;
        if (other instanceof BlockLattice) return true;
        if (other == ModBlocks.GRAPE_LEAVES && ((BlockGrapeLeaves) other).getAxis(world, ox, oy, oz) == axis)
            return true;
        return false;
    }

    @Override
    public boolean isSideSupported(World world, int x, int y, int z, ForgeDirection facing) {
        if (facing == ForgeDirection.DOWN) return false;
        if (super.isSideSupported(world, x, y, z, facing)) return true;
        return isAttachable(world, x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, getAxis(world, x, y, z));
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        if (dir == ForgeDirection.UP) {
            return canPlaceBlockOnSide(world, x, y, z, ForgeDirection.DOWN.ordinal());
        }
        if (super.canPlaceBlockOnSide(world, x, y, z, side)) return true;
        ForgeDirection back = dir.getOpposite();
        return isAttachable(world, x + back.offsetX, y + back.offsetY, z + back.offsetZ, axisOf(dir));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityArrow && isArrowInAABB(world, x, y, z, (EntityArrow) entity)) {
            this.dropBlock(world, x, y, z);
        }
    }

    protected boolean isArrowInAABB(World world, int x, int y, int z, EntityArrow entity) {
        int axis = getAxis(world, x, y, z);
        double xExp = axis == AXIS_X ? 0 : 0.125;
        double yExp = axis == AXIS_Y ? 0 : 0.125;
        double zExp = axis == AXIS_Z ? 0 : 0.125;
        setBlockBoundsBasedOnState(world, x, y, z);
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ)
            .expand(xExp, yExp, zExp);
        return entity.boundingBox != null && aabb.intersectsWith(entity.boundingBox);
    }

}
