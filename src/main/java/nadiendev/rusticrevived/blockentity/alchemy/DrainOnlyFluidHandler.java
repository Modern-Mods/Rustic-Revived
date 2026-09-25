package nadiendev.rusticrevived.blockentity.alchemy;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Fluid handler view that can be drained but never filled (legacy {@code tank.setCanFill(false)}).
 */
public class DrainOnlyFluidHandler extends DelegatingResourceHandler<FluidResource> {
	public DrainOnlyFluidHandler(ResourceHandler<FluidResource> tank) {
		super(tank);
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return resource.isEmpty() ? super.getCapacityAsLong(index, resource) : 0;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return false;
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int insert(FluidResource resource, int amount, TransactionContext transaction) {
		return 0;
	}
}
