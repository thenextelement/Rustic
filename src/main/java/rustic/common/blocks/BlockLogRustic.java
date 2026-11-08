package rustic.common.blocks;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rustic.core.Rustic;

/** Metadata: low 2 bits = wood type, bits 4/8 = axis (like vanilla logs). */
public class BlockLogRustic extends BlockLog implements ItemBlockMeta.INamedVariants {

	public BlockLogRustic() {
		super();
		setBlockName(Rustic.MODID + ".log");
		setCreativeTab(Rustic.farmingTab);
		GameRegistry.registerBlock(this, ItemBlockMeta.class, "log");
		Blocks.fire.setFireInfo(this, 5, 5);
	}

	@Override
	public String[] getVariantNames() {
		return BlockPlanksRustic.EnumType.NAMES;
	}

	@Override
	public int damageDropped(int meta) {
		return BlockPlanksRustic.EnumType.byMetadata(meta & 3).getMetadata();
	}

	@Override
	protected ItemStack createStackedBlock(int meta) {
		return new ItemStack(this, 1, damageDropped(meta));
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
	public void registerBlockIcons(IIconRegister register) {
		int n = BlockPlanksRustic.EnumType.values().length;
		field_150167_a = new IIcon[n];
		field_150166_b = new IIcon[n];
		for (BlockPlanksRustic.EnumType type : BlockPlanksRustic.EnumType.values()) {
			field_150167_a[type.getMetadata()] = register.registerIcon(Rustic.MODID + ":log_" + type.getName());
			field_150166_b[type.getMetadata()] = register.registerIcon(Rustic.MODID + ":log_" + type.getName() + "_top");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IIcon getSideIcon(int meta) {
		return field_150167_a[(meta & 3) % field_150167_a.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IIcon getTopIcon(int meta) {
		return field_150166_b[(meta & 3) % field_150166_b.length];
	}

}
