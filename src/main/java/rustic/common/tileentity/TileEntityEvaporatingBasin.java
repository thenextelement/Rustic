package rustic.common.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.blocks.BlockTileBase;
import rustic.common.crafting.IEvaporatingBasinRecipe;
import rustic.common.crafting.Recipes;
import rustic.common.util.FluidHelper;
import rustic.common.util.ItemStackHandlerRustic;

public class TileEntityEvaporatingBasin extends TileEntityFluidBase implements BlockTileBase.IBreakable {

	private static final int CAPACITY = FluidContainerRegistry.BUCKET_VOLUME * 6;

	private FluidStack evaporatedFluid;
	private int age = 0;
	private int remainder = 0;

	private final ItemStackHandlerRustic itemStackHandler = new ItemStackHandlerRustic(1) {
		@Override
		protected void onContentsChanged(int slot) {
			sync();
		}
	};

	public TileEntityEvaporatingBasin() {
		super(CAPACITY);
		tank = new RusticTank(CAPACITY) {
			@Override
			public boolean canFillFluidType(FluidStack fluid) {
				return fluid != null && Recipes.evaporatingRecipesMap.containsKey(fluid.getFluid()) && canFill();
			}

			@Override
			public int fill(FluidStack resource, boolean doFill) {
				return canFillFluidType(resource) ? super.fill(resource, doFill) : 0;
			}

			@Override
			protected void onContentsChanged() {
				onTankChanged();
			}
		};
	}

	public ItemStackHandlerRustic getItemHandler() {
		return itemStackHandler;
	}

	@Override
	public boolean canFill(net.minecraftforge.common.util.ForgeDirection from, net.minecraftforge.fluids.Fluid fluid) {
		return Recipes.evaporatingRecipesMap.containsKey(fluid) && super.canFill(from, fluid);
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) return;
		age++;
		if (age >= 1000000) age = 0;
		if (age % 20 != 0) return;

		if (getFluid() == null || (evaporatedFluid != null && evaporatedFluid.getFluid() != getFluid())) {
			// change of recipe - reset
			evaporatedFluid = null;
			remainder = 0;
		}
		int toDrain = 0;
		IEvaporatingBasinRecipe recipe;
		if (getFluid() != null) {
			recipe = Recipes.evaporatingRecipesMap.get(getFluid());
			if (recipe != null) {
				int temp = 20 * recipe.getAmount() + remainder;
				toDrain = temp / recipe.getTime();
				remainder = temp % recipe.getTime();
			}
		}
		FluidStack drained = toDrain > 0 ? tank.drainInternal(toDrain, true) : null;
		if (drained != null) {
			if (evaporatedFluid == null || evaporatedFluid.getFluid() != drained.getFluid()) {
				evaporatedFluid = drained;
			} else {
				evaporatedFluid.amount += drained.amount;
			}
			sync();
		}
		if (evaporatedFluid != null && evaporatedFluid.amount > 0) {
			recipe = Recipes.evaporatingRecipesMap.get(evaporatedFluid.getFluid());
			if (recipe == null) return;
			ItemStack result = recipe.getOutput();
			if (evaporatedFluid.amount >= recipe.getAmount() && itemStackHandler.insertItem(0, result, true) == null) {
				evaporatedFluid.amount -= recipe.getAmount();
				itemStackHandler.insertItem(0, result.copy(), false);
				sync();
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("items")) itemStackHandler.deserializeNBT(tag.getCompoundTag("items"));
		evaporatedFluid = tag.hasKey("EvaporatedFluid") ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("EvaporatedFluid")) : null;
		remainder = tag.getInteger("remainder");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setTag("items", itemStackHandler.serializeNBT());
		if (evaporatedFluid != null) {
			tag.setTag("EvaporatedFluid", evaporatedFluid.writeToNBT(new NBTTagCompound()));
		}
		tag.setInteger("remainder", remainder);
	}

	public boolean activate(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
			float hitZ) {
		ItemStack heldItem = player.getCurrentEquippedItem();
		if (heldItem != null) {
			if (FluidHelper.isFluidContainer(heldItem)) {
				if (!world.isRemote && FluidHelper.interactWithTank(player, tank)) sync();
				return true; // prevents placing a bucket by clicking a basin with less than 1000mb space
			}
		} else if (player.isSneaking() && getAmount() > 0) {
			if (!world.isRemote) {
				tank.drainInternal(CAPACITY, true);
				world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "game.neutral.swim.splash", 1F, 1F);
				sync();
			}
			return true;
		}
		if (itemStackHandler.getStackInSlot(0) != null) {
			if (!world.isRemote) {
				world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, itemStackHandler.getStackInSlot(0)));
				itemStackHandler.setStackInSlot(0, null);
				sync();
			}
			return true;
		}
		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z) {
		if (!world.isRemote) {
			spawnAsEntity(world, x, y, z, itemStackHandler.getStackInSlot(0));
			itemStackHandler.getStacks()[0] = null;
		}
	}

}
