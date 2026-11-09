package rustic.common.util;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import rustic.common.Config;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.items.ItemFluidBottle;
import rustic.core.Rustic;

public class RusticUtils {

	public static final DamageSource BAD_AMBROSIA_DAMAGE = (new DamageSource("badAmbrosia")).setDamageBypassesArmor()
			.setDamageAllowedInCreativeMode();

	public static String getRegistryName(Item item) {
		String name = GameData.getItemRegistry().getNameForObject(item);
		return name == null ? "" : name;
	}

	public static boolean isVantaOilableWeapon(ItemStack stack) {
		if (stack == null) return false;
		Item item = stack.getItem();
		if (item instanceof ItemSword || item instanceof ItemAxe) return true;
		if (item.getHarvestLevel(stack, "axe") >= 0) return true;
		String itemName = getRegistryName(item);
		for (String entry : Config.VANTA_OIL_WHITELIST) {
			if (entry.startsWith("#")) {
				for (ItemStack ore : OreDictionary.getOres(entry.substring(1))) {
					if (OreDictionary.itemMatches(ore, stack, false)) return true;
				}
				continue;
			}
			int damageSeparatorIndex = entry.indexOf("@");
			if (damageSeparatorIndex >= 0) {
				String entryName = entry.substring(0, damageSeparatorIndex);
				if (itemName.equals(entryName)) {
					String dmgString = entry.substring(damageSeparatorIndex + 1);
					if (dmgString.isEmpty()) return true;
					try {
						if (stack.getItemDamage() == Integer.parseInt(dmgString)) return true;
					} catch (NumberFormatException e) {}
				}
			} else if (itemName.equals(entry)) {
				return true;
			}
		}
		return false;
	}

	public static FluidStack getBottleFluid(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemFluidBottle)) return null;
		return ((ItemFluidBottle) stack.getItem()).getFluid(stack);
	}

	public static boolean isVantaOilBottle(ItemStack stack) {
		FluidStack fluidStack = getBottleFluid(stack);
		return fluidStack != null && fluidStack.getFluid() == ModFluids.VANTA_OIL;
	}

	public static boolean isBottleOf(ItemStack stack, Fluid fluid) {
		if (stack == null || fluid == null) return false;
		if (fluid == FluidRegistry.WATER) {
			// a plain water bottle (damage 0, no custom effects)
			return stack.getItem() == Items.potionitem && stack.getItemDamage() == 0
					&& (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("CustomPotionEffects"));
		}
		FluidStack fluidStack = getBottleFluid(stack);
		return fluidStack != null && fluidStack.getFluid() == fluid;
	}

	public static void givePlayerItem(EntityPlayer player, ItemStack stack) {
		if (player == null || stack == null) return;
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropPlayerItemWithRandomChoice(stack, false);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<ItemStack> getSubItems(Item item) {
		List<ItemStack> list = new ArrayList<>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			if (itemTab == null) continue;
			List subItems = new ArrayList();
			try {
				item.getSubItems(item, itemTab, subItems);
			} catch (RuntimeException | LinkageError e) {
				Rustic.logger.warn("Caught a crash while getting sub-items of {}", item, e);
			}
			for (Object o : subItems) {
				ItemStack subItem = (ItemStack) o;
				if (subItem != null && subItem.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
					ItemStack copy = subItem.copy();
					copy.stackSize = 1;
					list.add(copy);
				}
			}
		}
		if (list.isEmpty()) list.add(new ItemStack(item));
		return list;
	}

	public static MovingObjectPosition rayTrace(World world, EntityPlayer player, boolean useLiquids) {
		float f = player.rotationPitch;
		float f1 = player.rotationYaw;
		double d0 = player.posX;
		double d1 = player.posY + (world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight());
		double d2 = player.posZ;
		Vec3 start = Vec3.createVectorHelper(d0, d1, d2);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double reach = 5.0D;
		if (player instanceof EntityPlayerMP) {
			reach = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}
		Vec3 end = start.addVector(f6 * reach, f5 * reach, f7 * reach);
		return world.func_147447_a(start, end, useLiquids, !useLiquids, false);
	}

}
