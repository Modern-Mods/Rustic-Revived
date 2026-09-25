package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.block.storage.CabinetBlock;
import nadiendev.rusticrevived.compat.invtweaks.InvTweaksCompat;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/**
 * Common (both sides) setup of the storage subsystem: game event listeners, capabilities,
 * dispenser behaviours, flammability... Called from {@link CommonSetup}.
 */
public final class StorageSetup {
	private StorageSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(StorageSetup::enqueueImc);
	}

	/** Called from {@link RegisterCapabilitiesEvent}. */
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VASE.get(), (vase, side) -> new InvWrapper(vase));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BARREL.get(), (barrel, side) -> new InvWrapper(barrel));
		// a double cabinet exposes both halves (54 slots) from either block
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CABINET.get(),
				(cabinet, side) -> new InvWrapper(CabinetBlock.getContainer(cabinet)));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.APIARY.get(), (apiary, side) -> apiary.getItemHandler(side));
	}

	/** Called during FMLCommonSetupEvent (already inside enqueueWork: not thread safe work is fine). */
	public static void commonSetup() {
	}

	private static void enqueueImc(InterModEnqueueEvent event) {
		if (ModList.get().isLoaded(InvTweaksCompat.MODID)) {
			InvTweaksCompat.sendImc();
		}
	}
}
