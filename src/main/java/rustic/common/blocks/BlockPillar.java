package rustic.common.blocks;

import java.util.Collections;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.json.JsonModelRegistry;
import rustic.core.Rustic;

/** Uses vanilla's rotated pillar renderer (like logs). Metadata: 0 = y, 4 = x, 8 = z. */
public class BlockPillar extends BlockRotatedPillar {

    private final String name;
    @SideOnly(Side.CLIENT)
    private IIcon side, top;

    public BlockPillar(String type) {
        super(Material.rock);
        this.name = type + "_pillar";
        setBlockName(Rustic.MODID + "." + name);
        setHardness(2.0F);
        setCreativeTab(Rustic.decorTab);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        IIcon[] icons = JsonModelRegistry.registerCubeIcons(name, Collections.singletonMap("axis", "y"), register);
        side = icons[2];
        top = icons[1];
        blockIcon = side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int meta) {
        return side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getTopIcon(int meta) {
        return top;
    }

}
