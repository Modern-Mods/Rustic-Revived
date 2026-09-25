package nadiendev.rusticrevived.blockentity.alchemy;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Fluid handler view that can be drained but never filled (legacy {@code tank.setCanFill(false)}).
 */
public record DrainOnlyFluidHandler(IFluidHandler tank) implements IFluidHandler {
	@Override
	public int getTanks() {
		return tank.getTanks();
	}

	@Override
	public FluidStack getFluidInTank(int index) {
		return tank.getFluidInTank(index);
	}

	@Override
	public int getTankCapacity(int index) {
		return tank.getTankCapacity(index);
	}

	@Override
	public boolean isFluidValid(int index, FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return tank.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return tank.drain(maxDrain, action);
	}
}
