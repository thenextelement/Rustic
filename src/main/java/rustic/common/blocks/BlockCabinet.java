package rustic.common.blocks;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.tileentity.TileEntityCabinet;
import rustic.common.util.Facing;
import rustic.core.Rustic;

/**
 * Metadata: 8 = top half of a double cabinet, 4 = mirrored (hinge on the other side), low 2 bits = 5 - facing ordinal.
 * Rendered by a tile entity special renderer.
 */
public class BlockCabinet extends BlockTileBase implements ISupportShape {

	public static final int GUI_ID = 3;

	/** Hit position from {@link #onBlockPlaced}, used in {@link #onBlockPlacedBy} which knows the placer. */
	private static final ThreadLocal<float[]> LAST_HIT = new ThreadLocal<>();

	public static class ItemBlockCabinet extends ItemBlock {
		public ItemBlockCabinet(Block block) {
			super(block);
		}

		@Override
		@SideOnly(Side.CLIENT)
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("material", Constants.NBT.TAG_COMPOUND)) {
				ItemStack material = ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("material"));
				if (material != null) tooltip.add(material.getDisplayName());
			}
		}
	}

	public BlockCabinet() {
		super(Material.wood, "cabinet", false);
		setHardness(2.5F);
		setStepSound(soundTypeWood);
		register(ItemBlockCabinet.class);
	}

	public static ForgeDirection getFacing(int meta) {
		return BlockRetort.getFacing(meta);
	}

	public static boolean isMirrored(int meta) {
		return (meta & 4) != 0;
	}

	public static boolean isTop(int meta) {
		return (meta & 8) != 0;
	}

	/** Whether this is the lower half of a double cabinet. */
	public static boolean isBottom(IBlockAccess world, int x, int y, int z) {
		return !isTop(world.getBlockMetadata(x, y, z)) && world.getBlock(x, y + 1, z) instanceof BlockCabinet
				&& isTop(world.getBlockMetadata(x, y + 1, z));
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		if (world.isRemote) return true;
		if (!(world.getTileEntity(x, y, z) instanceof TileEntityCabinet)) return false;
		if (side == getFacing(world.getBlockMetadata(x, y, z)).ordinal()) {
			player.openGui(Rustic.instance, GUI_ID, world, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);
		if (world.getBlock(x, y + 1, z) instanceof BlockCabinet && isTop(world.getBlockMetadata(x, y + 1, z))) {
			world.setBlockMetadataWithNotify(x, y + 1, z, world.getBlockMetadata(x, y + 1, z) & ~8, 3);
		}
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		return new ArrayList<>(); // dropped with its material by the tile entity
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCabinet();
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		LAST_HIT.set(new float[] { hitX, hitY, hitZ });
		return meta;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		float[] hit = LAST_HIT.get();
		LAST_HIT.remove();
		float hitX = hit == null ? 0.5F : hit[0], hitZ = hit == null ? 0.5F : hit[2];
		ForgeDirection f = Facing.horizontalFacing(placer).getOpposite();
		boolean m;
		if (f == ForgeDirection.NORTH) m = hitX >= 0.5;
		else if (f == ForgeDirection.SOUTH) m = hitX < 0.5;
		else if (f == ForgeDirection.EAST) m = hitZ >= 0.5;
		else m = hitZ < 0.5;

		boolean t = false;
		if (world.getBlock(x, y - 1, z) instanceof BlockCabinet) {
			int below = world.getBlockMetadata(x, y - 1, z);
			t = !isTop(below) && isMirrored(below) == m && getFacing(below) == f;
		}
		if (t && world.getTileEntity(x, y - 1, z) instanceof TileEntityCabinet) {
			((TileEntityCabinet) world.getTileEntity(x, y - 1, z)).updateContainingBlockInfo();
		}
		world.setBlockMetadataWithNotify(x, y, z, (t ? 8 : 0) | (m ? 4 : 0) | BlockRetort.metaForFacing(f), 3);

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityCabinet) {
			TileEntityCabinet te = (TileEntityCabinet) tile;
			if (stack.hasDisplayName()) te.setCustomName(stack.getDisplayName());
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null && tag.hasKey("material")) {
				te.material = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("material"));
			}
			te.markDirty();
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return side != getFacing(world.getBlockMetadata(x, y, z));
	}

	@Override
	public boolean canSupportOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return isSideSolid(world, x, y, z, side);
	}

}
