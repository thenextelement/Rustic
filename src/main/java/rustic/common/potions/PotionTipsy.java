package rustic.common.potions;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionTipsy extends PotionBase {

    public PotionTipsy(int id) {
        super(id, true, 7900290, "tipsy");
        setIconIndex(4, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amp) {
        if (entity != null && !entity.worldObj.isRemote && amp > 1) {
            if (amp > 2) {
                entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 1, false));
                entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 400, 1, false));
                entity.addPotionEffect(new PotionEffect(Potion.blindness.id, 400, 0, false));
            } else {
                entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 0, false));
                entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 400, 0, false));
            }
            // remove curative items after, in case the effects were already active before
            List<ItemStack> cureItems = new ArrayList<>();
            PotionEffect nausea = entity.getActivePotionEffect(Potion.confusion);
            PotionEffect slowness = entity.getActivePotionEffect(Potion.moveSlowdown);
            PotionEffect blindness = entity.getActivePotionEffect(Potion.blindness);
            PotionEffect tipsy = entity.getActivePotionEffect(this);
            if (nausea != null) nausea.setCurativeItems(cureItems);
            if (slowness != null) slowness.setCurativeItems(cureItems);
            if (blindness != null) blindness.setCurativeItems(cureItems);
            if (tipsy != null) tipsy.setCurativeItems(cureItems);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return (amplifier > 1) && ((duration % 100) == 0);
    }

}
