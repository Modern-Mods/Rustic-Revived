package nadiendev.rusticrevived.menu.storage;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Chest-like menu shared by the vase, barrel and cabinets (legacy Container{Vase,Barrel,Cabinet,
 * CabinetDouble}): rows of 9 plain container slots followed by the player inventory, so sorting mods
 * (Inventory Tweaks) treat it as a chest.
 */
public abstract class StorageMenu extends AbstractContainerMenu {
	private final Container container;
	private final int rows;

	/**
	 * @param firstRowY y of the first storage row (legacy GUI textures)
	 * @param playerY   y of the first player inventory row
	 */
	protected StorageMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, int rows, int firstRowY,
			int playerY) {
		super(type, containerId);
		checkContainerSize(container, rows * 9);
		this.container = container;
		this.rows = rows;
		container.startOpen(playerInventory.player);

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(container, col + row * 9, 8 + col * 18, firstRowY + row * 18));
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerY + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, playerY + 58));
		}
	}

	public int getRowCount() {
		return rows;
	}

	/** Whether this menu shows {@code target} (alone or as half of a double inventory). */
	public boolean isFor(Container target) {
		return container == target || container instanceof CompoundContainer compound && compound.contains(target);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			int storageSlots = rows * 9;
			if (index < storageSlots) {
				if (!moveItemStackTo(stack, storageSlots, slots.size(), true)) return ItemStack.EMPTY;
			} else if (!moveItemStackTo(stack, 0, storageSlots, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return result;
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
	}
}
