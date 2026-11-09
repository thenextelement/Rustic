package rustic.common.blocks.crops;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.blocks.BlockBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ISupportShape;
import rustic.common.blocks.ModBlocks;
import rustic.common.items.ItemStakeCropSeed;
import rustic.core.Rustic;

public class BlockCropStake extends BlockBase implements IModelBlock, ISupportShape {

    public static final Material CROP_STAKE = new Material(MapColor.woodColor) {

        @Override
        public boolean isSolid() {
            return false;
        }
    };

    public BlockCropStake() {
        super(CROP_STAKE, "crop_stake");
        setHardness(2F);
        setResistance(5F);
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeWood);
        setHarvestLevel("axe", 0);
        setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) return false;
        if (stack.getItem() instanceof ItemStakeCropSeed) {
            ItemStakeCropSeed seed = (ItemStakeCropSeed) stack.getItem();
            if (world.getBlock(x, y - 1, z)
                .canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, seed.getCrop())) {
                world.setBlock(x, y, z, seed.getCrop(), 0, 3);
                if (!player.capabilities.isCreativeMode) stack.stackSize--;
                return true;
            }
        } else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.ROPE)) {
            world.setBlock(x, y, z, ModBlocks.STAKE_TIED, 0, 2);
            world.scheduleBlockUpdate(x, y, z, ModBlocks.STAKE_TIED, 1);
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x + 0.4375, y, z + 0.4375, x + 0.5625, y + 1, z + 0.5625);
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
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.offsetY != 0;
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.offsetY != 0;
    }

    @Override
    public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
        return true;
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
