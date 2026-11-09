package rustic.common.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class PotionBlazingTrail extends PotionBase {

    protected PotionBlazingTrail(int id) {
        super(id, false, 16738816, "blazing_trail");
        setIconIndex(2, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        World world = entity.worldObj;
        int x = MathHelper.floor_double(entity.posX), y = MathHelper.floor_double(entity.posY),
            z = MathHelper.floor_double(entity.posZ);
        if (!world.isRemote && entity.onGround
            && world.isAirBlock(x, y, z)
            && Blocks.fire.canPlaceBlockAt(world, x, y, z)) {
            world.setBlock(x, y, z, Blocks.fire, 0, 3);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return (duration % 10) == 0;
    }

}
