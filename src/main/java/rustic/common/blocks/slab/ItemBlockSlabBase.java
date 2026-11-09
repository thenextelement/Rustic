package rustic.common.blocks.slab;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

/**
 * {@link ItemSlab} with a constructor whose parameter types match what {@code GameRegistry.registerBlock} passes
 * reflectively (it looks constructors up by the exact runtime classes of the extra arguments).
 */
public class ItemBlockSlabBase extends ItemSlab {

    public ItemBlockSlabBase(Block block, BlockSlabBase single, BlockDoubleSlabBase doubleSlab, Boolean isDouble) {
        super(block, single, doubleSlab, isDouble);
    }

}
