package rustic.common.blocks.fluids;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidDrinkable extends FluidRustic {

	public FluidDrinkable(String fluidName, String still, String flowing) {
		super(fluidName, still, flowing);
	}
	
	public abstract void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid);

}
