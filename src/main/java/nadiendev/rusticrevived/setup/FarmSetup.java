package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.compat.dynamictrees.DynamicTreesCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Common (both sides) setup of the farm subsystem: game event listeners, capabilities,
 * dispenser behaviours, flammability... Called from {@link CommonSetup}.
 * <p>
 * Seeds from grass and vines are global loot modifiers and chickens eat Rustic seeds through the
 * {@code minecraft:chicken_food} tag (see {@code FarmData}), so no game event listener is needed.
 * Flammability is declared by the blocks themselves ({@code getFlammability} /
 * {@code getFireSpreadSpeed}, see {@link nadiendev.rusticrevived.block.farm.FlammableWoodBlocks}):
 * {@code FireBlock#setFlammable} is private in 26.1.
 */
public final class FarmSetup {
	private FarmSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} listeners here. */
	public static void init(IEventBus modBus) {
		DynamicTreesCompat.init(modBus);
	}

	/** Called from {@link RegisterCapabilitiesEvent}. */
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
	}

	/** Called during FMLCommonSetupEvent (already inside enqueueWork: not thread safe work is fine). */
	public static void commonSetup() {
	}
}
