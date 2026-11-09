package rustic.common.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.tileentity.TileEntityVase;
import rustic.core.Rustic;

/** Metadata = vase variant (0..12). */
public class BlockVase extends BlockTileBase implements IModelBlock, ISupportShape {

    public static final int GUI_ID = 1;
    public static final int MIN_VARIANT = 0, MAX_VARIANT = 12;
    public static final int[] ITEM_ORDER = { 0, // brown ceramic
        5, // brown ceramic w/ horse design
        6, // white ceramic
        2, // white ceramic w/ blue pattern
        1, // white ceramic w/ brown pattern
        7, // light gray ceramic
        4, // dark gray ceramic
        3, // green ceramic
        8, // alt model brown ceramic
        9, // alt model white ceramic
        10, // alt model white ceramic w/ blue pattern
        12, // alt model medium gray ceramic
        11, // alt model dark gray ceramic
    };

    public static class ItemBlockVase extends ItemBlock {

        public ItemBlockVase(net.minecraft.block.Block block) {
            super(block);
            setHasSubtypes(true);
            setMaxDamage(0);
        }

        @Override
        public int getMetadata(int damage) {
            return damage;
        }
    }

    public BlockVase() {
        super(Material.rock, "vase", false);
        setHardness(0.5F);
        setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1F, 0.875F);
        register(ItemBlockVase.class);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVase();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (!(world.getTileEntity(x, y, z) instanceof TileEntityVase)) return false;
        player.openGui(Rustic.instance, GUI_ID, world, x, y, z);
        return true;
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return meta;
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int variant : ITEM_ORDER) {
            list.add(new ItemStack(item, 1, variant));
        }
    }

    @Override
    public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN;
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
        return Collections.singletonMap("variant", String.valueOf(Math.min(Math.max(meta, MIN_VARIANT), MAX_VARIANT)));
    }

}
