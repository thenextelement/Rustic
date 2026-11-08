package rustic.common.blocks.slab;

import net.minecraft.block.material.Material;

/** Full (double) slab. Registered together with its half slab by {@link BlockSlabBase}. */
public class BlockDoubleSlabBase extends BlockSlabBase {

	public BlockDoubleSlabBase(Material material, String name) {
		this(material, name, null);
	}

	public BlockDoubleSlabBase(Material material, String name, SoundType sound) {
		super(true, material, name, sound);
	}

	@Override
	public int quantityDropped(java.util.Random random) {
		return 2;
	}

}
