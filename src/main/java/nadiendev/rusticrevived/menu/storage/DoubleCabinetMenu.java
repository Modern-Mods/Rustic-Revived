package nadiendev.rusticrevived.menu.storage;

import nadiendev.rusticrevived.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

/**
 * Double cabinet menu, 54 slots: upper half first (legacy ContainerCabinetDouble).
 */
public class DoubleCabinetMenu extends StorageMenu {
	/** Client side constructor used by the menu type. */
	public DoubleCabinetMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, new SimpleContainer(6 * 9));
	}

	public DoubleCabinetMenu(int containerId, Inventory playerInventory, Container container) {
		super(ModMenus.CABINET_DOUBLE.get(), containerId, playerInventory, container, 6, 18, 140);
	}
}
