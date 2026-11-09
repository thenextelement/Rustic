package rustic.common.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.world.WorldGenIronwoodTree;
import rustic.common.world.WorldGenOliveTree;
import rustic.core.Rustic;

/** Metadata: low 3 bits = wood type, 8 = growth stage. */
public class BlockSaplingRustic extends BlockBush implements IGrowable, ItemBlockMeta.INamedVariants {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockSaplingRustic() {
        super();
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        setBlockName(Rustic.MODID + ".sapling");
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeGrass);
        GameRegistry.registerBlock(this, ItemBlockMeta.class, "sapling");
    }

    @Override
    public String[] getVariantNames() {
        return BlockPlanksRustic.EnumType.NAMES;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            super.updateTick(world, x, y, z, rand);
            if (world.getBlockLightValue(x, y + 1, z) >= 9 && rand.nextInt(7) == 0) {
                grow(world, rand, x, y, z);
            }
        }
    }

    public void grow(World world, Random rand, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & 8) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, meta | 8, 4);
        } else {
            generateTree(world, x, y, z, rand);
        }
    }

    public void generateTree(World world, int x, int y, int z, Random rand) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, rand, x, y, z)) return;
        int meta = world.getBlockMetadata(x, y, z);
        WorldGenerator generator;
        switch (BlockPlanksRustic.EnumType.byMetadata(meta & 7)) {
            case IRONWOOD:
                generator = new WorldGenIronwoodTree(true);
                break;
            case OLIVE:
            default:
                generator = new WorldGenOliveTree(true);
                break;
        }
        world.setBlock(x, y, z, Blocks.air, 0, 4);
        if (!generator.generate(world, rand, x, y, z)) {
            world.setBlock(x, y, z, this, meta, 4);
        }
    }

    @Override
    public int damageDropped(int meta) {
        return BlockPlanksRustic.EnumType.byMetadata(meta & 7)
            .getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (BlockPlanksRustic.EnumType type : BlockPlanksRustic.EnumType.values()) {
            list.add(new ItemStack(item, 1, type.getMetadata()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[BlockPlanksRustic.EnumType.values().length];
        for (BlockPlanksRustic.EnumType type : BlockPlanksRustic.EnumType.values()) {
            icons[type.getMetadata()] = register.registerIcon(Rustic.MODID + ":sapling_" + type.getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[BlockPlanksRustic.EnumType.byMetadata(meta & 7)
            .getMetadata()];
    }

    // IGrowable: canGrow
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        return true;
    }

    // IGrowable: canUseBonemeal
    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return world.rand.nextFloat() < 0.45D;
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        grow(world, rand, x, y, z);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        return EnumPlantType.Plains;
    }

}
