package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import rustic.common.tileentity.TileEntityBrewingBarrel;
import rustic.common.util.Facing;
import rustic.core.Rustic;

/** Metadata = horizontal facing index. */
public class BlockBrewingBarrel extends BlockTileBase implements IModelBlock {

    public static final int GUI_ID = 5;

    public BlockBrewingBarrel() {
        super(Material.wood, "brewing_barrel");
        setCreativeTab(Rustic.farmingTab);
        setHardness(1.5F);
        this.setLightOpacity(0);
        setStepSound(soundTypeWood);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(
            x,
            y,
            z,
            Facing.horizontalIndex(
                Facing.horizontalFacing(placer)
                    .getOpposite()),
            2);
        super.onBlockPlacedBy(world, x, y, z, placer, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBrewingBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityBrewingBarrel) {
            if (!world.isRemote) player.openGui(Rustic.instance, GUI_ID, world, x, y, z);
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
        Map<String, String> props = new HashMap<>();
        props.put("facing", Facing.name(Facing.fromHorizontalIndex(meta & 3)));
        return props;
    }

}
