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
		if (player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) >= 5 || player.isSneaking() || player.isRiding()) return true;
		if (!world.isRemote) {
			List<?> chairs = world.getEntitiesWithinAABB(EntityChair.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
			if (chairs.isEmpty()) {
				EntityChair chair = new EntityChair(world, x, y, z, getFacing(world, x, y, z));
				world.spawnEntityInWorld(chair);
				player.mountEntity(chair);
			}
		}
		return true;
	}

	public static class EntityChair extends Entity {


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
			return 0.0D;
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
