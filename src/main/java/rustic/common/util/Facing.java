package rustic.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Helpers that mirror 1.12's {@code EnumFacing} horizontal conventions on top of {@link ForgeDirection}, so that
 * metadata layouts stay the same as in the 1.12 version of the mod.
 */
public final class Facing {

	/** 1.12 horizontal index order: south, west, north, east. */
	public static final ForgeDirection[] HORIZONTALS = { ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.NORTH,
			ForgeDirection.EAST };

	private Facing() {}

	public static ForgeDirection fromHorizontalIndex(int index) {
		return HORIZONTALS[Math.abs(index % 4)];
	}

	public static int horizontalIndex(ForgeDirection dir) {
		switch (dir) {
			case SOUTH: return 0;
			case WEST: return 1;
			case NORTH: return 2;
			case EAST: return 3;
			default: return -1;
		}
	}

	/** The horizontal direction the entity is looking towards. */
	public static ForgeDirection horizontalFacing(Entity entity) {
		return fromHorizontalIndex(MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3);
	}

	/** Clockwise seen from above (north -> east), like 1.12's {@code EnumFacing.rotateY()}. */
	public static ForgeDirection rotateY(ForgeDirection dir) {
		return dir.getRotation(ForgeDirection.UP);
	}

	public static ForgeDirection rotateYCCW(ForgeDirection dir) {
		return dir.getRotation(ForgeDirection.DOWN);
	}

	public static float horizontalAngle(ForgeDirection dir) {
		return horizontalIndex(dir) * 90F;
	}

	public static String name(ForgeDirection dir) {
		return dir.name().toLowerCase(java.util.Locale.ROOT);
	}

	/** The direction of an entity looking at a side or angle, used for 6-way placement (like pistons). */
	public static ForgeDirection fromSide(int side) {
		return ForgeDirection.getOrientation(side);
	}

}
