package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.blocks.BlockLattice;
import rustic.common.blocks.BlockRope;
import rustic.common.blocks.BlockRopeBase;
import rustic.common.blocks.ModBlocks;
import rustic.common.items.ModItems;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

/**
 * Grape vines growing along a horizontal rope. Metadata: 1 = along z (else x), 2 = distance 1 (a spread vine), 8 =
 * has grapes.
 */
public class BlockGrapeLeaves extends BlockRopeBase implements IGrowable {

    public BlockGrapeLeaves() {
        super(Material.leaves, "grape_leaves", false);
        GameRegistry.registerBlock(this, null, name);
        this.setTickRandomly(true);
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeGrass);
        Blocks.fire.setFireInfo(this, 30, 60);
    }

    @Override
    public int getAxis(int meta) {
        return (meta & 1) != 0 ? AXIS_Z : AXIS_X;
    }

    @Override
    public int metaForAxis(int axis) {
        return axis == AXIS_Z ? 1 : 0;
    }

    public static int getDist(int meta) {
        return (meta & 2) != 0 ? 1 : 0;
    }

    public static boolean hasGrapes(int meta) {
        return (meta & 8) != 0;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        if (hasGrapes(meta)) stacks.add(new ItemStack(ModItems.GRAPES));
        return stacks;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (!world.isRemote) {
            world.setBlock(x, y, z, ModBlocks.ROPE, ModBlocks.ROPE.metaForAxis(getAxis(meta)), 3);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        if (hasGrapes(meta)) {
            world.setBlockMetadataWithNotify(x, y, z, meta & ~8, 3);
            if (!world.isRemote) {
                ItemStack stack = new ItemStack(ModItems.GRAPES, world.rand.nextInt(2) + 1);
                if (!RusticUtils.addToInventory(player, stack)) {
                    ForgeDirection dir = ForgeDirection.getOrientation(side);
                    world.spawnEntityInWorld(
                        new EntityItem(
                            world,
                            x + 0.5 + dir.offsetX,
                            y + 0.5 + dir.offsetY,
                            z + 0.5 + dir.offsetZ,
                            stack));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getDist(meta) < 1) {
            setBlockBounds(0, 0, 0, 1, 1, 1);
        } else if (getAxis(meta) == AXIS_Z) {
            setBlockBounds(0.1875F, 0.1875F, 0.0F, 0.8125F, 0.8125F, 1.0F);
        } else {
            setBlockBounds(0.0F, 0.1875F, 0.1875F, 1.0F, 0.8125F, 0.8125F);
        }
    }

    @Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return false;
    }

    @Override
    public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return ModItems.GRAPES;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!world.isRemote && !isBlockSupported(world, x, y, z)) {
            dropBlock(world, x, y, z);
        }
    }

    @Override
    public boolean isSideSupported(World world, int x, int y, int z, ForgeDirection facing) {
        int ox = x + facing.offsetX, oy = y + facing.offsetY, oz = z + facing.offsetZ;
        Block other = world.getBlock(ox, oy, oz);
        int axis = getAxis(world, x, y, z);
        boolean isSame = other == this && getAxis(world, ox, oy, oz) == axis;
        boolean isRope = other == ModBlocks.ROPE && ModBlocks.ROPE.getAxis(world, ox, oy, oz) == axis;
        boolean isSideSolid = world.isSideSolid(ox, oy, oz, facing.getOpposite(), false);
        boolean isTiedStake = other == ModBlocks.STAKE_TIED;
        boolean isLattice = other instanceof BlockLattice;
        return isSame || isRope || isSideSolid || isTiedStake || isLattice;
    }

    @Override
    public boolean isBlockSupported(World world, int x, int y, int z) {
        if (getAxis(world, x, y, z) == AXIS_X) {
            return isSideSupported(world, x, y, z, ForgeDirection.WEST)
                && isSideSupported(world, x, y, z, ForgeDirection.EAST);
        }
        return isSideSupported(world, x, y, z, ForgeDirection.NORTH)
            && isSideSupported(world, x, y, z, ForgeDirection.SOUTH);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        if (!isBlockSupported(world, x, y, z)) {
            dropBlock(world, x, y, z);
            return;
        }
        if (world.getBlockLightValue(x, y + 1, z) >= 9) {
            int meta = world.getBlockMetadata(x, y, z);
            int dist = getDist(meta);
            if (dist > 0 && !hasGrapes(meta) && world.isAirBlock(x, y - 1, z)) {
                if (rand.nextInt(36) == 0) {
                    world.setBlockMetadataWithNotify(x, y, z, meta | 8, 3);
                }
            } else if (dist < 1 && canSpread(world, x, y, z)) {
                if (rand.nextInt(31) == 0) {
                    spread(world, x, y, z);
                }
            }
        }
    }

    // IGrowable: canGrow
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getDist(meta) > 0) return !hasGrapes(meta) && world.isAirBlock(x, y - 1, z);
        return canSpread(world, x, y, z);
    }

    // IGrowable: canUseBonemeal
    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getDist(meta) > 0) {
            world.setBlockMetadataWithNotify(x, y, z, meta | 8, 3);
        } else {
            spread(world, x, y, z);
        }
    }

    private boolean isRopeAlong(World world, int x, int y, int z, int axis) {
        return world.getBlock(x, y, z) == ModBlocks.ROPE && ModBlocks.ROPE.getAxis(world, x, y, z) == axis;
    }

    public boolean canSpread(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getDist(meta) != 0) return false;
        int axis = getAxis(meta);
        if (axis == AXIS_X) return isRopeAlong(world, x - 1, y, z, axis) || isRopeAlong(world, x + 1, y, z, axis);
        return isRopeAlong(world, x, y, z - 1, axis) || isRopeAlong(world, x, y, z + 1, axis);
    }

    public void spread(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getDist(meta) >= 1) return;
        int axis = getAxis(meta);
        int dx = axis == AXIS_X ? 1 : 0, dz = axis == AXIS_Z ? 1 : 0;
        boolean neg = isRopeAlong(world, x - dx, y, z - dz, axis);
        boolean pos = isRopeAlong(world, x + dx, y, z + dz, axis);
        int sign;
        if (neg && pos) sign = world.rand.nextFloat() < 0.5 ? -1 : 1;
        else if (neg) sign = -1;
        else if (pos) sign = 1;
        else return;
        int nx = x + sign * dx, nz = z + sign * dz;
        int ropeAxis = ModBlocks.ROPE.getAxis(world, nx, y, nz);
        world.setBlock(nx, y, nz, this, metaForAxis(ropeAxis) | 2, 3);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean isDangling(IBlockAccess world, int x, int y, int z) {
        Block below = world.getBlock(x, y - 1, z);
        return below instanceof BlockRope && ((BlockRope) below).getAxis(world, x, y - 1, z) == AXIS_Y;
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
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        Map<String, String> props = getInventoryProperties(world.getBlockMetadata(x, y, z));
        props.put("dangle", String.valueOf(isDangling(world, x, y, z)));
        return props;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("axis", AXIS_NAMES[getAxis(meta)]);
        props.put("distance", String.valueOf(getDist(meta)));
        props.put("grapes", String.valueOf(hasGrapes(meta)));
        props.put("dangle", "false");
        return props;
    }

}
