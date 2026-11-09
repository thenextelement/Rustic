package rustic.common.potions;

import net.minecraft.entity.EntityLivingBase;

public class PotionFeather extends PotionBase {

	protected PotionFeather(int id) {
		super(id, false, 14474460, "feather");
		setIconIndex(1, 0);
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amplifier) {
		if (!entity.onGround && entity.motionY < -0.4) {
			entity.motionY += 0.1;
			entity.velocityChanged = true;
			entity.fallDistance = 0;
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

}
