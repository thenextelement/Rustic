package rustic.common.tileentity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** FluidTank with 1.12-style external fill/drain switches, internal fill/drain and a change callback. */
public class RusticTank extends FluidTank {

    protected boolean canFill = true;
    protected boolean canDrain = true;

    public RusticTank(int capacity) {
        super(capacity);
    }

    public RusticTank setCanFill(boolean canFill) {
        this.canFill = canFill;
        return this;
    }

    public RusticTank setCanDrain(boolean canDrain) {
        this.canDrain = canDrain;
        return this;
    }

    public boolean canFill() {
        return canFill;
    }

    public boolean canDrain() {
        return canDrain;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return canFill && fluid != null && (getFluid() == null || getFluid().isFluidEqual(fluid));
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return canFill ? fillInternal(resource, doFill) : 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return canDrain ? drainInternal(maxDrain, doDrain) : null;
    }

    public int fillInternal(FluidStack resource, boolean doFill) {
        int filled = super.fill(resource, doFill);
        if (doFill && filled > 0) onContentsChanged();
        return filled;
    }

    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);
        if (doDrain && drained != null && drained.amount > 0) onContentsChanged();
        return drained;
    }

    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        if (resource == null || getFluid() == null || !resource.isFluidEqual(getFluid())) return null;
        return drainInternal(resource.amount, doDrain);
    }

    public void setFluidAndNotify(FluidStack fluid) {
        setFluid(fluid);
        onContentsChanged();
    }

    protected void onContentsChanged() {}

}
