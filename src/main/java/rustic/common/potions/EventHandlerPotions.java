package rustic.common.potions;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import rustic.common.network.MessageFirePowerAttack;
import rustic.common.network.PacketHandler;

public class EventHandlerPotions {

	private final Random rand = new Random();

	/** Drinking water sobers you up a bit. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWaterBottleUse(PlayerUseItemEvent.Finish event) {
		EntityLivingBase entity = event.entityLiving;
		PotionEffect effect = entity.getActivePotionEffect(PotionsRustic.TIPSY);
		if (effect != null && !entity.worldObj.isRemote) {
			ItemStack stack = event.item;
			if (stack != null && stack.getItem() == Items.potionitem && stack.getItemDamage() == 0) {
				int duration = effect.getDuration();
				int amplifier = effect.getAmplifier();
				if (rand.nextFloat() < 0.1F) {
					amplifier--;
				} else {
					duration -= (rand.nextInt(800) + 200);
				}
				entity.removePotionEffect(PotionsRustic.TIPSY.id);
				if (amplifier >= 0 && duration > 0) {
					entity.addPotionEffect(new PotionEffect(PotionsRustic.TIPSY.id, duration, amplifier, false));
				}
			}
		}
	}

	@SubscribeEvent
	public void onJumpEvent(LivingEvent.LivingJumpEvent event) {
		if (event.entityLiving != null && event.entityLiving.isPotionActive(PotionsRustic.FULLMETAL_POTION)) {
			event.entityLiving.motionY = 0;
		}
	}

	@SubscribeEvent
	public void onSleepEvent(PlayerSleepInBedEvent event) {
		if (event.entityPlayer != null && event.entityPlayer.isPotionActive(PotionsRustic.FULLMETAL_POTION)) {
			event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;
		}
	}

	@SubscribeEvent
	public void onFallEvent(LivingFallEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if (entity.isPotionActive(PotionsRustic.FULLMETAL_POTION) && event.distance >= 0.6F && !entity.isInWater()
				&& !entity.handleLavaMovement()) {
			entity.playSound("random.anvil_land", 1.0F, 1.0F);
		}
	}

	/** Left click (on anything) with fire power shoots a fireball. */
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouse(MouseEvent event) {
		if (event.button != 0 || !event.buttonstate) return;
		EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		if (p == null || Minecraft.getMinecraft().currentScreen != null) return;
		if (!p.isPotionActive(PotionsRustic.FIRE_POWER_POTION)) return;
		if (!p.isSwingInProgress && !p.isSneaking()) {
			PacketHandler.INSTANCE.sendToServer(new MessageFirePowerAttack());
		}
	}

	@SubscribeEvent
	public void onPlayerAttack(AttackEntityEvent event) {
		// handled by onMouse on the client
	}

	public static void doFirePowerAttack(EntityPlayer p) {
		if (p == null || p.worldObj.isRemote) return;
		PotionEffect effect = p.getActivePotionEffect(PotionsRustic.FIRE_POWER_POTION);
		if (effect == null) return;
		int ex = MathHelper.floor_double(p.posX), ey = MathHelper.floor_double(p.posY + p.getEyeHeight()), ez = MathHelper.floor_double(p.posZ);
		if (p.worldObj.getBlock(ex, ey, ez).getMaterial() == Material.water) return;

		double f = 0.005;
		Vec3 look = p.getLookVec();
		double d1 = look.xCoord * 40d;
		double d2 = look.yCoord * 40d;
		double d3 = look.zCoord * 40d;
		EntityFireball fb;
		if (effect.getAmplifier() > 0) {
			fb = new EntityLargeFireball(p.worldObj, p, d1 + p.getRNG().nextGaussian() * f, d2, d3 + p.getRNG().nextGaussian() * f);
			((EntityLargeFireball) fb).field_92057_e = effect.getAmplifier();
		} else {
			fb = new EntitySmallFireball(p.worldObj, p, d1 + p.getRNG().nextGaussian() * f, d2, d3 + p.getRNG().nextGaussian() * f);
		}
		fb.posY = p.posY + p.getEyeHeight();
		fb.posX += look.xCoord;
		fb.posZ += look.zCoord;
		fb.posY += look.yCoord;
		fb.motionX += p.motionX;
		fb.motionZ += p.motionZ;
		if (!p.onGround) fb.motionY += p.motionY;
		fb.setPosition(fb.posX, fb.posY, fb.posZ);
		if (fb.boundingBox != null && p.worldObj.checkBlockCollision(fb.boundingBox.expand(0.001, 0.001, 0.001))) {
			return;
		}
		p.worldObj.playSoundEffect(p.posX + look.xCoord, p.posY + p.getEyeHeight() + look.yCoord, p.posZ + look.zCoord,
				"mob.ghast.fireball", 1.0F, 0.4F / (p.getRNG().nextFloat() * 0.4F + 1.2F));
		p.worldObj.spawnEntityInWorld(fb);
	}

	// FULLMETAL, IRON SKIN, FULL, MAGIC RESISTANCE, WITHER WARD
	@SubscribeEvent
	public void onEntityDamage(LivingHurtEvent event) {
		DamageSource source = event.source;
		EntityLivingBase entity = event.entityLiving;
		if (entity.isPotionActive(PotionsRustic.FULLMETAL_POTION) && source != DamageSource.outOfWorld && !source.canHarmInCreative()) {
			event.ammount = 0;
			return;
		}
		PotionEffect ironSkin = entity.getActivePotionEffect(PotionsRustic.IRON_SKIN_POTION);
		if (ironSkin != null && !source.isUnblockable()) {
			// 1.12 gave +3 armor per level; each armor point blocks 4% of the damage
			float reduction = Math.min(0.12F * (ironSkin.getAmplifier() + 1), 0.8F);
			event.ammount *= 1F - reduction;
		}
		if (source == DamageSource.starve) {
			PotionEffect effect = entity.getActivePotionEffect(PotionsRustic.FULL_POTION);
			if (effect != null) {
				event.ammount = Math.max(event.ammount - (effect.getAmplifier() + 1), 0);
			}
		} else if (source == DamageSource.magic) {
			PotionEffect effect = entity.getActivePotionEffect(PotionsRustic.MAGIC_RESISTANCE_POTION);
			if (effect != null) {
				event.ammount /= (2F * (effect.getAmplifier() + 1F));
			}
		} else if (source == DamageSource.wither) {
			PotionEffect effect = entity.getActivePotionEffect(PotionsRustic.WITHER_WARD_POTION);
			if (effect != null) {
				event.ammount /= (2F * (effect.getAmplifier() + 1F));
			}
		}
	}

	// UNDYING
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeath(LivingDeathEvent event) {
		if (event.source.canHarmInCreative()) return;
		if (!(event.entityLiving instanceof EntityPlayer)) return;
		EntityPlayer p = (EntityPlayer) event.entityLiving;
		PotionEffect effect = p.getActivePotionEffect(PotionsRustic.UNDYING_POTION);
		if (effect == null) return;
		p.removePotionEffect(PotionsRustic.UNDYING_POTION.id);
		p.setHealth(1.0F);
		p.clearActivePotions();
		p.addPotionEffect(new PotionEffect(Potion.regeneration.id, 900, 1));
		p.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 100, 1));
		if (effect.getAmplifier() > 0) {
			p.addPotionEffect(new PotionEffect(PotionsRustic.UNDYING_POTION.id, effect.getDuration(),
					effect.getAmplifier() - 1, effect.getIsAmbient()));
		}
		event.setCanceled(true);
	}

}
