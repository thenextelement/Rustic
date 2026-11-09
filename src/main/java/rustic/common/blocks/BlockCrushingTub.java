package rustic.common.blocks;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.tileentity.TileEntityCrushingTub;
import rustic.core.Rustic;

public class BlockCrushingTub extends BlockTileBase implements IModelBlock {

    public BlockCrushingTub() {
        super(Material.wood, "crushing_tub");
        this.setHardness(1.5F);
        this.setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeWood);
        this.setLightOpacity(0);
        setBlockBounds(0F, 0F, 0F, 1F, 0.5625F, 1F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCrushingTub();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityCrushingTub
            && ((TileEntityCrushingTub) tile).activate(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
        if (entity instanceof EntityLivingBase) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityCrushingTub) {
                ((TileEntityCrushingTub) tile).crush((EntityLivingBase) entity);
            }
        }
        super.onFallenUpon(world, x, y, z, entity, fallDistance);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN;
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
