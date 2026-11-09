package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.json.JsonBlockState;
import rustic.client.json.JsonModelRegistry;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

/** Oak-like leaves that grow apples. Metadata: low 2 bits = apple age (0..3), 4 = not decayable, 8 = check decay. */
public class BlockLeavesApple extends BlockLeaves implements IGrowable, IModelBlock {

    public BlockLeavesApple() {
        super();
        setBlockName(Rustic.MODID + ".leaves_apple");
        setCreativeTab(Rustic.farmingTab);
        GameRegistry.registerBlock(this, rustic.common.blocks.ItemBlockLeaves.class, "leaves_apple");
        Blocks.fire.setFireInfo(this, 30, 60);
    }

    public int getMaxAge() {
        return 3;
    }

    public static int getAge(int meta) {
        return meta & 3;
    }

    @Override
    public String[] func_150125_e() {
        return new String[] { "apple" };
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        if (world.getBlock(x, y, z) != this) return;
        int meta = world.getBlockMetadata(x, y, z);
        int age = getAge(meta);
        if (age < getMaxAge() && isAirAdjacent(world, x, y, z) && rand.nextInt(51) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, (meta & ~3) | (age + 1), 2);
        }
    }

    protected static boolean isAirAdjacent(World world, int x, int y, int z) {
        return world.isAirBlock(x, y - 1, z) || world.isAirBlock(x, y, z - 1)
            || world.isAirBlock(x, y, z + 1)
            || world.isAirBlock(x - 1, y, z)
            || world.isAirBlock(x + 1, y, z);
    }

    // IGrowable: canGrow
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        return getAge(world.getBlockMetadata(x, y, z)) < getMaxAge() && isAirAdjacent(world, x, y, z);
    }

    // IGrowable: canUseBonemeal
    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return func_149851_a(world, x, y, z, world.isRemote);
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int age = Math.min(getAge(meta) + MathHelper.getRandomIntegerInRange(world.rand, 2, 5), getMaxAge());
        world.setBlockMetadataWithNotify(x, y, z, (meta & ~3) | age, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        if (getAge(meta) >= getMaxAge()) {
            world.setBlockMetadataWithNotify(x, y, z, meta & ~3, 3);
            if (!world.isRemote) {
                ItemStack stack = new ItemStack(Items.apple);
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
    protected void func_150124_c(World world, int x, int y, int z, int meta, int chance) {
        // apples are dropped in getDrops
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.APPLE_SAPLING);
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<>();
        int chance = 20;
        if (fortune > 0) {
            chance -= 2 << fortune;
            if (chance < 10) chance = 10;
        }
        if (world.rand.nextInt(chance) == 0) {
            ret.add(new ItemStack(getItemDropped(meta, world.rand, fortune), 1, 0));
        }
        if (getAge(meta) >= getMaxAge()) {
            ret.add(new ItemStack(Items.apple));
        }
        return ret;
    }

    @Override
    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<>();
        ret.add(new ItemStack(Blocks.leaves, 1, 0));
        return ret;
    }

    @Override
    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack(Blocks.leaves, 1, 0);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return !world.getBlock(x, y, z)
            .isOpaqueCube();
    }

    @Override
    public int getRenderType() {
        return ModBlocks.MODEL_RENDER_ID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        IIcon icon = JsonModelRegistry.registerBlock(this, register);
        field_150129_M[0] = new IIcon[] { icon };
        field_150129_M[1] = new IIcon[] { icon };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return field_150129_M[0][0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return world.getBiomeGenForCoords(x, z)
            .getBiomeFoliageColor(x, y, z);
    }

    @Override
    public String getBlockStateName() {
        return "leaves_apple";
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        Map<String, String> props = getInventoryProperties(world.getBlockMetadata(x, y, z));
        props.put(JsonBlockState.RANDOM_KEY, String.valueOf(((x * 31 + y) * 31 + z) & 7));
        return props;
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        Map<String, String> props = new HashMap<>();
        props.put("apple_age", String.valueOf(getAge(meta)));
        return props;
    }

}
