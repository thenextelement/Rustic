package rustic.common.items;

import rustic.common.blocks.crops.BlockStakeCrop;
import rustic.core.Rustic;

/** Seeds planted by using them on a crop stake. */
public class ItemStakeCropSeed extends ItemBase {

	private final BlockStakeCrop crop;

	public ItemStakeCropSeed(String name, BlockStakeCrop crop) {
		super(name);
		setCreativeTab(Rustic.farmingTab);
		this.crop = crop;
	}

	public BlockStakeCrop getCrop() {
		return crop;
	}

}
