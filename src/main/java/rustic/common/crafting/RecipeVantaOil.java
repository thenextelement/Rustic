package rustic.common.crafting;

import rustic.common.util.FluidHelper;
import rustic.common.util.RusticUtils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.Config;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.items.ModItems;
import rustic.common.util.ElixirUtils;
import rustic.common.util.RusticUtils;

public class RecipeVantaOil implements IRecipe {
	
	public static int getReturnBottleCount(InventoryCrafting inv) {
		int bottleCount = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack tempStack = inv.getStackInSlot(i);
			if ((tempStack == null)) continue;
			
			if (RusticUtils.isVantaOilBottle(tempStack)) {
				bottleCount++;
				continue;
			}
			PotionEffect tempEffect = getIngredientEffect(tempStack);
			if (tempEffect != null) {
				bottleCount++;
				continue;
			}
		}
		
		return bottleCount;
	}
	

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack weaponStack = null;
		ItemStack oilStack = null;
		Potion ingPotion = null;
		PotionEffect weaponEffect = null;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack tempStack = inv.getStackInSlot(i);
			if ((tempStack == null)) continue;
			
			if (RusticUtils.isVantaOilBottle(tempStack)) {
				if ((oilStack != null)) return false;
				oilStack = tempStack;
				continue;
			}
			PotionEffect tempEffect = getIngredientEffect(tempStack);
			if (tempEffect != null) {
				if (ingPotion != null) {
					// check if ingredient effects are compatible
					if (ingPotion != Potion.potionTypes[tempEffect.getPotionID()]) return false;
				} else {
					ingPotion = Potion.potionTypes[tempEffect.getPotionID()];
					if (weaponEffect != null) {
						// check if weapon and ing potions are compatible
						Potion weaponPotion = Potion.potionTypes[weaponEffect.getPotionID()];
						if ((weaponPotion != null) && (weaponPotion != ingPotion)) return false;
					}
				}
				continue;
			}
			if (RusticUtils.isVantaOilableWeapon(tempStack)) {
				if ((weaponStack != null)) return false;
				weaponStack = tempStack;
				weaponEffect = ElixirUtils.getVantaOilEffect(weaponStack);
				if ((weaponEffect != null) && (ingPotion != null)) {
					// check if weapon and ing potions are compatible
					Potion weaponPotion = Potion.potionTypes[weaponEffect.getPotionID()];
					if ((weaponPotion != null) && (weaponPotion != ingPotion)) return false;
				}
				continue;
			}
			
			return false;
		}
		
		return ((weaponStack != null) && (oilStack != null) && (ingPotion != null));
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack weaponStack = null;
		ItemStack oilStack = null;
		Potion ingPotion = null;
		int amplifier = 0;
		int duration = 0;
		PotionEffect weaponEffect = null;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack tempStack = inv.getStackInSlot(i);
			if ((tempStack == null)) continue;
			
			if (RusticUtils.isVantaOilBottle(tempStack)) {
				if ((oilStack != null)) return null;
				oilStack = tempStack;
				continue;
			}
			PotionEffect tempEffect = getIngredientEffect(tempStack);
			if (tempEffect != null) {
				if (ingPotion != null) {
					if ((tempEffect.getAmplifier() >= 0) && (tempEffect.getAmplifier() < amplifier)) amplifier = tempEffect.getAmplifier();
					duration += ingPotion.isInstant() ? 1 : Math.max(tempEffect.getDuration(), 1);
					// check if ingredient effects are compatible
					if (ingPotion != Potion.potionTypes[tempEffect.getPotionID()]) return null;
				} else {
					ingPotion = Potion.potionTypes[tempEffect.getPotionID()];
					amplifier = tempEffect.getAmplifier();
					duration = ingPotion.isInstant() ? 1 : Math.max(tempEffect.getDuration(), 1);
					if (weaponEffect != null) {
						// check if weapon and ing potions are compatible
						Potion weaponPotion = Potion.potionTypes[weaponEffect.getPotionID()];
						if ((weaponPotion != null) && (weaponPotion != ingPotion)) return null;
					}
				}
				continue;
			}
			if (RusticUtils.isVantaOilableWeapon(tempStack)) {
				if ((weaponStack != null)) return null;
				weaponStack = tempStack;
				weaponEffect = ElixirUtils.getVantaOilEffect(weaponStack);
				if ((weaponEffect != null) && (ingPotion != null)) {
					// check if weapon and ing effects are compatible
					Potion weaponPotion = Potion.potionTypes[weaponEffect.getPotionID()];
					if ((weaponPotion != null) && (weaponPotion != ingPotion)) return null;
				}
				continue;
			}
			
			return null;
		}
		
		if ((weaponStack == null) || (oilStack == null) || (ingPotion == null)) return null;
		
		if (weaponEffect != null) {
			duration += Math.max(weaponEffect.getDuration(), 0);
			if ((weaponEffect.getAmplifier() >= 0) && (weaponEffect.getAmplifier() < amplifier)) amplifier = weaponEffect.getAmplifier();
		}
		
		ItemStack returnStack = weaponStack.copy();
		returnStack.stackSize = 1;
		ElixirUtils.setVantaOilEffect(returnStack, new PotionEffect(ingPotion.id, duration, amplifier));
		
		return returnStack;
	}
	
	
	public static PotionEffect getIngredientEffect(ItemStack stack) {
		final Item item = stack.getItem();
		if (item == Items.potionitem) {
			List<PotionEffect> effects = Items.potionitem.getEffects(stack);
			if (effects.size() == 1) {
				PotionEffect effect = effects.get(0);
				if (Potion.potionTypes[effect.getPotionID()] != null) return effect;
			}
			return (effects.size() == 1) ? effects.get(0) : null;
		} else if (item == ModItems.ELIXIR) {
			List<PotionEffect> effects = ElixirUtils.getEffects(stack);
			if (effects.size() == 1) {
				PotionEffect effect = effects.get(0);
				if (Potion.potionTypes[effect.getPotionID()] != null) return effect;
			}
		}
		return null;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}


	@Override
	public int getRecipeSize() {
		return 9;
	}
	

}
