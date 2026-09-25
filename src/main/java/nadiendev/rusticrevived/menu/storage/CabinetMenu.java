package nadiendev.rusticrevived.menu.storage;

import nadiendev.rusticrevived.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

/**
 * Single cabinet menu, 27 slots (legacy ContainerCabinet).
 */
public class CabinetMenu extends StorageMenu {
	/** Client side constructor used by the menu type. */
	public CabinetMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, new SimpleContainer(3 * 9));
	}

	public CabinetMenu(int containerId, Inventory playerInventory, Container container) {
		super(ModMenus.CABINET.get(), containerId, playerInventory, container, 3, 17, 84);
	}
}
