package rustic.common.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import rustic.core.Rustic;

public class BlockFenceGateRustic extends BlockFenceGate {

	private final String planksTexture;

	/** @param planksTexture icon name of the planks texture, e.g. "rustic:planks_olive" */
	public BlockFenceGateRustic(String planksTexture, String name) {
		super();
		this.planksTexture = planksTexture;
		setBlockName(Rustic.MODID + "." + name);
		setHardness(2F);
		setResistance(5F);
		setStepSound(soundTypeWood);
		setCreativeTab(Rustic.decorTab);
		GameRegistry.registerBlock(this, name);
		Blocks.fire.setFireInfo(this, 5, 20);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon(planksTexture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return blockIcon;
	}

}
