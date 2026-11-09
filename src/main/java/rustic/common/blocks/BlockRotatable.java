package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.util.Facing;

/** A model block facing one of the four horizontal directions, stored in the low two bits of metadata. */
public class BlockRotatable extends BlockBase implements IModelBlock {

    public BlockRotatable(Material mat, String name) {
        super(mat, name);
    }

    public static ForgeDirection getFacing(int meta) {
        return Facing.fromHorizontalIndex(meta & 3);
    }

    public ForgeDirection getFacing(IBlockAccess world, int x, int y, int z) {
        return getFacing(world.getBlockMetadata(x, y, z));
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        ForgeDirection facing = Facing.horizontalFacing(placer)
            .getOpposite();
        world.setBlockMetadataWithNotify(
            x,
            y,
            z,
            (world.getBlockMetadata(x, y, z) & ~3) | Facing.horizontalIndex(facing),
            2);
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection next = Facing.rotateY(getFacing(meta));
        world.setBlockMetadataWithNotify(x, y, z, (meta & ~3) | Facing.horizontalIndex(next), 3);
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
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
        props.put("facing", Facing.name(getFacing(meta)));
        return props;
    }

}
