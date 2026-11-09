package rustic.common.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.core.Rustic;

public class BlockFluidRustic extends BlockFluidClassic {

    public BlockFluidRustic(String name, Fluid fluid, Material material) {
        super(fluid, material);
        setBlockName(Rustic.MODID + "." + name);
        GameRegistry.registerBlock(this, name);
        fluid.setBlock(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        if (getFluid() instanceof FluidRustic) {
            ((FluidRustic) getFluid()).registerIcons(register);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        Fluid fluid = getFluid();
        return (side == 0 || side == 1) ? fluid.getStillIcon() : fluid.getFlowingIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        Block neighbor = world.getBlock(x, y, z);
        return neighbor != this && super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block.getMaterial()
            .isLiquid()) return false;
        return super.canDisplace(world, x, y, z);
    }

    @Override
    public boolean displaceIfPossible(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == this) return false;
        if (block instanceof BlockFluidBase && world.getBlockMetadata(x, y, z) == 0) return false;
        if (block.getMaterial()
            .isLiquid()) return false;
        if (block == Blocks.snow_layer) {
            world.setBlockToAir(x, y, z);
            return true;
        }
        return super.displaceIfPossible(world, x, y, z);
    }

}
