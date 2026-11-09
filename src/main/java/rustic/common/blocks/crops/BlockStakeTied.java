package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.blocks.BlockBase;
import rustic.common.blocks.BlockRope;
import rustic.common.blocks.BlockRopeBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ISupportShape;
import rustic.common.blocks.ModBlocks;

/** A crop stake with rope tied to it. Has no item form; breaking it leaves the stake and drops the rope. */
public class BlockStakeTied extends BlockBase implements IModelBlock, ISupportShape {

    public BlockStakeTied() {
        super(Material.wood, "stake_tied", false);
        setStepSound(soundTypeWood);
        setHardness(1F);
        setResistance(5F);
        setCreativeTab(null);
        setBlockBounds(0.3125F, 0.0F, 0.3125F, 0.6875F, 1.0F, 0.6875F);
        GameRegistry.registerBlock(this, null, name);
        Blocks.fire.setFireInfo(this, 15, 40);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x + 0.375, y, z + 0.375, x + 0.625, y + 1, z + 0.625);
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
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (!world.isRemote) {
            world.setBlock(x, y, z, ModBlocks.CROP_STAKE, 0, 3);
            world.scheduleBlockUpdate(x, y, z, ModBlocks.CROP_STAKE, 1);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(ModBlocks.ROPE, 1));
        return stacks;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(ModBlocks.ROPE);
    }

    private boolean connects(IBlockAccess world, int x, int y, int z, int axis) {
        Block block = world.getBlock(x, y, z);
        if (block == this) return true;
        if (block instanceof BlockRope || block == ModBlocks.GRAPE_LEAVES) {
            return ((BlockRopeBase) block).getAxis(world, x, y, z) == axis;
        }
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.offsetY != 0;
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.offsetY != 0;
    }

    @Override
    public String getBlockStateName() {
        return name;
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        Map<String, String> props = new HashMap<>();
        props.put("north", String.valueOf(connects(world, x, y, z - 1, BlockRopeBase.AXIS_Z)));
        props.put("south", String.valueOf(connects(world, x, y, z + 1, BlockRopeBase.AXIS_Z)));
        props.put("west", String.valueOf(connects(world, x - 1, y, z, BlockRopeBase.AXIS_X)));
        props.put("east", String.valueOf(connects(world, x + 1, y, z, BlockRopeBase.AXIS_X)));
        return props;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        for (String dir : new String[] { "north", "south", "west", "east" }) props.put(dir, "false");
        return props;
    }

}
