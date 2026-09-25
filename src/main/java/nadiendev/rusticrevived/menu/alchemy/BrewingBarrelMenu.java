package nadiendev.rusticrevived.menu.alchemy;

import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.CULTURE_IN_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.CULTURE_OUT_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.INPUT_IN_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.INPUT_OUT_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.OUTPUT_IN_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.OUTPUT_OUT_SLOT;
import static nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity.SLOT_COUNT;

import nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Brewing barrel menu (legacy ContainerBrewingBarrel): a container slot above and a result slot
 * below each of the culture, input and output tanks.
 */
public class BrewingBarrelMenu extends AbstractContainerMenu {
	private static final int INVENTORY_START = SLOT_COUNT;
	private static final int HOTBAR_START = INVENTORY_START + 27;
	private static final int INVENTORY_END = HOTBAR_START + 9;

	private final BrewingBarrelBlockEntity barrel;
	private final ContainerData data;

	/** Client side constructor used by the menu type. */
	public BrewingBarrelMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, readBlockEntity(playerInventory, extraData.readBlockPos()),
				new SimpleContainerData(BrewingBarrelBlockEntity.DATA_COUNT));
	}

	public BrewingBarrelMenu(int containerId, Inventory playerInventory, BrewingBarrelBlockEntity barrel, ContainerData data) {
		super(ModMenus.BREWING_BARREL.get(), containerId);
		checkContainerDataCount(data, BrewingBarrelBlockEntity.DATA_COUNT);
		this.barrel = barrel;
		this.data = data;
		AlchemyItems items = barrel.getItems();
		addSlot(new ContainerSlot(items, INPUT_IN_SLOT, 62, 7));
		addSlot(new ContainerSlot(items, OUTPUT_IN_SLOT, 116, 7));
		addSlot(new ContainerSlot(items, CULTURE_IN_SLOT, 26, 15));
		addSlot(new ContainerSlot(items, INPUT_OUT_SLOT, 62, 63));
		addSlot(new ContainerSlot(items, OUTPUT_OUT_SLOT, 116, 63));
		addSlot(new ContainerSlot(items, CULTURE_OUT_SLOT, 26, 55));
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; ++col) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
		addDataSlots(data);
	}

	private static BrewingBarrelBlockEntity readBlockEntity(Inventory playerInventory, BlockPos pos) {
		if (playerInventory.player.level().getBlockEntity(pos) instanceof BrewingBarrelBlockEntity barrel) {
			return barrel;
		}
		throw new IllegalStateException("No brewing barrel at " + pos);
	}

	public BrewingBarrelBlockEntity getBarrel() {
		return barrel;
	}

	public int getBrewTime() {
		return BrewingBarrelBlockEntity.joinChunks(data.get(BrewingBarrelBlockEntity.DATA_BREW_TIME_LOW),
				data.get(BrewingBarrelBlockEntity.DATA_BREW_TIME_HIGH));
	}

	public int getMaxBrewTime() {
		return BrewingBarrelBlockEntity.joinChunks(data.get(BrewingBarrelBlockEntity.DATA_MAX_BREW_TIME_LOW),
				data.get(BrewingBarrelBlockEntity.DATA_MAX_BREW_TIME_HIGH));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index < SLOT_COUNT) {
			if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stack, original);
		} else if (!moveToBarrel(stack)) {
			if (index < HOTBAR_START) {
				if (!moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
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

	/** Empty containers go to the output tank first, booze to the culture tank, other fluids to the input tank. */
	private boolean moveToBarrel(ItemStack stack) {
		if (!BrewingBarrelBlockEntity.isFluidContainer(stack)) {
			return false;
		}
		FluidStack fluid = stack.is(Items.GLASS_BOTTLE) ? FluidStack.EMPTY : FluidUtil.getFirstStackContained(stack);
		if (fluid.isEmpty()) {
			return moveItemStackTo(stack, OUTPUT_IN_SLOT, OUTPUT_IN_SLOT + 1, false)
					|| moveItemStackTo(stack, INPUT_IN_SLOT, INPUT_IN_SLOT + 1, false)
					|| moveItemStackTo(stack, CULTURE_IN_SLOT, CULTURE_IN_SLOT + 1, false);
		}
		if (BrewingBarrelBlockEntity.isBooze(fluid)) {
			return moveItemStackTo(stack, CULTURE_IN_SLOT, CULTURE_IN_SLOT + 1, false);
		}
		return moveItemStackTo(stack, INPUT_IN_SLOT, INPUT_IN_SLOT + 1, false);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(barrel, player);
	}

	/** Barrel slot only accepting what the barrel accepts there (nothing for the result slots). */
	private static final class ContainerSlot extends ResourceHandlerSlot {
		ContainerSlot(AlchemyItems items, int index, int x, int y) {
			super(items, items::set, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return BrewingBarrelBlockEntity.isItemValid(getSlotIndex(), stack);
		}
	}
}
