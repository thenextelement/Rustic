package rustic.common.blocks.crops;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.json.JsonModelRegistry;
import rustic.common.world.WorldGenAppleTree;
import rustic.core.Rustic;

/** Metadata 1 = second growth stage. */
public class BlockSaplingApple extends BlockBush implements IGrowable {

    public BlockSaplingApple() {
        super();
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        setBlockName(Rustic.MODID + ".sapling_apple");
        GameRegistry.registerBlock(this, "sapling_apple");
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeGrass);
        setHardness(0.0F);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            super.updateTick(world, x, y, z, rand);
            if (world.getBlockLightValue(x, y + 1, z) >= 9 && rand.nextInt(7) == 0) {
                func_149853_b(world, rand, x, y, z);
            }
        }
    }

    public void generateTree(World world, int x, int y, int z, Random rand) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, rand, x, y, z)) return;
        int meta = world.getBlockMetadata(x, y, z);
        WorldGenerator generator = new WorldGenAppleTree(true);
        world.setBlock(x, y, z, Blocks.air, 0, 4);
        if (!generator.generate(world, rand, x, y, z)) {
            world.setBlock(x, y, z, this, meta, 4);
        }
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
        return world.rand.nextFloat() < 0.45D;
    }

    // IGrowable: grow
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 1, 4);
        } else {
            generateTree(world, x, y, z, rand);
        }
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        return EnumPlantType.Plains;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = JsonModelRegistry
            .registerStateTexture("sapling_apple", java.util.Collections.singletonMap("stage", "0"), "cross", register);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return blockIcon;
    }

}
