package rustic.common.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.tileentity.TileEntityCondenserBase;
import rustic.common.util.Facing;
import rustic.core.Rustic;

/**
 * Two-block-tall condenser. Metadata: 4 = bottom half, low 2 bits = 5 - facing ordinal (same as 1.12).
 */
public abstract class BlockCondenserBase extends BlockTileBase implements IModelBlock, ISupportShape {

    public BlockCondenserBase(String name) {
        super(Material.rock, name);
        setHardness(2F);
        setCreativeTab(Rustic.alchemyTab);
        setStepSound(soundTypeStone);
    }

    public static boolean isBottom(int meta) {
        return (meta & 4) != 0;
    }

    public static ForgeDirection getFacing(int meta) {
        return BlockRetort.getFacing(meta);
    }

    protected abstract int getGuiId();

    public abstract Block getRetort();

    /** Whether a third retort is required behind the condenser. */
    protected abstract boolean needsBackRetort();

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        if (isBottom(world.getBlockMetadata(x, y, z))) setBlockBounds(0, 0, 0, 1, 1, 1);
        else setBlockBounds(0, 0, 0, 1, 0.5F, 1);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return isBottom(world.getBlockMetadata(x, y, z)) ? 255 : 0;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isBottom(world.getBlockMetadata(x, y, z));
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isBottom(world.getBlockMetadata(x, y, z)) || side == ForgeDirection.UP;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.isAirBlock(x, y + 1, z);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return 4;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facingMeta = BlockRetort.metaForFacing(
            Facing.horizontalFacing(placer)
                .getOpposite());
        world.setBlockMetadataWithNotify(x, y, z, 4 | facingMeta, 2);
        world.setBlock(x, y + 1, z, this, facingMeta, 3);
        super.onBlockPlacedBy(world, x, y, z, placer, stack);
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        boolean bottom = isBottom(meta);
        int otherY = bottom ? y + 1 : y - 1;
        if (world.getBlock(x, otherY, z) == this) {
            if (!world.isRemote && !player.capabilities.isCreativeMode) {
                world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(this, 1, 0)));
            }
            world.setBlockToAir(x, otherY, z);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public int getMobilityFlag() {
        return 2; // can't be pushed
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        int by = isBottom(world.getBlockMetadata(x, y, z)) ? y : y - 1;
        TileEntity tile = world.getTileEntity(x, by, z);
        if (tile instanceof TileEntityCondenserBase && hasRetorts(world, x, by, z)) {
            if (!((TileEntityCondenserBase) tile).activate(world, x, by, z, player, side, hitX, hitY, hitZ)) {
                if (!world.isRemote) player.openGui(Rustic.instance, getGuiId(), world, x, by, z);
            }
            return true;
        }
        return false;
    }

    private boolean isRetortFacing(World world, int x, int y, int z, ForgeDirection facing) {
        return world.getBlock(x, y, z) == getRetort()
            && BlockRetort.getFacing(world.getBlockMetadata(x, y, z)) == facing;
    }

    /** Checks for the retorts at the sides (and the back, for the advanced condenser), each facing away. */
    public boolean hasRetorts(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != this) return false;
        ForgeDirection facing = getFacing(world.getBlockMetadata(x, y, z));
        ForgeDirection left = Facing.rotateYCCW(facing), right = Facing.rotateY(facing), back = facing.getOpposite();
        if (!isRetortFacing(world, x + left.offsetX, y, z + left.offsetZ, left)) return false;
        if (!isRetortFacing(world, x + right.offsetX, y, z + right.offsetZ, right)) return false;
        if (needsBackRetort() && !isRetortFacing(world, x + back.offsetX, y, z + back.offsetZ, back)) return false;
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getBlockStateName() {
        return name;
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        return getRenderPropertiesForMeta(world.getBlockMetadata(x, y, z));
    }

    /** Placed-block properties; meta 0..3 is a top half (meta 0 = top facing east). */
    public Map<String, String> getRenderPropertiesForMeta(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("facing", Facing.name(getFacing(meta)));
        props.put("bottom", String.valueOf(isBottom(meta)));
        return props;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("facing", Facing.name(getFacing(meta)));
        props.put("bottom", "true");
        return props;
    }

}
