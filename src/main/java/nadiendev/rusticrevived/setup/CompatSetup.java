package nadiendev.rusticrevived.setup;

import net.neoforged.bus.api.IEventBus;

/**
 * Optional mod integrations (JEI, KubeJS and CraftTweaker load through their own plugin
 * discovery; InvTweaks / Quark / Dynamic Trees hooks go here). Guard every call with
 * {@code ModList.get().isLoaded(...)}.
 */
public final class CompatSetup {
	private CompatSetup() {
	}

	public static void init(IEventBus modBus) {
	}

	public static void commonSetup() {
	}
}
