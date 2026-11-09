package rustic.common.entities;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import rustic.common.items.ModItems;
import rustic.common.potions.PotionsRustic;

public class EntityTomato extends EntityThrowable {

	public EntityTomato(World world) {
		super(world);
	}

	public EntityTomato(World world, EntityLivingBase thrower) {
		super(world, thrower);
	}

	public EntityTomato(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte id) {
		if (id == 3) {
			Random rand = this.worldObj.rand;
			for (int i = 0; i < rand.nextInt(8) + 4; ++i) {
				this.worldObj.spawnParticle("iconcrack_" + Item.getIdFromItem(ModItems.TOMATO), this.posX, this.posY, this.posZ,
						(rand.nextDouble() * 0.06) - 0.03, (rand.nextDouble() * 0.06) - 0.03, (rand.nextDouble() * 0.06) - 0.03);
			}
		} else {
			super.handleHealthUpdate(id);
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition result) {
		if (result.entityHit != null && result.entityHit != this.getThrower()) {
			result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0F);
			if (!this.worldObj.isRemote && result.entityHit instanceof EntityLivingBase) {
				((EntityLivingBase) result.entityHit).addPotionEffect(new PotionEffect(PotionsRustic.SHAME_POTION.id, 400, 0, false));
			}
		}
		if (result.entityHit == null || result.entityHit != this.getThrower()) {
			this.worldObj.playSoundAtEntity(this, "mob.slime.small", 0.5F, 0.4F / (this.worldObj.rand.nextFloat() * 0.4F + 0.8F));
			if (!this.worldObj.isRemote) {
				this.worldObj.setEntityState(this, (byte) 3);
				this.setDead();
			}
		}
	}

}
