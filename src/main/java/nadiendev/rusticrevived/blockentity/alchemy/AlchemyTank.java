package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.function.Predicate;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Single tank of an alchemy machine (the 26.1 replacement of NeoForge's FluidTank): a one index
 * fluid resource handler with an optional fluid filter, a change callback and the FluidTank
 * style helpers the machines use (each helper runs in its own root transaction).
 */
public class AlchemyTank extends FluidStacksResourceHandler {
	private final Predicate<FluidStack> filter;
	private final Runnable onChanged;

	public AlchemyTank(int capacity, Predicate<FluidStack> filter, Runnable onChanged) {
		super(1, capacity);
		this.filter = filter;
		this.onChanged = onChanged;
	}

	public AlchemyTank(int capacity, Runnable onChanged) {
		this(capacity, fluid -> true, onChanged);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return filter.test(resource.toStack(1));
	}

	@Override
	protected void onContentsChanged(int index, FluidStack previousContents) {
		onChanged.run();
	}

	/** Whether the tank would accept the fluid (ignoring its current content and room). */
	public boolean isFluidValid(FluidStack fluid) {
		return !fluid.isEmpty() && filter.test(fluid);
	}

	/** A copy of the content. */
	public FluidStack getFluid() {
		return FluidUtil.getStack(this, 0);
	}

	public Fluid getFluidType() {
		return getResource(0).getFluid();
	}

	public int getFluidAmount() {
		return getAmountAsInt(0);
	}

	public int getCapacity() {
		return capacity;
	}

	public int getSpace() {
		return Math.max(0, capacity - getFluidAmount());
	}

	public boolean isEmpty() {
		return getFluidAmount() <= 0;
	}

	/** Replaces the content. */
	public void setFluid(FluidStack fluid) {
		set(0, FluidResource.of(fluid), fluid.isEmpty() ? 0 : fluid.getAmount());
	}

	/** Fills the tank with (part of) the fluid; returns the amount filled. */
	public int fill(FluidStack fluid, boolean simulate) {
		if (fluid.isEmpty()) return 0;
		try (Transaction tx = Transaction.openRoot()) {
			int filled = insert(0, FluidResource.of(fluid), fluid.getAmount(), tx);
			if (!simulate) tx.commit();
			return filled;
		}
	}

	/** Drains up to the given amount of the content; returns what was drained. */
	public FluidStack drain(int amount, boolean simulate) {
		FluidResource resource = getResource(0);
		if (resource.isEmpty() || amount <= 0) return FluidStack.EMPTY;
		try (Transaction tx = Transaction.openRoot()) {
			int drained = extract(0, resource, amount, tx);
			if (!simulate) tx.commit();
			return drained > 0 ? resource.toStack(drained) : FluidStack.EMPTY;
		}
	}
}
