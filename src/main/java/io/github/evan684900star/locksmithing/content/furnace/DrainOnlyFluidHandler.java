package io.github.evan684900star.locksmithing.content.furnace;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Vue d'un réservoir où les tuyaux peuvent seulement pomper. */
public record DrainOnlyFluidHandler(IFluidHandler inner) implements IFluidHandler {
	@Override
	public int getTanks() {
		return inner.getTanks();
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return inner.getFluidInTank(tank);
	}

	@Override
	public int getTankCapacity(int tank) {
		return inner.getTankCapacity(tank);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return inner.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return inner.drain(maxDrain, action);
	}
}
