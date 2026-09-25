package nadiendev.rusticrevived.menu.storage;

import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Apiary menu (legacy ContainerApiary): a bee slot above a honeycomb slot.
 */
public class ApiaryMenu extends AbstractContainerMenu {
	private static final int BEE_SLOT = 0;
	private static final int HONEYCOMB_SLOT = 1;
	private static final int INVENTORY_START = 2;
	private static final int HOTBAR_START = INVENTORY_START + 27;
	private static final int END = HOTBAR_START + 9;

	private final ContainerLevelAccess access;

	/** Client side constructor used by the menu type. */
	public ApiaryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, new ItemStacksResourceHandler(1), new ItemStacksResourceHandler(1), ContainerLevelAccess.NULL);
	}

	public ApiaryMenu(int containerId, Inventory playerInventory, ItemStacksResourceHandler bees, ItemStacksResourceHandler honeycomb,
			ContainerLevelAccess access) {
		super(ModMenus.APIARY.get(), containerId);
		this.access = access;
		addSlot(new ResourceHandlerSlot(bees, bees::set, 0, 80, 26) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ModItems.BEE.get());
			}
		});
		addSlot(new ResourceHandlerSlot(honeycomb, honeycomb::set, 0, 80, 44) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ModItems.HONEYCOMB.get());
			}
		});
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < INVENTORY_START) {
				if (!moveItemStackTo(stack, INVENTORY_START, END, true)) return ItemStack.EMPTY;
				slot.onQuickCraft(stack, result);
			} else if (stack.is(ModItems.BEE.get())) {
				if (!moveItemStackTo(stack, BEE_SLOT, BEE_SLOT + 1, false)) return ItemStack.EMPTY;
			} else if (stack.is(ModItems.HONEYCOMB.get())) {
				if (!moveItemStackTo(stack, HONEYCOMB_SLOT, HONEYCOMB_SLOT + 1, false)) return ItemStack.EMPTY;
			} else if (index < HOTBAR_START) {
				if (!moveItemStackTo(stack, HOTBAR_START, END, false)) return ItemStack.EMPTY;
			} else if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, stack);
		}
		return result;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.APIARY.get());
	}
}
