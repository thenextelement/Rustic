package rustic.common.blocks;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import rustic.common.tileentity.TileEntityEvaporatingBasin;
import rustic.core.Rustic;

public class BlockEvaporatingBasin extends BlockTileBase implements IModelBlock {

    public BlockEvaporatingBasin() {
        super(Material.rock, "evaporating_basin");
        setHardness(1.25F);
        this.setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeStone);
        setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.25F, 0.9375F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityEvaporatingBasin();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityEvaporatingBasin
            && ((TileEntityEvaporatingBasin) tile).activate(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @Override
    public String getBlockStateName() {
        return name;
    }

    @Override
    public Map<String, String> getRenderProperties(IBlockAccess world, int x, int y, int z) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        return Collections.emptyMap();
    }

}
