package rustic.common.blocks;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Same placement rules and metadata as {@link BlockCandle}. */
public class BlockIronTorch extends BlockCandle {

	public BlockIronTorch() {
		this("iron_torch");
	}

	public BlockIronTorch(String name) {
		this(Material.circuits, name, true);
	}

	public BlockIronTorch(Material mat, String name, boolean register) {
		super(mat, name, register);
		this.setHardness(0F);
		this.setLightLevel(1.0F);
		setStepSound(soundTypeMetal);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch (facingOf(world.getBlockMetadata(x, y, z))) {
			case EAST: setBlockBounds(0.0F, 0.0F, 0.375F, 0.40625F, 0.7F, 0.625F); break;
			case WEST: setBlockBounds(0.59375F, 0.0F, 0.375F, 1.0F, 0.7F, 0.625F); break;
			case SOUTH: setBlockBounds(0.375F, 0.0F, 0.0F, 0.625F, 0.7F, 0.40625F); break;
			case NORTH: setBlockBounds(0.375F, 0.0F, 0.59375F, 0.625F, 0.7F, 1.0F); break;
			default: setBlockBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.7F, 0.6F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.4F, 0.0F, 0.4F, 0.6F, 0.7F, 0.6F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		ForgeDirection facing = facingOf(world.getBlockMetadata(x, y, z));
		double d0 = x + 0.5D;
		double d1 = y + 0.7D;
		double d2 = z + 0.5D;
		if (facing.offsetY == 0) {
			ForgeDirection back = facing.getOpposite();
			d0 += 0.21875D * back.offsetX;
			d2 += 0.21875D * back.offsetZ;
		}
		world.spawnParticle("smoke", d0, d1, d2, 0, 0, 0);
		world.spawnParticle("flame", d0, d1, d2, 0, 0, 0);
	}

}
