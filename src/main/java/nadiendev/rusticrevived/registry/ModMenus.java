package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.alchemy.AdvancedCondenserMenu;
import nadiendev.rusticrevived.menu.alchemy.BrewingBarrelMenu;
import nadiendev.rusticrevived.menu.alchemy.CondenserMenu;
import nadiendev.rusticrevived.menu.storage.ApiaryMenu;
import nadiendev.rusticrevived.menu.storage.CabinetMenu;
import nadiendev.rusticrevived.menu.storage.DoubleCabinetMenu;
import nadiendev.rusticrevived.menu.storage.RusticBarrelMenu;
import nadiendev.rusticrevived.menu.storage.VaseMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu types. Every menu is opened with {@code player.openMenu(provider, buf -> buf.writeBlockPos(pos))}
 * and its client constructor reads that position back.
 */
public final class ModMenus {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, RusticRevived.NAMESPACE);

	public static final DeferredHolder<MenuType<?>, MenuType<VaseMenu>> VASE = register("vase", VaseMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<RusticBarrelMenu>> BARREL = register("barrel", RusticBarrelMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<CabinetMenu>> CABINET = register("cabinet", CabinetMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<DoubleCabinetMenu>> CABINET_DOUBLE = register("cabinet_double", DoubleCabinetMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<ApiaryMenu>> APIARY = register("apiary", ApiaryMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<CondenserMenu>> CONDENSER = register("condenser", CondenserMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<AdvancedCondenserMenu>> CONDENSER_ADVANCED = register("condenser_advanced", AdvancedCondenserMenu::new);
	public static final DeferredHolder<MenuType<?>, MenuType<BrewingBarrelMenu>> BREWING_BARREL = register("brewing_barrel", BrewingBarrelMenu::new);

	private ModMenus() {
	}

	private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> register(String name, IContainerFactory<T> factory) {
		return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
	}
}
