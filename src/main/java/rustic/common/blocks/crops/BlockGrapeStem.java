package rustic.common.blocks.crops;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.blocks.BlockBase;
import rustic.common.blocks.BlockRopeBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;
import rustic.core.Rustic;

/** Metadata = age (0..3). Once grown it turns a horizontal rope above it into grape leaves. */
public class BlockGrapeStem extends BlockBase implements IGrowable, IPlantable, IModelBlock {

    private static final float[] HEIGHTS = { 0.25F, 0.5F, 1.0F, 1.0F };

    public BlockGrapeStem() {
        super(Material.plants, "grape_stem");
        setStepSound(soundTypeGrass);
        setHardness(0.5F);
        setCreativeTab(Rustic.farmingTab);
        this.setTickRandomly(true);
        Blocks.fire.setFireInfo(this, 20, 100);
    }

    public int getMaxAge() {
        return 3;
    }

    private int getAge(int meta) {
        return Math.min(meta & 3, getMaxAge());
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
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        return EnumPlantType.Crop;
    }

    @Override
    public Block getPlant(IBlockAccess world, int x, int y, int z) {
        return this;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, HEIGHTS[getAge(world.getBlockMetadata(x, y, z))], 0.625F);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    private boolean hasHorizontalRopeAbove(World world, int x, int y, int z) {
        return world.getBlock(x, y + 1, z) == ModBlocks.ROPE
            && ModBlocks.ROPE.getAxis(world, x, y + 1, z) != BlockRopeBase.AXIS_Y;
    }

    private void growLeaves(World world, int x, int y, int z) {
        int axis = ModBlocks.ROPE.getAxis(world, x, y + 1, z);
        world.setBlock(x, y + 1, z, ModBlocks.GRAPE_LEAVES, ModBlocks.GRAPE_LEAVES.metaForAxis(axis), 3);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        if (world.getBlockLightValue(x, y + 1, z) >= 9) {
            int age = getAge(world.getBlockMetadata(x, y, z));
            if (age < getMaxAge()) {
                if (rand.nextInt((int) (25.0F / 7F) + 1) == 0) {
                    world.setBlockMetadataWithNotify(x, y, z, age + 1, 2);
                }
            } else if (hasHorizontalRopeAbove(world, x, y, z)) {
                if (rand.nextInt((int) (25.0F / 7F) + 1) == 0) {
                    growLeaves(world, x, y, z);
                }
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.getBlock(x, y - 1, z)
            .canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
    }

    // IGrowable: canGrow
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        int age = getAge(world.getBlockMetadata(x, y, z));
        return age < getMaxAge() || hasHorizontalRopeAbove(world, x, y, z);
    }

    // IGrowable: canUseBonemeal
    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        int age = getAge(world.getBlockMetadata(x, y, z));
        if (age < getMaxAge()) {
            age = Math.min(age + MathHelper.getRandomIntegerInRange(world.rand, 2, 5), getMaxAge());
            world.setBlockMetadataWithNotify(x, y, z, age, 2);
        } else if (hasHorizontalRopeAbove(world, x, y, z)) {
            growLeaves(world, x, y, z);
        }
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
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
        return Collections.singletonMap("age", String.valueOf(getAge(meta)));
    }

}
