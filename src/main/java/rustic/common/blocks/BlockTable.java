package rustic.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockTable extends BlockBase implements IModelBlock, ISupportShape {

    public BlockTable(String type) {
        super(Material.wood, "table_" + type);
        setHardness(1F);
        setStepSound(soundTypeWood);
        setBlockBounds(0F, 0.875F, 0F, 1F, 1F, 1F);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.UP;
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.UP;
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
        boolean north = world.getBlock(x, y, z - 1) instanceof BlockTable;
        boolean south = world.getBlock(x, y, z + 1) instanceof BlockTable;
        boolean east = world.getBlock(x + 1, y, z) instanceof BlockTable;
        boolean west = world.getBlock(x - 1, y, z) instanceof BlockTable;
        return legs(!north && !west, !north && !east, !south && !east, !south && !west);
    }

    @Override
    public Map<String, String> getInventoryProperties(int meta) {
        return legs(true, true, true, true);
    }

    private static Map<String, String> legs(boolean nw, boolean ne, boolean se, boolean sw) {
        Map<String, String> props = new HashMap<>();
        props.put("nw", String.valueOf(nw));
        props.put("ne", String.valueOf(ne));
        props.put("se", String.valueOf(se));
        props.put("sw", String.valueOf(sw));
        return props;
    }

}
