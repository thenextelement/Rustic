package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.util.Facing;

/** Metadata (like torches): 1 = east, 2 = west, 3 = south, 4 = north, 5 = standing. */
public class BlockCandle extends BlockBase implements IModelBlock, ISupportShape {

    protected static final ForgeDirection[] PLACEMENTS = { ForgeDirection.UP, ForgeDirection.NORTH,
        ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

    public BlockCandle() {
        this("candle");
    }

    public BlockCandle(String name) {
        this(Material.circuits, name, true);
    }

    public BlockCandle(Material mat, String name, boolean register) {
        super(mat, name, register);
        this.setHardness(1F);
        this.setLightLevel(1.0F);
        setStepSound(soundTypeMetal);
    }

    public static ForgeDirection getFacing(int meta) {
        switch (meta & 7) {
            case 1:
                return ForgeDirection.EAST;
            case 2:
                return ForgeDirection.WEST;
            case 3:
                return ForgeDirection.SOUTH;
            case 4:
                return ForgeDirection.NORTH;
            default:
                return ForgeDirection.UP;
        }
    }

    /** Facing for the given metadata; overridden by blocks with a different metadata layout. */
    public ForgeDirection facingOf(int meta) {
        return getFacing(meta);
    }

    public static int metaForFacing(ForgeDirection dir) {
        switch (dir) {
            case EAST:
                return 1;
            case WEST:
                return 2;
            case SOUTH:
                return 3;
            case NORTH:
                return 4;
            default:
                return 5;
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    protected boolean canPlaceOn(World world, int x, int y, int z) {
        if (ISupportShape.canSupport(world, x, y, z, ForgeDirection.UP)) return true;
        Block block = world.getBlock(x, y, z);
        return block.canPlaceTorchOnTop(world, x, y, z);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        for (ForgeDirection dir : PLACEMENTS) {
            if (canPlaceAt(world, x, y, z, dir)) return true;
        }
        return false;
    }

    protected boolean canPlaceAt(World world, int x, int y, int z, ForgeDirection facing) {
        ForgeDirection back = facing.getOpposite();
        int bx = x + back.offsetX, by = y + back.offsetY, bz = z + back.offsetZ;
        if (facing.offsetY == 0) return world.isSideSolid(bx, by, bz, facing, true);
        return facing == ForgeDirection.UP && canPlaceOn(world, bx, by, bz);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        ForgeDirection facing = ForgeDirection.getOrientation(side);
        if (canPlaceAt(world, x, y, z, facing)) return metaForFacing(facing);
        for (ForgeDirection dir : Facing.HORIZONTALS) {
            if (world.isSideSolid(x - dir.offsetX, y, z - dir.offsetZ, dir, true)) return metaForFacing(dir);
        }
        return metaForFacing(ForgeDirection.UP);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        checkForDrop(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        checkForDrop(world, x, y, z);
    }

    protected boolean checkForDrop(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != this) return false;
        int meta = world.getBlockMetadata(x, y, z);
        if (canPlaceAt(world, x, y, z, facingOf(meta))) return true;
        dropBlockAsItem(world, x, y, z, meta, 0);
        world.setBlockToAir(x, y, z);
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        switch (facingOf(world.getBlockMetadata(x, y, z))) {
            case EAST:
                setBlockBounds(0.0F, 0.0F, 0.35F, 0.3F, 0.8F, 0.65F);
                break;
            case WEST:
                setBlockBounds(0.7F, 0.0F, 0.35F, 1.0F, 0.8F, 0.65F);
                break;
            case SOUTH:
                setBlockBounds(0.35F, 0.0F, 0.0F, 0.65F, 0.8F, 0.3F);
                break;
            case NORTH:
                setBlockBounds(0.35F, 0.0F, 0.7F, 0.65F, 0.8F, 1.0F);
                break;
            default:
                setBlockBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.9375F, 0.6F);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.9375F, 0.6F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        ForgeDirection facing = facingOf(world.getBlockMetadata(x, y, z));
        double d0 = x + 0.5D;
        double d1 = y + 0.7D;
        double d2 = z + 0.5D;
        if (facing.offsetY == 0) {
            ForgeDirection back = facing.getOpposite();
            world.spawnParticle("smoke", d0 + 0.27D * back.offsetX, d1 + 0.25D, d2 + 0.27D * back.offsetZ, 0, 0, 0);
            world.spawnParticle("flame", d0 + 0.27D * back.offsetX, d1 + 0.25D, d2 + 0.27D * back.offsetZ, 0, 0, 0);
        } else {
            world.spawnParticle("smoke", d0, d1 + 0.33D, d2, 0, 0, 0);
            world.spawnParticle("flame", d0, d1 + 0.33D, d2, 0, 0, 0);
        }
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN;
    }

    @Override
    public String getBlockStateName() {
        return name;
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        return getInventoryProperties(world.getBlockMetadata(x, y, z));
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("facing", Facing.name(facingOf(meta)));
        return props;
    }

}
