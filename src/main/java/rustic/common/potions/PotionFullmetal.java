package rustic.common.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;

public class PotionFullmetal extends PotionBase {

    public static final String MOVEMENT_SPEED_MODIFIER_UUID = "0CA3AAC1-2CD1-4552-AC7E-DD4984B8D7A3";
    public static final String KNOCKBACK_RESISTANCE_MODIFIER_UUID = "639E0E9E-0B0D-474C-86FA-626901789BAC";

    protected PotionFullmetal(int id) {
        super(id, false, 8220521, "fullmetal");
        setIconIndex(0, 1);
        func_111184_a(SharedMonsterAttributes.movementSpeed, MOVEMENT_SPEED_MODIFIER_UUID, -1F, 2);
        func_111184_a(SharedMonsterAttributes.knockbackResistance, KNOCKBACK_RESISTANCE_MODIFIER_UUID, 9001F, 0);
        this.setEffectiveness(1.0D);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.setJumping(false);
        entity.setSprinting(false);
        if (entity.ridingEntity instanceof EntityLivingBase) {
            entity.mountEntity(null);
        }
        if (!entity.onGround) {
            if (entity.isInWater() || entity.handleLavaMovement()) {
                entity.motionY -= 0.27 / 4;
            } else {
                entity.motionY -= 0.07;
            }
            entity.velocityChanged = true;
        }
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).capabilities.isFlying = false;
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public double func_111183_a(int amplifier, AttributeModifier modifier) {
        return modifier.getAmount();
    }

}
