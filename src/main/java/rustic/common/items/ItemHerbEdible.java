package rustic.common.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import rustic.client.json.JsonModelLoader;
import rustic.common.blocks.crops.BlockHerbBase;
import rustic.core.Rustic;

/** An edible herb that can also be planted to grow its crop. */
public class ItemHerbEdible extends ItemFood implements IPlantable {

    private final BlockHerbBase herbBlock;

    public ItemHerbEdible(BlockHerbBase herbBlock, int hunger, float saturation) {
        super(hunger, saturation, false);
        this.herbBlock = herbBlock;
        setUnlocalizedName(Rustic.MODID + "." + herbBlock.getRegistryName());
        setCreativeTab(Rustic.alchemyTab);
        GameRegistry.registerItem(this, herbBlock.getRegistryName());
        initFood();
    }

    public void initFood() {}

    @Override
    public String getUnlocalizedName() {
        return herbBlock.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return herbBlock.getUnlocalizedName();
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister register) {
        String icon = JsonModelLoader.itemIcon(herbBlock.getRegistryName());
        itemIcon = register.registerIcon(icon != null ? icon : Rustic.MODID + ":herbs/" + herbBlock.getRegistryName());
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (side != 1) return false;
        if (!player.canPlayerEdit(x, y, z, side, stack) || !player.canPlayerEdit(x, y + 1, z, side, stack))
            return false;
        Block soil = world.getBlock(x, y, z);
        if (soil.canSustainPlant(world, x, y, z, ForgeDirection.UP, this) && world.isAirBlock(x, y + 1, z)) {
            world.setBlock(x, y + 1, z, herbBlock);
            --stack.stackSize;
            return true;
        }
        return false;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        return herbBlock.getPlantType(world, x, y, z);
    }

    @Override
    public Block getPlant(IBlockAccess world, int x, int y, int z) {
        return herbBlock;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
        return 0;
    }

}
