package rustic.common.blocks;

import java.util.Map;

import net.minecraft.world.IBlockAccess;

/**
 * A block rendered by Rustic's JSON model renderer, which reads the 1.12-style blockstate and model JSON files in
 * {@code assets/rustic/blockstates} and {@code assets/rustic/models/block}.
 * <p>
 * The block describes its current appearance as a map of blockstate property names to values (e.g. {@code facing ->
 * east}), which is matched against the blockstate file exactly like vanilla 1.12 would.
 */
public interface IModelBlock {

	/** Name of the blockstate file (without extension), usually the block's registry name. */
	String getBlockStateName();

	/** Blockstate properties for the block placed in the world. */
	Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z);

	/** Blockstate properties used to render the item form for the given item damage. */
	Map<String, String> getInventoryProperties(int meta);

	/** Whether to apply 1.12's random horizontal "XZ" offset (like flowers and berry bushes). */
	default boolean hasRandomOffset() {
		return false;
	}

	/** 1.12's per-position random XZ offset, in blocks. */
	static double[] randomOffset(int x, int z) {
		long i = (long) (x * 3129871) ^ (long) z * 116129781L;
		i = i * i * 42317861L + i * 11L;
		return new double[] { ((i >> 16 & 15L) / 15.0F - 0.5D) * 0.5D, ((i >> 24 & 15L) / 15.0F - 0.5D) * 0.5D };
	}

	/** Whether the model should be drawn when rendering the block as an item (otherwise a flat icon is used). */
	default boolean renderItemAsModel(int meta) {
		return true;
	}

}
