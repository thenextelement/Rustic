package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.tileentity.TileEntityApiary;
import rustic.common.util.Facing;
import rustic.core.Rustic;

public class BlockApiary extends BlockTileBase implements IModelBlock {

	public static final int GUI_ID = 0;

	public BlockApiary() {
		super(Material.wood, "apiary");
		this.setHardness(1F);
		this.setCreativeTab(Rustic.farmingTab);
		setStepSound(soundTypeWood);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityApiary();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		if (world.isRemote) return true;
		if (!(world.getTileEntity(x, y, z) instanceof TileEntityApiary)) return false;
		player.openGui(Rustic.instance, GUI_ID, world, x, y, z);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		ForgeDirection facing = Facing.horizontalFacing(placer).getOpposite();
		world.setBlockMetadataWithNotify(x, y, z, Facing.horizontalIndex(facing), 2);
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
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
		props.put("facing", Facing.name(Facing.fromHorizontalIndex(meta & 3)));
		return props;
	}

}
