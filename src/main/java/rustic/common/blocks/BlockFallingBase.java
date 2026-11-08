package rustic.common.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import rustic.client.json.JsonModelRegistry;
import rustic.core.Rustic;

public class BlockFallingBase extends BlockFalling {

	protected final String name;

	public BlockFallingBase(Material mat, String name) {
		this(mat, name, true);
	}

	public BlockFallingBase(Material mat, String name, boolean register) {
		super(mat);
		this.name = name;
		setBlockName(Rustic.MODID + "." + name);
		setHardness(1F);
		setCreativeTab(Rustic.decorTab);
		if (register) {
			GameRegistry.registerBlock(this, ItemBlock.class, name);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		if (this instanceof IModelBlock) {
			blockIcon = JsonModelRegistry.registerBlock(this, register);
		} else {
			blockIcon = JsonModelRegistry.registerCubeIcons(name, java.util.Collections.emptyMap(), register)[1];
		}
	}

	@Override
	public int getRenderType() {
		return this instanceof IModelBlock ? ModBlocks.MODEL_RENDER_ID : super.getRenderType();
	}

}
