package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.compat.jei.SyncedRecipes;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * Optional mod integrations (JEI and KubeJS load through their own plugin discovery; InvTweaks /
 * Dynamic Trees hooks go here). Guard every call with {@code ModList.get().isLoaded(...)}.
 * <p>
 * Since 1.21.2 clients no longer receive every recipe: the server always syncs Rustic's machine
 * recipe types (clients may have JEI even when the server has not), and JEI clients keep them in
 * {@link SyncedRecipes}.
 */
public final class CompatSetup {
	private CompatSetup() {
	}

	public static void init(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(CompatSetup::onDatapackSync);
		if (FMLEnvironment.getDist().isClient() && ModList.get().isLoaded("jei")) {
			SyncedRecipes.init();
		}
	}

	public static void commonSetup() {
	}

	private static void onDatapackSync(OnDatapackSyncEvent event) {
		event.sendRecipes(ModRecipes.CRUSHING_TUB.get(), ModRecipes.EVAPORATING_BASIN.get(), ModRecipes.CONDENSER.get(), ModRecipes.BREWING.get());
	}
}
