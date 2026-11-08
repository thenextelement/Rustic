package rustic.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGargoyle extends BlockRotatable implements ISupportShape {

	public BlockGargoyle() {
		super(Material.rock, "gargoyle");
		setHardness(1.0F);
		setLightOpacity(0);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 0.875F, 0.875F);
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return side == ForgeDirection.DOWN;
	}

}
