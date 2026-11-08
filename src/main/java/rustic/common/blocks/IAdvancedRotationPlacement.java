package rustic.common.blocks;

import net.minecraftforge.common.util.ForgeDirection;

public interface IAdvancedRotationPlacement {

	/**
	 * Picks a horizontal facing from the clicked side and hit position: clicking a wall faces away from it, clicking a
	 * floor/ceiling faces towards the nearest edge.
	 */
	default ForgeDirection getFacingForAdvancedRotationPlacement(int side, float hitX, float hitY, float hitZ) {
		ForgeDirection facing = ForgeDirection.getOrientation(side);
		if (facing.offsetY != 0) {
			float hitXFromCenter = hitX - 0.5F;
			float hitZFromCenter = hitZ - 0.5F;
			if (Math.abs(hitXFromCenter) >= Math.abs(hitZFromCenter)) {
				return (hitXFromCenter > 0) ? ForgeDirection.EAST : ForgeDirection.WEST;
			}
			return (hitZFromCenter > 0) ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
		}
		return facing.getOpposite();
	}

}
