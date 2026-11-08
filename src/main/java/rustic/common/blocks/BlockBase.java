package rustic.common.blocks;

import java.util.Collections;
import java.util.Map;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;
import rustic.client.json.JsonModelRegistry;
import rustic.core.Rustic;

public class BlockBase extends Block {

	protected final String name;

	@SideOnly(Side.CLIENT)
	protected IIcon[] faceIcons;

	/** Set on the client when the 1.12 item model is a flat "item/generated" sprite. */
	protected boolean flatItem;

	public BlockBase(Material mat, String name) {
		this(mat, name, true);
	}

	public BlockBase(Material mat, String name, boolean register) {
		super(mat);
		this.name = name;
		setBlockName(Rustic.MODID + "." + name);
		setBlockTextureName(Rustic.MODID + ":" + name);
		setHardness(1F);
		setCreativeTab(Rustic.decorTab);
		if (register) {
			register();
		}
	}

	public String getRegistryName() {
		return name;
	}

	public void register(Class<? extends ItemBlock> itemClass) {
		GameRegistry.registerBlock(this, itemClass, name);
	}

	public void register() {
		register(ItemBlock.class);
	}

	public BlockBase setBlockSoundType(SoundType soundType) {
		setStepSound(soundType);
		return this;
	}

	/** Properties used to look up this block's cube textures in its blockstate file. */
	protected Map<String, String> getCubeProperties(int meta) {
		return Collections.emptyMap();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		flatItem = getItemIconName() != null;
		if (this instanceof IModelBlock) {
			blockIcon = JsonModelRegistry.registerBlock(this, register);
		} else {
			faceIcons = JsonModelRegistry.registerCubeIcons(name, getCubeProperties(0), register);
			blockIcon = faceIcons[1];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if (faceIcons != null && side >= 0 && side < 6) return faceIcons[side];
		return blockIcon;
	}

	/** Item texture from the 1.12 item model, if it is a flat sprite; null otherwise (the item renders as a block). */
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemIconName() {
		return rustic.client.json.JsonModelLoader.itemIcon(name);
	}

	@Override
	public int getRenderType() {
		if (this instanceof IModelBlock) return flatItem ? ModBlocks.MODEL_RENDER_ID_FLAT : ModBlocks.MODEL_RENDER_ID;
		return super.getRenderType();
	}

}
