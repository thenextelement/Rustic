package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.blocks.BlockBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;

/** A crop growing on a crop stake (tomatoes, chili peppers). Metadata = age (0..3). */
public class BlockStakeCrop extends BlockBase implements IGrowable, IPlantable, IModelBlock {

	public static final Material STAKE_CROP = new Material(MapColor.foliageColor) {
		{
			setImmovableMobility();
		}

		@Override
		public boolean isSolid() {
			return false;
		}

		@Override
		public boolean blocksMovement() {
			return false;
		}

		@Override
		public boolean getCanBlockGrass() {
			return false;
		}

		@Override
		public boolean isOpaque() {
			return false;
		}
	};

	public BlockStakeCrop(String name) {
		super(STAKE_CROP, name, false);
		setStepSound(soundTypeGrass);
		setHardness(0);
		setTickRandomly(true);
		setCreativeTab(null);
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1.0F, 0.875F);
		GameRegistry.registerBlock(this, null, name);
		Blocks.fire.setFireInfo(this, 30, 100);
	}

	public int getMaxAge() {
		return 3;
	}

	public int getMaxHeight() {
		return 3;
	}

	protected int getAge(int meta) {
		return Math.min(meta & 3, getMaxAge());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + 0.4375, y, z + 0.4375, x + 0.5625, y + 1, z + 0.5625);
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
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);
		if (!world.isRemote) world.setBlock(x, y, z, ModBlocks.CROP_STAKE, 0, 3);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		Random rand = world.rand;
		stacks.add(new ItemStack(getSeed(), rand.nextInt(2) + 1));
		if (getAge(meta) >= getMaxAge()) {
			stacks.add(new ItemStack(getCrop(rand)));
		}
		return stacks;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		super.onNeighborBlockChange(world, x, y, z, neighbor);
		checkAndDropBlock(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, x, y, z, rand);
		if (!checkAndDropBlock(world, x, y, z)) return;
		if (world.getBlockLightValue(x, y + 1, z) >= 9) {
			int meta = world.getBlockMetadata(x, y, z);
			int age = getAge(meta);
			if (age < getMaxAge()) {
				float f = getGrowthChance(world, x, y, z);
				if (rand.nextInt((int) (50.0F / f) + 1) == 0) {
					world.setBlockMetadataWithNotify(x, y, z, age + 1, 2);
				}
			} else if (world.getBlock(x, y + 1, z) == ModBlocks.CROP_STAKE
					&& world.getBlock(x, y - (getMaxHeight() - 1), z) != this) {
				float f = getGrowthChance(world, x, y, z);
				if (rand.nextInt((int) (30.0F / f) + 1) == 0) {
					world.setBlock(x, y + 1, z, this, 0, 3);
				}
			}
		}
	}

	protected float getGrowthChance(World world, int x, int y, int z) {
		float growth = 0.125F * (world.getBlockLightValue(x, y, z) - 11);
		Block soil = world.getBlock(x, y - 1, z);
		if (soil.isFertile(world, x, y - 1, z) || soil == this) growth *= 1.5F;
		return Math.abs(1.5F + growth);
	}

	/** Returns false (and turns back into a plain stake) if the crop can't stay here. */
	protected boolean checkAndDropBlock(World world, int x, int y, int z) {
		if (!canBlockStay(world, x, y, z)) {
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, ModBlocks.CROP_STAKE, 0, 3);
			return false;
		}
		return true;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		Block soil = world.getBlock(x, y - 1, z);
		return (world.getFullBlockLightValue(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z))
				&& (soil == this || soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this));
	}

	// IGrowable: canGrow
	@Override
	public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
		return getAge(world.getBlockMetadata(x, y, z)) < getMaxAge();
	}

	// IGrowable: canUseBonemeal
	@Override
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		return true;
	}

	// IGrowable: grow
	@Override
	public void func_149853_b(World world, Random rand, int x, int y, int z) {
		int age = getAge(world.getBlockMetadata(x, y, z)) + MathHelper.getRandomIntegerInRange(world.rand, 2, 5);
		world.setBlockMetadataWithNotify(x, y, z, Math.min(age, getMaxAge()), 2);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Crop;
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return getSeed();
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}

	public Item getSeed() {
		return null;
	}

	public Item getCrop() {
		return null;
	}

	protected Item getCrop(Random rand) {
		return this.getCrop();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		boolean result = tryHarvest(world, x, y, z, player, side);
		for (int cy = y + 1; cy < world.getHeight(); cy++) {
			if (world.getBlock(x, cy, z) != this) break;
			result = tryHarvest(world, x, cy, z, player, side) || result;
		}
		for (int cy = y - 1; cy >= 0; cy--) {
			if (world.getBlock(x, cy, z) != this) break;
			result = tryHarvest(world, x, cy, z, player, side) || result;
		}
		return result;
	}

	public boolean tryHarvest(World world, int x, int y, int z, EntityPlayer player, int side) {
		int meta = world.getBlockMetadata(x, y, z);
		if (getAge(meta) >= getMaxAge()) {
			world.setBlockMetadataWithNotify(x, y, z, getMaxAge() - 1, 2);
			if (!world.isRemote) {
				ItemStack stack = new ItemStack(getCrop(world.rand));
				if (!player.inventory.addItemStackToInventory(stack)) {
					ForgeDirection dir = ForgeDirection.getOrientation(side);
					world.spawnEntityInWorld(new EntityItem(world, x + 0.5 + dir.offsetX, y + 0.5 + dir.offsetY,
							z + 0.5 + dir.offsetZ, stack));
				}
			}
			return true;
		}
		return false;
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
		return Collections.singletonMap("age", String.valueOf(getAge(meta)));
	}

}
