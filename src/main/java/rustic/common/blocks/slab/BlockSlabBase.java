package rustic.common.blocks.slab;

import java.util.Collections;
import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.json.JsonModelRegistry;
import rustic.core.Rustic;

/** Single (half) slab. Uses vanilla's 1.7.10 slab logic: metadata bit 8 marks the top half. */
public class BlockSlabBase extends BlockSlab {

    protected final String name;
    protected BlockSlabBase single;
    protected BlockSlabBase doubleSlab;
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    protected BlockSlabBase(boolean isDouble, Material material, String name, SoundType sound) {
        super(isDouble, material);
        this.name = name;
        setBlockName(Rustic.MODID + "." + name);
        setHardness(2.0F);
        if (sound != null) setStepSound(sound);
        if (!isDouble) {
            this.useNeighborBrightness = true;
            setCreativeTab(Rustic.decorTab);
        }
    }

    public BlockSlabBase(Material material, String name, BlockDoubleSlabBase doubleSlab) {
        this(material, name, doubleSlab, null);
    }

    public BlockSlabBase(Material material, String name, BlockDoubleSlabBase doubleSlab, SoundType sound) {
        this(false, material, name, sound);
        this.single = this;
        this.doubleSlab = doubleSlab;
        doubleSlab.single = this;
        doubleSlab.doubleSlab = doubleSlab;
        GameRegistry.registerBlock(this, ItemBlockSlabBase.class, name, this, doubleSlab, false);
        GameRegistry.registerBlock(doubleSlab, ItemBlockSlabBase.class, doubleSlab.name, this, doubleSlab, true);
        if (material == Material.wood) {
            Blocks.fire.setFireInfo(this, 5, 20);
            Blocks.fire.setFireInfo(doubleSlab, 5, 20);
        }
    }

    @Override
    public String func_150002_b(int meta) {
        return single.getUnlocalizedName();
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(single);
    }

    @Override
    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack(single, 2, 0);
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(single);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = JsonModelRegistry
            .registerCubeIcons(doubleSlab.name, Collections.singletonMap("half", "bottom"), register);
        blockIcon = icons[2];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[side % 6];
    }

}
