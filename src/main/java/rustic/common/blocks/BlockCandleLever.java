package rustic.common.blocks;

import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Candle metadata in the low 3 bits, 8 = pulled (powered). */
public class BlockCandleLever extends BlockCandle {

    public BlockCandleLever() {
        this("candle_lever");
    }

    public BlockCandleLever(String name) {
        this(Material.circuits, name, true);
    }

    public BlockCandleLever(Material mat, String name, boolean register) {
        super(mat, name, register);
        this.setHardness(1F);
        this.setLightLevel(1.0F);
        this.setTickRandomly(false);
        setStepSound(soundTypeMetal);
    }

    public static boolean isPowered(int meta) {
        return (meta & 8) != 0;
    }

    @Override
    public int tickRate(World world) {
        return 80; // 4 seconds
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        if (isPowered(meta)) return true;
        world.setBlockMetadataWithNotify(x, y, z, meta | 8, 3);
        world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
        notifyNeighbors(world, x, y, z, facingOf(meta));
        world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
        return true;
    }

    private void notifyNeighbors(World world, int x, int y, int z, ForgeDirection facing) {
        world.notifyBlocksOfNeighborChange(x, y, z, this);
        ForgeDirection back = facing.getOpposite();
        world.notifyBlocksOfNeighborChange(x + back.offsetX, y + back.offsetY, z + back.offsetZ, this);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (isPowered(meta)) {
            notifyNeighbors(world, x, y, z, facingOf(meta));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return isPowered(world.getBlockMetadata(x, y, z)) ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (!isPowered(meta)) return 0;
        return facingOf(meta).ordinal() == side ? 15 : 0;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection facing = facingOf(meta);
        boolean pulled = isPowered(meta);
        double px = x + 0.5D;
        double py = y + 0.7D;
        double pz = z + 0.5D;
        if (facing.offsetY == 0) {
            ForgeDirection back = facing.getOpposite();
            double fx = px + 0.27D * back.offsetX * (pulled ? 0.0 : 1.0);
            double fy = py + 0.25D + (pulled ? -0.125 : 0.0);
            double fz = pz + 0.27D * back.offsetZ * (pulled ? 0.0 : 1.0);
            world.spawnParticle("smoke", fx, fy, fz, 0, 0, 0);
            world.spawnParticle("flame", fx, fy, fz, 0, 0, 0);
        } else {
            double fy = py + 0.33D + (pulled ? -0.125 : 0.0);
            double fz = pz + (pulled ? -0.35 : 0.0);
            world.spawnParticle("smoke", px, fy, fz, 0, 0, 0);
            world.spawnParticle("flame", px, fy, fz, 0, 0, 0);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            if (isPowered(meta)) {
                world.setBlockMetadataWithNotify(x, y, z, meta & 7, 3);
                notifyNeighbors(world, x, y, z, facingOf(meta));
                world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.5F);
                world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
            }
        }
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = super.getInventoryProperties(meta);
        props.put("powered", String.valueOf(isPowered(meta)));
        return props;
    }

}
