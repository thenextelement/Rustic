package rustic.common.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import rustic.common.items.ModItems;
import rustic.core.Rustic;

/** Metadata: low 2 bits = wood type, 4 = not decayable (player placed), 8 = check decay (like vanilla). */
public class BlockLeavesRustic extends BlockLeaves implements ItemBlockMeta.INamedVariants {

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockLeavesRustic() {
		super();
		setBlockName(Rustic.MODID + ".leaves");
		setCreativeTab(Rustic.farmingTab);
		GameRegistry.registerBlock(this, ItemBlockLeaves.class, "leaves");
		Blocks.fire.setFireInfo(this, 30, 60);
	}

	@Override
	public String[] getVariantNames() {
		return BlockPlanksRustic.EnumType.NAMES;
	}

	@Override
	public String[] func_150125_e() {
		return BlockPlanksRustic.EnumType.NAMES;
	}

	/** Drops the leaves' fruit; called by vanilla's leaf drop logic. */
	@Override
	protected void func_150124_c(World world, int x, int y, int z, int meta, int chance) {
		// fruit drops are handled in getDrops so that the chance can depend on the wood type
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.SAPLING);
	}

	@Override
	public int damageDropped(int meta) {
		return meta & 3;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		Random rand = world.rand;
		int chance = 20;
		if (fortune > 0) {
			chance -= 2 << fortune;
			if (chance < 10) chance = 10;
		}
		if (rand.nextInt(chance) == 0) {
			ret.add(new ItemStack(getItemDropped(meta, rand, fortune), 1, damageDropped(meta)));
		}

		BlockPlanksRustic.EnumType type = BlockPlanksRustic.EnumType.byMetadata(meta & 3);
		chance = type == BlockPlanksRustic.EnumType.OLIVE ? 6 : 16;
		if (fortune > 0) {
			chance -= fortune;
			if (chance < 2) chance = 2;
		}
		if (rand.nextInt(chance) == 0) {
			ret.add(new ItemStack(type == BlockPlanksRustic.EnumType.OLIVE ? ModItems.OLIVES : ModItems.IRONBERRIES));
		}
		return ret;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(new ItemStack(this, 1, world.getBlockMetadata(x, y, z) & 3));
		return ret;
	}

	@Override
	protected ItemStack createStackedBlock(int meta) {
		return new ItemStack(this, 1, meta & 3);
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) & 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockPlanksRustic.EnumType type : BlockPlanksRustic.EnumType.values()) {
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return ColorizerFoliage.getFoliageColorBasic();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int meta) {
		return ColorizerFoliage.getFoliageColorBasic();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int r = 0, g = 0, b = 0;
		for (int dz = -1; dz <= 1; ++dz) {
			for (int dx = -1; dx <= 1; ++dx) {
				int color = world.getBiomeGenForCoords(x + dx, z + dz).getBiomeFoliageColor(x + dx, y, z + dz);
				r += (color & 16711680) >> 16;
				g += (color & 65280) >> 8;
				b += color & 255;
			}
		}
		return (r / 9 & 255) << 16 | (g / 9 & 255) << 8 | b / 9 & 255;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[BlockPlanksRustic.EnumType.values().length];
		for (BlockPlanksRustic.EnumType type : BlockPlanksRustic.EnumType.values()) {
			icons[type.getMetadata()] = register.registerIcon(Rustic.MODID + ":leaves_" + type.getName());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[(meta & 3) % icons.length];
	}

	@Override
	public boolean isOpaqueCube() {
		return Blocks.leaves.isOpaqueCube();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return Blocks.leaves.shouldSideBeRendered(world, x, y, z, side);
	}

}
