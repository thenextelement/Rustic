package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.util.Facing;

/** Metadata = facing (ForgeDirection ordinal = 1.12 EnumFacing index): the side it was placed against. */
public class BlockLantern extends BlockBase implements IModelBlock, ISupportShape {

    public BlockLantern(Material mat, String name) {
        this(mat, name, true);
    }

    public BlockLantern(Material mat, String name, boolean register) {
        super(mat, name, register);
        setLightLevel(1.0F);
        setHardness(2.0F);
        setStepSound(soundTypeMetal);
    }

    public static ForgeDirection getFacing(int meta) {
        ForgeDirection dir = ForgeDirection.getOrientation(meta);
        return dir == ForgeDirection.UNKNOWN ? ForgeDirection.UP : dir;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        if (getFacing(world.getBlockMetadata(x, y, z)) == ForgeDirection.DOWN) {
            setBlockBounds(0.25F, 0.375F, 0.25F, 0.75F, 1F, 0.75F);
        } else {
            setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.625F, 0.75F);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.625F, 0.75F);
    }

    @Override
    public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
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
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        ForgeDirection facing = getFacing(world.getBlockMetadata(x, y, z));
        double d0 = x + 0.5D;
        double d1 = y + 0.33D;
        double d2 = z + 0.5D;
        if (facing != ForgeDirection.DOWN) {
            world.spawnParticle("flame", d0, d1, d2, 0.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle("flame", d0, d1 + 0.375D, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        ForgeDirection mountFace = getFacing(world.getBlockMetadata(x, y, z)).getOpposite();
        if (mountFace == side) return true;
        return side == ForgeDirection.DOWN && mountFace != ForgeDirection.UP;
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
        props.put("facing", Facing.name(getFacing(meta)));
        return props;
    }

}
