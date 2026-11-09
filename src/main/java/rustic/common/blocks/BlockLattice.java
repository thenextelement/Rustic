package rustic.common.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.blocks.crops.BlockGrapeLeaves;

/** Metadata 1 = covered in leaves. Connections are computed from neighbours. */
public class BlockLattice extends BlockBase implements IModelBlock, ISupportShape {

    private static final String[] DIR_NAMES = { "down", "up", "north", "south", "west", "east" };

    public BlockLattice(Material mat, String name) {
        this(mat, name, true);
    }

    public BlockLattice(Material mat, String name, boolean register) {
        super(mat, name, register);
        setHardness(2.0F);
        setStepSound(soundTypeMetal);
    }

    public boolean[] getConnections(IBlockAccess world, int x, int y, int z) {
        boolean[] out = new boolean[6];
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            out[dir.ordinal()] = canConnectTo(world, x, y, z, dir);
        }
        return out;
    }

    private boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection facing) {
        int ox = x + facing.offsetX, oy = y + facing.offsetY, oz = z + facing.offsetZ;
        Block block = world.getBlock(ox, oy, oz);
        if (block.isAir(world, ox, oy, oz)) return false;
        if (block instanceof BlockLattice) return true;
        if (block instanceof BlockGrapeLeaves) {
            return ((BlockGrapeLeaves) block).getAxis(world, ox, oy, oz) == BlockRopeBase.axisOf(facing);
        }
        if (block instanceof BlockRopeBase) {
            return ((BlockRopeBase) block).getAxis(world, ox, oy, oz) == BlockRopeBase.axisOf(facing);
        }
        if (block instanceof BlockLeaves || block.getMaterial() == Material.glass
            || block.getMaterial() == Material.ice) {
            return false; // like 1.12's isExceptBlockForAttachWithPiston
        }
        return ISupportShape.canSupport(world, ox, oy, oz, facing.getOpposite());
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity entity) {
        boolean[] c = getConnections(world, x, y, z);
        addBox(world, x, y, z, mask, list, entity, .375f, .375f, .375f, .625f, .625f, .625f);
        if (c[0]) addBox(world, x, y, z, mask, list, entity, .4375f, 0f, .4375f, .5625f, .4375f, .5625f);
        if (c[1]) addBox(world, x, y, z, mask, list, entity, .4375f, .5625f, .4375f, .5625f, 1f, .5625f);
        if (c[2]) addBox(world, x, y, z, mask, list, entity, .4375f, .4375f, 0f, .5625f, .5625f, .4375f);
        if (c[3]) addBox(world, x, y, z, mask, list, entity, .4375f, .4375f, .5625f, .5625f, .5625f, 1f);
        if (c[4]) addBox(world, x, y, z, mask, list, entity, 0f, .4375f, .4375f, .4375f, .5625f, .5625f);
        if (c[5]) addBox(world, x, y, z, mask, list, entity, .5625f, .4375f, .4375f, 1f, .5625f, .5625f);
        setBlockBoundsBasedOnState(world, x, y, z);
    }

    @SuppressWarnings({ "rawtypes" })
    private void addBox(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity, float x1,
        float y1, float z1, float x2, float y2, float z2) {
        setBlockBounds(x1, y1, z1, x2, y2, z2);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        boolean[] c = getConnections(world, x, y, z);
        setBlockBounds(
            c[4] ? 0 : 0.375F,
            c[0] ? 0 : 0.375F,
            c[2] ? 0 : 0.375F,
            c[5] ? 1 : 0.625F,
            c[1] ? 1 : 0.625F,
            c[3] ? 1 : 0.625F);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        if (held != null && Block.getBlockFromItem(held.getItem()) instanceof BlockLeaves
            && world.getBlockMetadata(x, y, z) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 1, 3);
            if (!player.capabilities.isCreativeMode) held.stackSize--;
            return true;
        }
        return false;
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
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
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return world.getBiomeGenForCoords(x, z)
            .getBiomeFoliageColor(x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    public String getBlockStateName() {
        return name;
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        Map<String, String> props = new HashMap<>();
        props.put("inventory", "false");
        props.put("leaves", String.valueOf(world.getBlockMetadata(x, y, z) == 1));
        boolean[] c = getConnections(world, x, y, z);
        for (int i = 0; i < 6; i++) props.put(DIR_NAMES[i], String.valueOf(c[i]));
        return props;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("inventory", "true");
        return props;
    }

}
