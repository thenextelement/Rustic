package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/** ItemBlock for blocks whose item damage selects a named variant: "tile.rustic.planks" + "_olive". */
public class ItemBlockMeta extends ItemBlock {

    public interface INamedVariants {

        String[] getVariantNames();
    }

    public ItemBlockMeta(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return field_150939_a.getIcon(2, damage);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (!(field_150939_a instanceof INamedVariants)) return super.getUnlocalizedName(stack);
        String[] names = ((INamedVariants) field_150939_a).getVariantNames();
        int meta = stack.getItemDamage();
        if (meta < 0 || meta >= names.length) meta = 0;
        return super.getUnlocalizedName() + "_" + names[meta];
    }

}
