package nadiendev.rusticrevived.compat.quark;

import nadiendev.rusticrevived.client.storage.StorageScreen;
import nadiendev.rusticrevived.menu.storage.StorageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.violetmoon.quark.api.IQuarkButtonAllowed;

/**
 * Storage screen marked with Quark's {@link IQuarkButtonAllowed} so Quark adds its chest buttons
 * (sort, deposit, restock, search) to modded screens, which it otherwise only does for whitelisted
 * classes. Only loaded when Quark is present (see {@link QuarkCompat}).
 */
public class QuarkStorageScreen<T extends StorageMenu> extends StorageScreen<T> implements IQuarkButtonAllowed {
	public QuarkStorageScreen(T menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
}
