package rustic.common.blocks;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.items.ModItems;
import rustic.core.Rustic;

public class BlockBeehive extends BlockRotatable implements ISupportShape {

	public BlockBeehive() {
		super(Material.gourd, "beehive");
		this.setHardness(0.5F);
		this.setCreativeTab(Rustic.farmingTab);
		setStepSound(soundTypeSlime());
		this.setLightOpacity(0);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1F, 0.875F);
	}

	private static SoundType soundTypeSlime() {
		return new SoundType("mob.slime.small", 1.0F, 1.0F) {
			@Override
			public String getBreakSound() {
				return "mob.slime.big";
			}

			@Override
			public String getStepResourcePath() {
				return "mob.slime.small";
			}

			@Override
			public String func_150496_b() {
				return "mob.slime.small";
			}
		};
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && world.getBlock(x, y + 1, z) instanceof BlockLeaves;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		Random rand = world.rand;
		int honeycomb = rand.nextInt(5);
		if (honeycomb > 0) ret.add(new ItemStack(ModItems.HONEYCOMB, honeycomb, 0));
		ret.add(new ItemStack(ModItems.BEE, rand.nextInt(2) + 1, 0));
		return ret;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		super.onNeighborBlockChange(world, x, y, z, neighbor);
		if (!(world.getBlock(x, y + 1, z) instanceof BlockLeaves)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return side.offsetY != 0;
	}

}
