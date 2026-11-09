package rustic.common.blocks;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.core.Rustic;

public class BlockDoorRustic extends BlockDoor {

    protected Item item;

    /**
     * @param name registry name, e.g. "olive_door"; textures are "rustic:door_olive_upper/lower" for the block and
     *             "rustic:door_olive" for the item
     */
    protected BlockDoorRustic(Material material, String name) {
        super(material);
        String wood = name.substring(0, name.indexOf('_'));
        setBlockName(Rustic.MODID + "." + name);
        setBlockTextureName(Rustic.MODID + ":door_" + wood);
        setHardness(3F);
        setStepSound(soundTypeWood);
        disableStats();
        GameRegistry.registerBlock(this, null, name);

        item = new ItemDoorRustic(material, this);
        item.setUnlocalizedName(Rustic.MODID + "." + name);
        item.setTextureName(Rustic.MODID + ":door_" + wood);
        item.setCreativeTab(Rustic.decorTab);
        GameRegistry.registerItem(item, name);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return (meta & 8) != 0 ? null : item;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return item;
    }

    public static class ItemDoorRustic extends ItemDoor {

        private final BlockDoor door;

        public ItemDoorRustic(Material material, BlockDoor door) {
            super(material);
            this.door = door;
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
            if (side != 1) return false;
            ++y;
            if (player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack)) {
                if (!door.canPlaceBlockAt(world, x, y, z)) return false;
                int facing = MathHelper.floor_double((player.rotationYaw + 180.0F) * 4.0F / 360.0F - 0.5D) & 3;
                placeDoorBlock(world, x, y, z, facing, door);
                --stack.stackSize;
                return true;
            }
            return false;
        }
    }

}
