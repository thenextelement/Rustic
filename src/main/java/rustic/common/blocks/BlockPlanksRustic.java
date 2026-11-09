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

public class BlockPlanksRustic extends BlockBase implements ItemBlockMeta.INamedVariants {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockPlanksRustic() {
        super(Material.wood, "planks", false);
        setHardness(2F);
        setResistance(5F);
        setStepSound(soundTypeWood);
        register(ItemBlockMeta.class);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public String[] getVariantNames() {
        return EnumType.NAMES;
    }

    @Override
    public int damageDropped(int meta) {
        return EnumType.byMetadata(meta)
            .getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (EnumType type : EnumType.values()) {
            list.add(new ItemStack(item, 1, type.getMetadata()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[EnumType.values().length];
        for (EnumType type : EnumType.values()) {
            icons[type.getMetadata()] = register.registerIcon(Rustic.MODID + ":planks_" + type.getName());
        }
        blockIcon = icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[EnumType.byMetadata(meta)
            .getMetadata()];
    }

    public static enum EnumType {

        OLIVE(0, "olive"),
        IRONWOOD(1, "ironwood");

        public static final String[] NAMES = { "olive", "ironwood" };
        private final int meta;
        private final String name;

        private EnumType(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public static EnumType byMetadata(int meta) {
            meta &= 7;
            if (meta < 0 || meta >= values().length) meta = 0;
            return values()[meta];
        }
    }

}
