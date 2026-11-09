package rustic.common.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.common.network.MessageDismountChair;
import rustic.common.network.PacketHandler;
import rustic.common.util.Facing;

public class BlockChair extends BlockRotatable {

    public BlockChair(String type) {
        super(Material.wood, "chair_" + type);
        setHardness(1F);
        setStepSound(soundTypeWood);
        setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.125F, 0.875F);
        Blocks.fire.setFireInfo(this, 5, 20);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) >= 5 || player.isSneaking() || player.isRiding())
            return true;
        if (!world.isRemote) {
            List<?> chairs = world
                .getEntitiesWithinAABB(EntityChair.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
            if (chairs.isEmpty()) {
                EntityChair chair = new EntityChair(world, x, y, z, getFacing(world, x, y, z));
                chair.pendingRider = player; // mounted a few ticks later, see EntityChair.onUpdate
                world.spawnEntityInWorld(chair);
            }
        }
        return true;
    }

    public static class EntityChair extends Entity {

        /**
         * Player to seat once the client knows this entity. FML sends modded entity spawns separately from vanilla
         * packets, so mounting in the same tick can reach the client before the chair exists there; the client then
         * drops the attach and the player never appears seated.
         */
        EntityPlayer pendingRider;
        private static final int MOUNT_DELAY = 3;

        public EntityChair(World world) {
            super(world);
            setSize(0F, 0F);
            noClip = true;
        }

        public EntityChair(World world, int x, int y, int z, ForgeDirection facing) {
            this(world);
            this.rotationYaw = Facing.horizontalAngle(facing);
            double xOffset = facing.offsetX * -0.125;
            double zOffset = facing.offsetZ * -0.125;
            setPosition(x + 0.5 + xOffset, y + 0.4, z + 0.5 + zOffset);
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
            if (!(worldObj.getBlock(x, y, z) instanceof BlockChair)) {
                setDead();
                return;
            }
            if (!worldObj.isRemote && pendingRider != null) {
                if (ticksExisted < MOUNT_DELAY) return;
                EntityPlayer rider = pendingRider;
                pendingRider = null;
                if (!rider.isDead && rider.ridingEntity == null && rider.getDistanceSq(posX, posY, posZ) < 9) {
                    rider.mountEntity(this);
                    // move the player onto the seat now (1.12 did this with setPositionAndUpdate); otherwise the
                    // distance check below sees the old position and unseats anyone who clicked from > ~1.4 blocks
                    updateRiderPosition();
                }
            }
            if (riddenByEntity == null) {
                if (!worldObj.isRemote) setDead();
                return;
            }
            if (!worldObj.isRemote) {
                if (riddenByEntity.isSneaking() || riddenByEntity.getDistanceSq(posX, posY, posZ) >= 2) {
                    riddenByEntity.mountEntity(null);
                    setDead();
                }
            }
        }

        @Override
        public double getMountedYOffset() {
            // a 1.7.10 rider sits at getYOffset() = -0.5 below the mount point, 1.12's at -0.35: lift the seat by the
            // difference so the player sits on the chair at the same height as in 1.12
            return 0.15D;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean canAttackWithItem() {
            return false;
        }

        @Override
        protected void entityInit() {}

        @Override
        public void setDead() {
            super.setDead();
            if (worldObj.isRemote) {
                PacketHandler.INSTANCE.sendToServer(new MessageDismountChair());
            }
        }

        @Override
        protected void readEntityFromNBT(NBTTagCompound compound) {}

        @Override
        protected void writeEntityToNBT(NBTTagCompound compound) {}
    }

}
