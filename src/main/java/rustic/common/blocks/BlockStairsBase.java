package rustic.common.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import rustic.core.Rustic;

public class BlockStairsBase extends BlockStairs {

	public BlockStairsBase(Block block, int meta, String name) {
		super(block, meta);
		this.setLightOpacity(0);
		setBlockName(Rustic.MODID + "." + name);
		GameRegistry.registerBlock(this, name);
		this.setCreativeTab(Rustic.decorTab);
	}

	public BlockStairsBase(Block block, String name) {
		this(block, 0, name);
	}

}
