package rustic.common.util;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import rustic.common.blocks.BlockRopeBase;
import rustic.common.blocks.ModBlocks;

/** Dispensers lower rope from a rope block in front of them. */
public class DispenseRope extends BehaviorDefaultDispenseItem {

    private static final DispenseRope INSTANCE = new DispenseRope();

    public static DispenseRope getInstance() {
        return INSTANCE;
    }

    private DispenseRope() {}

    @Override
    public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
        World world = source.getWorld();
        EnumFacing facing = BlockDispenser.func_149937_b(source.getBlockMetadata());
        int x = source.getXInt() + facing.getFrontOffsetX();
        int y = source.getYInt() + facing.getFrontOffsetY();
        int z = source.getZInt() + facing.getFrontOffsetZ();
        if (world.getBlock(x, y, z) == ModBlocks.ROPE) {
            if (addRope(world, x, y, z)) {
                stack.splitStack(1);
                return stack;
            }
        } else if (world.isAirBlock(x, y, z)
            && world.getBlock(x + facing.getFrontOffsetX(), y + facing.getFrontOffsetY(), z + facing.getFrontOffsetZ())
                == ModBlocks.ROPE) {
                    if (addRope(
                        world,
                        x + facing.getFrontOffsetX(),
                        y + facing.getFrontOffsetY(),
                        z + facing.getFrontOffsetZ())) {
                        stack.splitStack(1);
                        return stack;
                    }
                }
        return super.dispenseStack(source, stack);
    }

    private boolean addRope(World world, int x, int y, int z) {
        int yOffset = 1;
        for (; yOffset < 64 && world.getBlock(x, y - yOffset, z) == ModBlocks.ROPE; yOffset++) {
            if (ModBlocks.ROPE.getAxis(world, x, y - yOffset, z) != BlockRopeBase.AXIS_Y) {
                return false;
            }
        }
        if (y - yOffset > 0 && ModBlocks.ROPE.canPlaceBlockAt(world, x, y - yOffset, z)) {
            world.setBlock(x, y - yOffset, z, ModBlocks.ROPE, BlockRopeBase.AXIS_Y, 3);
            world.playSoundEffect(
                x + 0.5,
                y - yOffset + 0.5,
                z + 0.5,
                ModBlocks.ROPE.stepSound.func_150496_b(),
                (ModBlocks.ROPE.stepSound.getVolume() + 1.0F) / 2.0F,
                ModBlocks.ROPE.stepSound.getPitch() * 0.8F);
            return true;
        }
        return false;
    }

}
