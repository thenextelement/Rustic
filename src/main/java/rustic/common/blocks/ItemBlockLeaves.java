package rustic.common.blocks;

import net.minecraft.block.Block;

/** Like vanilla's ItemLeaves: leaves placed by players get the "no decay" bit (4). */
public class ItemBlockLeaves extends ItemBlockMeta {

    public ItemBlockLeaves(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage | 4;
    }

    @Override
    public String getUnlocalizedName(net.minecraft.item.ItemStack stack) {
        if (field_150939_a instanceof INamedVariants) return super.getUnlocalizedName(stack);
        return field_150939_a.getUnlocalizedName();
    }

}
