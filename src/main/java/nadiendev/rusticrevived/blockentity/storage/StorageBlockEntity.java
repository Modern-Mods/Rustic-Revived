package nadiendev.rusticrevived.blockentity.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chest-like 27 slot inventory shared by the vase, the barrel and the cabinet (legacy
 * TileEntityLockableLoot subclasses): custom names, locks and loot tables work like a chest.
 */
public abstract class StorageBlockEntity extends RandomizableContainerBlockEntity {
	public static final int SLOTS = 27;
	private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

	protected StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public int getContainerSize() {
		return SLOTS;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		if (!tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, items, registries);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!trySaveLootTable(tag)) {
			ContainerHelper.saveAllItems(tag, items, registries);
		}
	}
}
