package rustic.common.blocks;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockChandelier extends BlockFallingBase implements IModelBlock, ISupportShape {

    public BlockChandelier() {
        this("chandelier");
    }

    public BlockChandelier(String name) {
        this(Material.iron, name, true);
    }

    public BlockChandelier(Material mat, String name, boolean register) {
        super(mat, name, register);
        this.setHardness(2F);
        setStepSound(soundTypeAnvil);
        setBlockBounds(0F, 0F, 0F, 1F, 0.25F, 1F);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.offsetY == 0;
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side != ForgeDirection.DOWN;
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
    protected void func_149829_a(EntityFallingBlock fallingEntity) {
        fallingEntity.func_145806_a(true); // hurt entities
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            checkFallable(world, x, y, z);
        }
    }

    private boolean suspended(World world, int x, int y, int z) {
        if (world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN, false)) return true;
        Block above = world.getBlock(x, y + 1, z);
        return above instanceof BlockRopeBase
            && ((BlockRopeBase) above).getAxis(world, x, y + 1, z) == BlockRopeBase.AXIS_Y;
    }

    private void checkFallable(World world, int x, int y, int z) {
        if (BlockFalling.func_149831_e(world, x, y - 1, z) && y >= 0 && !suspended(world, x, y, z)) {
            int r = 32;
            if (!fallInstantly && world.checkChunksExist(x - r, y - r, z - r, x + r, y + r, z + r)) {
                EntityFallingBlock entity = new EntityFallingBlock(
                    world,
                    x + 0.5D,
                    y + 0.5D,
                    z + 0.5D,
                    this,
                    world.getBlockMetadata(x, y, z));
                this.func_149829_a(entity);
                world.spawnEntityInWorld(entity);
            } else {
                world.setBlockToAir(x, y, z);
                while (BlockFalling.func_149831_e(world, x, y - 1, z) && y > 0) --y;
                if (y > 0) world.setBlock(x, y, z, this);
            }
        }
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
