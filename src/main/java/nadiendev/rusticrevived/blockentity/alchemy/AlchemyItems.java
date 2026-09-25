package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.function.IntConsumer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Inventory of an alchemy machine (the 26.1 replacement of NeoForge's ItemStackHandler): an item
 * resource handler with a change callback and the stack based helpers the machines use (each
 * helper runs in its own root transaction).
 */
public class AlchemyItems extends ItemStacksResourceHandler {
	private final IntConsumer onChanged;

	public AlchemyItems(int size, IntConsumer onChanged) {
		super(size);
		this.onChanged = onChanged;
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		onChanged.accept(index);
	}

	/** A copy of the stack in the slot. */
	public ItemStack getStackInSlot(int slot) {
		return ItemUtil.getStack(this, slot);
	}

	public void setStackInSlot(int slot, ItemStack stack) {
		set(slot, ItemResource.of(stack), stack.getCount());
	}

	/** Inserts the stack into the slot; returns what did not fit. */
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) return ItemStack.EMPTY;
		try (Transaction tx = Transaction.openRoot()) {
			int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), tx);
			if (!simulate) tx.commit();
			return stack.copyWithCount(stack.getCount() - inserted);
		}
	}

	/** Extracts up to the given amount from the slot; returns what was extracted. */
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemResource resource = getResource(slot);
		if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
		try (Transaction tx = Transaction.openRoot()) {
			int extracted = extract(slot, resource, amount, tx);
			if (!simulate) tx.commit();
			return resource.toStack(extracted);
		}
	}
}
