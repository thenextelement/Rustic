package rustic.common.blocks.crops;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.blocks.BlockBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;
import rustic.core.Rustic;

/** Metadata = age (0..1); grows into an apple sapling. */
public class BlockAppleSeeds extends BlockBase implements IGrowable, IPlantable, IModelBlock {

    public BlockAppleSeeds() {
        super(Material.plants, "apple_seeds");
        setStepSound(soundTypeGrass);
        setHardness(0F);
        setTickRandomly(true);
        setCreativeTab(Rustic.farmingTab);
    }

    public int getMaxAge() {
        return 1;
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
        return EnumPlantType.Plains;
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
        setBlockBounds(0, 0, 0, 1, world.getBlockMetadata(x, y, z) >= 1 ? 0.5F : 0.125F, 1);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        if (world.getBlockLightValue(x, y + 1, z) >= 9 && rand.nextInt((int) (25.0F / 7F) + 1) == 0) {
            func_149853_b(world, rand, x, y, z);
        }
    }

    // IGrowable: canGrow
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        return true;
    }

    // IGrowable: canUseBonemeal
    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) < getMaxAge()) {
            world.setBlockMetadataWithNotify(x, y, z, getMaxAge(), 2);
        } else {
            world.setBlock(x, y, z, ModBlocks.APPLE_SAPLING, 0, 3);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.getBlock(x, y - 1, z)
            .canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return world.getBlock(x, y - 1, z)
            .canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canBlockStay(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
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
        return Collections.singletonMap("age", String.valueOf(Math.min(meta, 1)));
    }

}
