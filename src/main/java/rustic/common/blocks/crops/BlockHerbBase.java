package rustic.common.blocks.crops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import rustic.client.json.JsonModelLoader;
import rustic.client.json.JsonModelRegistry;
import rustic.core.Rustic;

/** Metadata = age (0..3). */
public abstract class BlockHerbBase extends BlockBush implements IGrowable, IPlantable {

	protected final String name;
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	private static final float[] HEIGHTS = { 0.125F, 0.25F, 0.5F, 0.875F };

	public BlockHerbBase(String name, boolean hasItemBlock) {
		super();
		this.name = name;
		setBlockName(Rustic.MODID + "." + name);
		if (hasItemBlock) {
			// the edible herb item (ItemHerbEdible) is registered separately under the same name
			GameRegistry.registerBlock(this, null, name);
		} else {
			GameRegistry.registerBlock(this, ItemHerbBase.class, name);
		}
		setHardness(0F);
		this.setTickRandomly(true);
		setStepSound(soundTypeGrass);
		setCreativeTab(Rustic.alchemyTab);
	}

	public String getRegistryName() {
		return name;
	}

	@Override
	public abstract EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z);

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		setBlockBounds(0F, 0F, 0F, 1F, HEIGHTS[getAge(world.getBlockMetadata(x, y, z))], 1F);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, x, y, z, rand);
		if (world.getBlock(x, y, z) != this) return;
		if (world.getBlockLightValue(x, y + 1, z) >= 9) {
			int age = getAge(world.getBlockMetadata(x, y, z));
			if (age < getMaxAge() && rand.nextInt((int) (45.0F / getGrowthChance()) + 1) == 0) {
				world.setBlockMetadataWithNotify(x, y, z, age + 1, 2);
			}
		}
	}

	protected float getGrowthChance() {
		return 3F;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(new ItemStack(getHerb()));
		int age = getAge(meta);
		if (age >= getMaxAge()) {
			int k = 2 + fortune;
			for (int i = 0; i < k; ++i) {
				if (world.rand.nextInt(2 * getMaxAge()) <= age) {
					ret.add(new ItemStack(getHerb()));
				}
			}
		}
		return ret;
	}

	public abstract Item getHerb();

	public int getMaxAge() {
		return 3;
	}

	protected int getAge(int meta) {
		return Math.min(meta & 3, getMaxAge());
	}

	public boolean isMaxAge(int meta) {
		return getAge(meta) >= getMaxAge();
	}

	// IGrowable: canGrow
	@Override
	public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
		return !isMaxAge(world.getBlockMetadata(x, y, z));
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
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return getHerb();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return getHerb();
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[getMaxAge() + 1];
		for (int age = 0; age <= getMaxAge(); age++) {
			icons[age] = JsonModelRegistry.registerStateTexture(name, Collections.singletonMap("age", String.valueOf(age)), "cross", register);
		}
		blockIcon = icons[getMaxAge()];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[getAge(meta)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemIconName() {
		return JsonModelLoader.itemIcon(name);
	}

	public static class ItemHerbBase extends ItemBlock implements IPlantable {
		private final BlockHerbBase herbBlock;

		public ItemHerbBase(Block block) {
			super(block);
			this.herbBlock = (BlockHerbBase) block;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IIcon getIconFromDamage(int damage) {
			if (getSpriteNumber() == 1) return super.getIconFromDamage(damage); // has its own item texture
			return herbBlock.getIcon(0, herbBlock.getMaxAge());
		}

		@Override
		public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
			return herbBlock.getPlantType(world, x, y, z);
		}

		@Override
		public Block getPlant(IBlockAccess world, int x, int y, int z) {
			return herbBlock;
		}

		@Override
		public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
			return 0;
		}
	}

}
