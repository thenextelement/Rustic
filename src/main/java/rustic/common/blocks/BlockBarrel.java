package rustic.common.blocks;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import rustic.common.tileentity.TileEntityBarrel;
import rustic.core.Rustic;

public class BlockBarrel extends BlockTileBase implements IModelBlock {

	public static final int GUI_ID = 2;

	public BlockBarrel() {
		super(Material.wood, "barrel");
		this.setHardness(1.5F);
		setStepSound(soundTypeWood);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1F, 0.875F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBarrel();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		if (world.isRemote) return true;
		if (!(world.getTileEntity(x, y, z) instanceof TileEntityBarrel)) return false;
		player.openGui(Rustic.instance, GUI_ID, world, x, y, z);
		return true;
	}

	@Override
	public String getBlockStateName() {
		return name;
	}

	@Override
	public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, String> getInventoryProperties(int meta) {
		return Collections.emptyMap();
	}

}
