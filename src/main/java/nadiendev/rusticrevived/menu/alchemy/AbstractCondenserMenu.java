package nadiendev.rusticrevived.menu.alchemy;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyItems;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Shared layout of the condenser menus (legacy ContainerCondenser / ContainerCondenserAdvanced):
 * result, fuel and bottle slots, the subclass' ingredient slots and the player inventory.
 */
public abstract class AbstractCondenserMenu extends AbstractContainerMenu {
	protected final AbstractCondenserBlockEntity condenser;
	private final ContainerData data;
	private final int machineSlots;
	private final FuelValues fuelValues;

	protected AbstractCondenserMenu(MenuType<?> type, int containerId, Inventory playerInventory, AbstractCondenserBlockEntity condenser,
			ContainerData data) {
		super(type, containerId);
		checkContainerDataCount(data, AbstractCondenserBlockEntity.DATA_COUNT);
		this.condenser = condenser;
		this.data = data;
		this.fuelValues = playerInventory.player.level().fuelValues();
		AlchemyItems items = condenser.getItems();
		addSlot(new ResourceHandlerSlot(items, items::set, AbstractCondenserBlockEntity.SLOT_RESULT, 105, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});
		addSlot(new ResourceHandlerSlot(items, items::set, AbstractCondenserBlockEntity.SLOT_FUEL, 66, 62) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return AbstractCondenserBlockEntity.isFuel(stack, fuelValues);
			}
		});
		addSlot(new ResourceHandlerSlot(items, items::set, AbstractCondenserBlockEntity.SLOT_BOTTLE, 105, 7));
		addIngredientSlots(items);
		this.machineSlots = slots.size();
		addPlayerInventory(playerInventory);
		addDataSlots(data);
	}

	/** Adds the modifier / ingredient slots (from {@link AbstractCondenserBlockEntity#SLOT_INGREDIENTS_START} on). */
	protected abstract void addIngredientSlots(AlchemyItems items);

	/** Reads the block entity a client side menu was opened for. */
	protected static <T extends AbstractCondenserBlockEntity> T readBlockEntity(Inventory playerInventory, BlockPos pos, Class<T> type) {
		if (type.isInstance(playerInventory.player.level().getBlockEntity(pos))) {
			return type.cast(playerInventory.player.level().getBlockEntity(pos));
		}
		throw new IllegalStateException("No condenser at " + pos);
	}

	private void addPlayerInventory(Inventory playerInventory) {
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; ++col) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}

	public AlchemyTank getTank() {
		return condenser.getTank();
	}

	public int getBurnTime() {
		return data.get(AbstractCondenserBlockEntity.DATA_BURN_TIME);
	}

	public int getItemBurnTime() {
		return data.get(AbstractCondenserBlockEntity.DATA_ITEM_BURN_TIME);
	}

	public int getBrewTime() {
		return data.get(AbstractCondenserBlockEntity.DATA_BREW_TIME);
	}

	public int getTotalBrewTime() {
		return data.get(AbstractCondenserBlockEntity.DATA_TOTAL_BREW_TIME);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		int inventoryEnd = machineSlots + 36;
		if (index < machineSlots) {
			if (!moveItemStackTo(stack, machineSlots, inventoryEnd, true)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stack, original);
		} else if (!moveToMachine(stack)) {
			if (index < machineSlots + 27) {
				if (!moveItemStackTo(stack, machineSlots + 27, inventoryEnd, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(stack, machineSlots, machineSlots + 27, false)) {
				return ItemStack.EMPTY;
			}
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == original.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, stack);
		return original;
	}

	private boolean moveToMachine(ItemStack stack) {
		if (stack.is(Items.GLASS_BOTTLE)) {
			return moveItemStackTo(stack, AbstractCondenserBlockEntity.SLOT_BOTTLE, AbstractCondenserBlockEntity.SLOT_BOTTLE + 1, false);
		}
		if (AbstractCondenserBlockEntity.isFuel(stack, fuelValues)) {
			return moveItemStackTo(stack, AbstractCondenserBlockEntity.SLOT_FUEL, AbstractCondenserBlockEntity.SLOT_FUEL + 1, false);
		}
		return moveItemStackTo(stack, AbstractCondenserBlockEntity.SLOT_INGREDIENTS_START, machineSlots, false);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(condenser, player);
	}
}
