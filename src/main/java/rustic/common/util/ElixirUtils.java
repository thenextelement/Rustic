package rustic.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class ElixirUtils {

	/*
	 * Custom Effect NBT Tag Format:
	 *
	 * {ElixirEffects:[{ Effect:"[potion name, e.g. potion.regeneration]", Id:[numeric id], Duration:[duration],
	 * Amplifier:[amplifier] }]}
	 *
	 * 1.7.10 potions have no registry names; the potion's unlocalized name is stored so that elixirs survive potion id
	 * changes, with the numeric id as a fallback. 1.12-style names like "minecraft:regeneration" are also understood.
	 */

	public static String getPotionKey(Potion potion) {
		return potion.getName();
	}

	public static Potion getPotionByName(String name) {
		if (name == null || name.isEmpty()) return null;
		String bare = name.contains(":") ? name.substring(name.indexOf(':') + 1) : null;
		for (Potion potion : Potion.potionTypes) {
			if (potion == null) continue;
			String key = potion.getName();
			if (key.equals(name)) return potion;
			if (bare != null) {
				String suffix = key.substring(key.indexOf('.') + 1);
				if (suffix.equalsIgnoreCase(bare)) return potion;
			}
		}
		return null;
	}

	private static Potion readPotion(NBTTagCompound tag) {
		Potion potion = getPotionByName(tag.getString("Effect"));
		if (potion == null && tag.hasKey("Id", 99)) {
			int id = tag.getInteger("Id");
			if (id >= 0 && id < Potion.potionTypes.length) potion = Potion.potionTypes[id];
		}
		return potion;
	}

	private static void writePotion(NBTTagCompound tag, Potion potion) {
		tag.setString("Effect", getPotionKey(potion));
		tag.setInteger("Id", potion.id);
	}

	public static List<PotionEffect> deserializeEffects(NBTTagCompound tag) {
		List<PotionEffect> effects = new ArrayList<>();
		if (tag != null && tag.hasKey("ElixirEffects", 9)) {
			NBTTagList tagList = tag.getTagList("ElixirEffects", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound effectTag = tagList.getCompoundTagAt(i);
				Potion potion = readPotion(effectTag);
				if (potion != null) {
					effects.add(new PotionEffect(potion.id, effectTag.getInteger("Duration"), effectTag.getInteger("Amplifier")));
				}
			}
		}
		return effects;
	}

	public static List<PotionEffect> getEffects(ItemStack stack) {
		if (stack != null && stack.hasTagCompound()) {
			return deserializeEffects(stack.getTagCompound());
		}
		return new ArrayList<>();
	}

	public static void addEffect(PotionEffect effect, ItemStack stack) {
		Potion potion = Potion.potionTypes[effect.getPotionID()];
		if (potion == null) return;
		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		if (!tag.hasKey("ElixirEffects", 9)) {
			tag.setTag("ElixirEffects", new NBTTagList());
		}
		NBTTagCompound effectTag = new NBTTagCompound();
		writePotion(effectTag, potion);
		effectTag.setInteger("Duration", effect.getDuration());
		effectTag.setInteger("Amplifier", effect.getAmplifier());
		tag.getTagList("ElixirEffects", 10).appendTag(effectTag);
		stack.setTagCompound(tag);
	}

	public static NBTTagCompound getVantaOilTag(ItemStack stack) {
		if (stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("vanta_oil", 10)) return null;
		return stack.getTagCompound().getCompoundTag("vanta_oil");
	}

	public static PotionEffect getVantaOilEffect(NBTTagCompound vantaTag) {
		if (vantaTag == null) return null;
		if (!vantaTag.hasKey("Effect", 8) || !vantaTag.hasKey("Duration", 3) || !vantaTag.hasKey("Amplifier", 3)) return null;
		int duration = vantaTag.getInteger("Duration");
		if (duration < 1) return null;
		int amplifier = vantaTag.getInteger("Amplifier");
		if (amplifier < 0) return null;
		Potion potion = readPotion(vantaTag);
		if (potion == null) return null;
		return new PotionEffect(potion.id, duration, amplifier);
	}

	public static PotionEffect getVantaOilEffect(ItemStack stack) {
		return getVantaOilEffect(getVantaOilTag(stack));
	}

	public static ItemStack setVantaOilTag(ItemStack stack, NBTTagCompound vantaTag) {
		if (stack != null) {
			if (vantaTag != null) {
				stack.setTagInfo("vanta_oil", vantaTag);
			} else if (stack.hasTagCompound()) {
				stack.getTagCompound().removeTag("vanta_oil");
				if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
			}
		}
		return stack;
	}

	public static NBTTagCompound setVantaOilEffect(NBTTagCompound vantaTag, PotionEffect effect) {
		if (effect == null || Potion.potionTypes[effect.getPotionID()] == null || effect.getDuration() < 1 || effect.getAmplifier() < 0) {
			return null;
		}
		if (vantaTag == null) vantaTag = new NBTTagCompound();
		writePotion(vantaTag, Potion.potionTypes[effect.getPotionID()]);
		vantaTag.setInteger("Duration", effect.getDuration());
		vantaTag.setInteger("Amplifier", effect.getAmplifier());
		return vantaTag;
	}

	public static ItemStack setVantaOilEffect(ItemStack stack, PotionEffect effect) {
		return setVantaOilTag(stack, setVantaOilEffect(getVantaOilTag(stack), effect));
	}

	public static int getColor(ItemStack stack) {
		List<PotionEffect> effects = getEffects(stack);
		if (effects.isEmpty()) return 16253176;
		Potion potion = Potion.potionTypes[effects.get(0).getPotionID()];
		return potion != null ? potion.getLiquidColor() : 16253176;
	}

	@SideOnly(Side.CLIENT)
	public static void addPotionTooltip(ItemStack itemIn, List<String> lores, float durationFactor) {
		List<PotionEffect> list = getEffects(itemIn);
		List<Object[]> modifiers = new ArrayList<>();
		if (list.isEmpty()) {
			lores.add(EnumChatFormatting.GRAY + I18n.format("potion.empty").trim());
			return;
		}
		for (PotionEffect potioneffect : list) {
			String s1 = I18n.format(potioneffect.getEffectName()).trim();
			Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
			@SuppressWarnings("unchecked")
			Map<IAttribute, AttributeModifier> map = potion.func_111186_k();
			if (map != null && !map.isEmpty()) {
				for (Entry<IAttribute, AttributeModifier> entry : map.entrySet()) {
					AttributeModifier modifier = entry.getValue();
					AttributeModifier scaled = new AttributeModifier(modifier.getName(),
							potion.func_111183_a(potioneffect.getAmplifier(), modifier), modifier.getOperation());
					modifiers.add(new Object[] { entry.getKey().getAttributeUnlocalizedName(), scaled });
				}
			}
			if (potioneffect.getAmplifier() > 0) {
				s1 = s1 + " " + I18n.format("potion.potency." + potioneffect.getAmplifier()).trim();
			}
			if (potioneffect.getDuration() > 20) {
				s1 = s1 + " (" + StringUtils.ticksToElapsedTime((int) (potioneffect.getDuration() * durationFactor)) + ")";
			}
			lores.add((potion.isBadEffect() ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + s1);
		}
		if (!modifiers.isEmpty()) {
			lores.add("");
			lores.add(EnumChatFormatting.DARK_PURPLE + I18n.format("potion.effects.whenDrank"));
			for (Object[] entry : modifiers) {
				AttributeModifier modifier = (AttributeModifier) entry[1];
				double amount = modifier.getAmount();
				double shown = (modifier.getOperation() != 1 && modifier.getOperation() != 2) ? amount : amount * 100.0D;
				if (amount > 0.0D) {
					lores.add(EnumChatFormatting.BLUE + I18n.format("attribute.modifier.plus." + modifier.getOperation(),
							ItemStack.field_111284_a.format(shown), I18n.format("attribute.name." + entry[0])));
				} else if (amount < 0.0D) {
					lores.add(EnumChatFormatting.RED + I18n.format("attribute.modifier.take." + modifier.getOperation(),
							ItemStack.field_111284_a.format(-shown), I18n.format("attribute.name." + entry[0])));
				}
			}
		}
	}

	public static final int VANTA_OIL_EFFECT_DURATION = 15 * 20;
	public static final int VANTA_OIL_EFFECT_MAX_DURATION = VANTA_OIL_EFFECT_DURATION + (5 * 20);

	public static int getRemainingVantaUses(PotionEffect effect) {
		int duration = effect.getDuration();
		if (duration <= 0) return 0;
		if (Potion.potionTypes[effect.getPotionID()].isInstant()) return duration;
		int hits = duration / VANTA_OIL_EFFECT_DURATION;
		int remainderDuration = duration % VANTA_OIL_EFFECT_DURATION;
		if ((remainderDuration > (VANTA_OIL_EFFECT_MAX_DURATION - VANTA_OIL_EFFECT_DURATION)) || (hits == 0)) hits++;
		return hits;
	}

	public static int getNextVantaHitDuration(int totalDuration) {
		return (totalDuration > VANTA_OIL_EFFECT_MAX_DURATION) ? VANTA_OIL_EFFECT_DURATION : totalDuration;
	}

	@SideOnly(Side.CLIENT)
	public static void addVantaOilTooltip(ItemStack itemIn, PotionEffect vantaEffect, List<String> lores) {
		if (!lores.isEmpty()) lores.add("");
		lores.add(EnumChatFormatting.DARK_PURPLE + I18n.format("tooltip.rustic.vanta_oil", vantaEffect.getDuration()).trim());
		String effectName = I18n.format(vantaEffect.getEffectName()).trim();
		Potion potion = Potion.potionTypes[vantaEffect.getPotionID()];
		if (vantaEffect.getAmplifier() > 0) {
			effectName = effectName + " " + I18n.format("potion.potency." + vantaEffect.getAmplifier()).trim();
		}
		if (!potion.isInstant()) {
			effectName = effectName + " (" + StringUtils.ticksToElapsedTime(getNextVantaHitDuration(vantaEffect.getDuration())) + ")";
		}
		lores.add(" " + (potion.isBadEffect() ? EnumChatFormatting.RED : EnumChatFormatting.BLUE) + effectName);
		int uses = getRemainingVantaUses(vantaEffect);
		lores.add(" " + I18n.format((uses == 1) ? "tooltip.rustic.vanta_oil_use" : "tooltip.rustic.vanta_oil_uses", uses));
	}

}
