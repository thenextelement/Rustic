package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.Config;
import rustic.common.blocks.BlockBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

/** Metadata = age (0..5). */
public abstract class BlockBerryBush extends BlockBase implements IPlantable, IGrowable, IModelBlock {

    public BlockBerryBush(String name) {
        super(Material.plants, name);
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeGrass);
        setHardness(0.2F);
        setLightOpacity(1);
        setTickRandomly(true);
        setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1F, 0.875F);
        Blocks.fire.setFireInfo(this, 40, 80);
    }

    public abstract Item getBerries();

    public int getMaxAge() {
        return 5;
    }

    protected int getAge(int meta) {
        return Math.min(meta, getMaxAge());
    }

    public boolean isMaxAge(int meta) {
        return getAge(meta) >= getMaxAge();
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        if (!checkAndDropBlock(world, x, y, z)) return;
        if (world.getBlockLightValue(x, y + 1, z) >= 9) {
            int age = getAge(world.getBlockMetadata(x, y, z));
            if (age < getMaxAge() && rand.nextInt((int) (25.0F / 6.0F) + 1) == 0) {
                world.setBlockMetadataWithNotify(x, y, z, age + 1, 3);
            }
        }
    }

    /** A bush stops spreading once this many of the 8 blocks around it are bushes. */
    public static final int MAX_NEIGHBOUR_BUSHES = 2;

    protected void growOutward(World world, int x, int y, int z, Random rand) {
        List<int[]> positions = new ArrayList<>();
        int bushes = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                if (world.getBlock(x + i, y, z + j) == this) bushes++;
                else if (canPlaceBlockAt(world, x + i, y, z + j)) positions.add(new int[] { x + i, z + j });
            }
        }
        if (bushes >= MAX_NEIGHBOUR_BUSHES) return;
        if (!positions.isEmpty()) {
            int[] p = positions.get(rand.nextInt(positions.size()));
            world.setBlock(p[0], y, p[1], this, 0, 3);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(this, 1, 0));
        if (isMaxAge(meta) && getBerries() != null) {
            stacks.add(new ItemStack(getBerries()));
        }
        return stacks;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        boolean isBonemeal = held != null && held.getItem() == Items.dye && held.getItemDamage() == 15;
        if (isMaxAge(world.getBlockMetadata(x, y, z)) && !isBonemeal) {
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
            if (!world.isRemote) {
                ItemStack stack = new ItemStack(getBerries());
                if (!RusticUtils.addToInventory(player, stack)) {
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, stack));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        entity.motionX *= 0.6F;
        entity.motionZ *= 0.6F;
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
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        Block soil = world.getBlock(x, y - 1, z);
        if (soil.getMaterial()
            .isLiquid()) return false;
        return super.canPlaceBlockAt(world, x, y, z)
            && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
    }

    protected boolean canSustainBush(Block block) {
        return block == Blocks.grass || block == Blocks.dirt
            || block == Blocks.farmland
            || block == ModBlocks.FERTILE_SOIL;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        checkAndDropBlock(world, x, y, z);
    }

    protected boolean checkAndDropBlock(World world, int x, int y, int z) {
        if (!canBlockStay(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
            return false;
        }
        return true;
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        Block soil = world.getBlock(x, y - 1, z);
        if (world.getBlock(x, y, z) == this) {
            return soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
        }
        return canSustainBush(soil);
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
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
        int age = getAge(world.getBlockMetadata(x, y, z));
        if (age >= getMaxAge()) {
            growOutward(world, x, y, z, rand);
        } else {
            age = Math.min(age + MathHelper.getRandomIntegerInRange(world.rand, 2, 5), getMaxAge());
            world.setBlockMetadataWithNotify(x, y, z, age, 3);
        }
    }

    @Override
    public boolean hasRandomOffset() {
        return Config.OFFSET_WILDBERRY_BUSHES;
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
        return getInventoryProperties(world.getBlockMetadata(x, y, z));
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        return Collections.singletonMap("age", String.valueOf(getAge(meta)));
    }

}
