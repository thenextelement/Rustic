package rustic.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.common.registry.GameRegistry;
import rustic.core.Rustic;

public class BlockFenceRustic extends BlockFence {

    /** @param planksTexture icon name of the planks texture, e.g. "rustic:planks_olive" */
    public BlockFenceRustic(String planksTexture, String name) {
        super(planksTexture, Material.wood);
        setBlockName(Rustic.MODID + "." + name);
        setHardness(2F);
        setResistance(5F);
        setStepSound(soundTypeWood);
        setCreativeTab(Rustic.decorTab);
        GameRegistry.registerBlock(this, name);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockFence && block.getMaterial() == this.getMaterial()) return true;
        if (block instanceof BlockFenceGate) return true;
        return super.canConnectFenceTo(world, x, y, z);
    }

}
