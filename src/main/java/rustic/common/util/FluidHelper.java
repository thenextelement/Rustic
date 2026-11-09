package rustic.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;
import rustic.common.items.ItemFluidBottle;
import rustic.common.items.ModItems;

/**
 * 1.7.10 replacement for the parts of 1.12's {@code FluidUtil} that Rustic uses. Understands
 * {@link FluidContainerRegistry} containers (buckets, vanilla bottles, cells from other mods),
 * {@link IFluidContainerItem}s, and turns glass bottles into Rustic fluid bottles.
 */
public final class FluidHelper {

	private FluidHelper() {}

	/** The fluid held by an item, or null. */
	public static FluidStack getFluidContained(ItemStack stack) {
		if (stack == null) return null;
		if (stack.getItem() instanceof IFluidContainerItem) {
			FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
			return fluid != null && fluid.amount > 0 ? fluid : null;
		}
		return FluidContainerRegistry.getFluidForFilledItem(stack);
	}

	/** Whether the item can hold fluids at all (full or empty). */
	public static boolean isFluidContainer(ItemStack stack) {
		if (stack == null) return false;
		return stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack)
				|| stack.getItem() == Items.glass_bottle;
	}

	/** A bucket (or other registered container) filled with the fluid, or null if there is none. */
	public static ItemStack getFilledBucket(FluidStack fluid) {
		return FluidContainerRegistry.fillFluidContainer(fluid, new ItemStack(Items.bucket));
	}

	/** The container left after emptying a full one. */
	public static ItemStack getEmptyContainer(ItemStack full) {
		if (full == null) return null;
		if (full.getItem() instanceof ItemFluidBottle) return new ItemStack(Items.glass_bottle);
		if (full.getItem() instanceof IFluidContainerItem) {
			ItemStack copy = full.copy();
			copy.stackSize = 1;
			((IFluidContainerItem) copy.getItem()).drain(copy, Integer.MAX_VALUE, true);
			return copy;
		}
		return FluidContainerRegistry.drainFluidContainer(full);
	}

	/**
	 * Fill a container with fluid from the given stack. Returns the filled container and sets {@code out[0]} to the
	 * amount used, or returns null if it can't be filled.
	 */
	public static ItemStack fillContainer(ItemStack empty, FluidStack available, int[] out) {
		if (empty == null || available == null) return null;
		ItemStack single = empty.copy();
		single.stackSize = 1;
		if (single.getItem() == Items.glass_bottle && ItemFluidBottle.VALID_FLUIDS.contains(available.getFluid())) {
			if (available.amount < ItemFluidBottle.CAPACITY) return null;
			out[0] = ItemFluidBottle.CAPACITY;
			ItemStack bottle = new ItemStack(ModItems.FLUID_BOTTLE);
			ModItems.FLUID_BOTTLE.fill(bottle, available, true);
			return bottle;
		}
		if (single.getItem() instanceof IFluidContainerItem) {
			IFluidContainerItem item = (IFluidContainerItem) single.getItem();
			int filled = item.fill(single, available, true);
			if (filled <= 0) return null;
			out[0] = filled;
			return single;
		}
		if (FluidContainerRegistry.isEmptyContainer(single)) {
			ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, single);
			if (filled == null) return null;
			FluidStack inside = FluidContainerRegistry.getFluidForFilledItem(filled);
			if (inside == null || inside.amount > available.amount) return null;
			out[0] = inside.amount;
			return filled;
		}
		return null;
	}

	/**
	 * Right-click a tank with the held item: empties full containers into the tank, or fills empty ones from it.
	 *
	 * @return true if any fluid moved
	 */
	public static boolean interactWithTank(EntityPlayer player, IFluidTank tank) {
		ItemStack held = player.getCurrentEquippedItem();
		if (held == null) return false;

		FluidStack contained = getFluidContained(held);
		if (contained != null) {
			if (tank.fill(contained, false) != contained.amount) return false;
			tank.fill(contained, true);
			if (!player.capabilities.isCreativeMode) {
				replaceHeldItem(player, getEmptyContainer(held));
			}
			return true;
		}

		FluidStack inTank = tank.getFluid();
		if (inTank == null || inTank.amount <= 0) return false;
		int[] used = new int[1];
		ItemStack filled = fillContainer(held, inTank.copy(), used);
		if (filled == null || used[0] <= 0) return false;
		FluidStack drained = tank.drain(used[0], false);
		if (drained == null || drained.amount < used[0]) return false;
		tank.drain(used[0], true);
		if (!player.capabilities.isCreativeMode) {
			replaceHeldItem(player, filled);
		} else {
			RusticUtils.givePlayerItem(player, filled);
		}
		return true;
	}

	/** Uses up one of the held item and gives the player the replacement. */
	private static void replaceHeldItem(EntityPlayer player, ItemStack replacement) {
		ItemStack held = player.getCurrentEquippedItem();
		if (held.stackSize <= 1) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, replacement);
		} else {
			held.stackSize--;
			RusticUtils.givePlayerItem(player, replacement);
		}
		player.inventoryContainer.detectAndSendChanges();
	}

}
