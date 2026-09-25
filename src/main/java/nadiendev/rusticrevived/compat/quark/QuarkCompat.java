package nadiendev.rusticrevived.compat.quark;

import nadiendev.rusticrevived.menu.storage.CabinetMenu;
import nadiendev.rusticrevived.menu.storage.DoubleCabinetMenu;
import nadiendev.rusticrevived.menu.storage.RusticBarrelMenu;
import nadiendev.rusticrevived.menu.storage.VaseMenu;
import nadiendev.rusticrevived.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Quark integration (legacy IChestButtonCallback / IDropoffManager). Quark shows its chest buttons on
 * screens implementing {@code IQuarkButtonAllowed} and transfers items into menus with at least 27
 * container slots, which the storage menus already have. Only call when Quark is loaded: this class
 * links against the Quark API.
 */
public final class QuarkCompat {
	public static final String MODID = "quark";

	private QuarkCompat() {
	}

	public static void registerStorageScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.VASE.get(), QuarkStorageScreen<VaseMenu>::new);
		event.register(ModMenus.BARREL.get(), QuarkStorageScreen<RusticBarrelMenu>::new);
		event.register(ModMenus.CABINET.get(), QuarkStorageScreen<CabinetMenu>::new);
		event.register(ModMenus.CABINET_DOUBLE.get(), QuarkStorageScreen<DoubleCabinetMenu>::new);
	}
}
