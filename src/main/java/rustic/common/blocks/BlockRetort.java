package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.util.Facing;
import rustic.core.Rustic;

/** Metadata = 5 - facing ordinal (east 0, west 1, south 2, north 3), as in 1.12. */
public class BlockRetort extends BlockBase implements IModelBlock, ISupportShape {

	public BlockRetort(String name) {
		super(Material.rock, name);
		setHardness(2F);
		this.setLightOpacity(0);
		setCreativeTab(Rustic.alchemyTab);
		setStepSound(soundTypeStone);
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
	}

	public static ForgeDirection getFacing(int meta) {
		return ForgeDirection.getOrientation(5 - (meta & 3));
	}

	public static int metaForFacing(ForgeDirection facing) {
		return 5 - facing.ordinal();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		world.setBlockMetadataWithNotify(x, y, z, metaForFacing(Facing.horizontalFacing(placer).getOpposite()), 2);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		ForgeDirection back = getFacing(world.getBlockMetadata(x, y, z)).getOpposite();
		int cx = x + back.offsetX, cz = z + back.offsetZ;
		Block other = world.getBlock(cx, y, cz);
		if (other == ModBlocks.CONDENSER || other == ModBlocks.CONDENSER_ADVANCED) {
			return other.onBlockActivated(world, cx, y, cz, player, side, hitX, hitY, hitZ);
		}
		return false;
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return side.offsetY != 0;
	}

	@Override
	public String getBlockStateName() {
		return name;
	}

	@Override
	public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
		return getInventoryProperties(world.getBlockMetadata(x, y, z));
	}

	@Override
	public Map<String, String> getInventoryProperties(int meta) {
		Map<String, String> props = new HashMap<>();
		props.put("facing", Facing.name(getFacing(meta)));
		return props;
	}

}
