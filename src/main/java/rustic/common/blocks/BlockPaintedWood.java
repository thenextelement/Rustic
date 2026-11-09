package rustic.common.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.core.Rustic;

/**
 * Painted planks: one block, metadata 0-15 = colour in wool / dye order (0 white ... 15 black). 1.12 had a separate
 * block per colour; the per-colour textures and "tile.rustic.painted_wood_&lt;colour&gt;" names are kept.
 */
public class BlockPaintedWood extends BlockBase implements ItemBlockMeta.INamedVariants {

    public static final String[] COLORS = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black" };

    /** Ore dictionary dye names, by metadata. */
    public static final String[] DYES = { "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue", "dyeYellow", "dyeLime",
        "dyePink", "dyeGray", "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue", "dyeBrown", "dyeGreen", "dyeRed",
        "dyeBlack" };

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockPaintedWood() {
        super(Material.wood, "painted_wood", false);
        setHardness(2F);
        setStepSound(soundTypeWood);
        register(ItemBlockMeta.class);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public String[] getVariantNames() {
        return COLORS;
    }

    @Override
    public int damageDropped(int meta) {
        return meta & 15;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int meta = 0; meta < COLORS.length; meta++) list.add(new ItemStack(item, 1, meta));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[COLORS.length];
        for (int meta = 0; meta < COLORS.length; meta++) {
            icons[meta] = register.registerIcon(Rustic.MODID + ":painted_wood_" + COLORS[meta]);
        }
        blockIcon = icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[meta & 15];
    }

}
