package rustic.common.potions;

import net.minecraft.entity.EntityLivingBase;
import rustic.common.network.MessageShameFX;
import rustic.common.network.PacketHandler;

public class PotionShame extends PotionBase {

	protected PotionShame(int id) {
		super(id, true, 16409650, "shame");
		setIconIndex(3, 0);
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp) {
		if (entity != null && !entity.worldObj.isRemote) {
			PacketHandler.INSTANCE.sendToAll(new MessageShameFX(entity.worldObj.rand.nextInt(6) + 6, entity.posX, entity.posY,
					entity.posZ, entity.motionX, entity.motionY, entity.motionZ, entity.width, entity.height));
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return (duration % 5) == 0;
	}

}
