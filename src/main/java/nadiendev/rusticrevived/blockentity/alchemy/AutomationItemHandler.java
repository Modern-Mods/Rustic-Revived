package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.function.BiPredicate;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A machine inventory as seen by automation: items only go into the slots that accept them,
 * anything can be taken out.
 */
public class AutomationItemHandler implements ResourceHandler<ItemResource> {
	private final ResourceHandler<ItemResource> items;
	private final BiPredicate<Integer, ItemStack> canInsert;

	public AutomationItemHandler(ResourceHandler<ItemResource> items, BiPredicate<Integer, ItemStack> canInsert) {
		this.items = items;
		this.canInsert = canInsert;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public ItemResource getResource(int index) {
		return items.getResource(index);
	}

	@Override
	public long getAmountAsLong(int index) {
		return items.getAmountAsLong(index);
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return resource.isEmpty() || isValid(index, resource) ? items.getCapacityAsLong(index, resource) : 0;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return canInsert.test(index, resource.toStack()) && items.isValid(index, resource);
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return isValid(index, resource) ? items.insert(index, resource, amount, transaction) : 0;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return items.extract(index, resource, amount, transaction);
	}
}
