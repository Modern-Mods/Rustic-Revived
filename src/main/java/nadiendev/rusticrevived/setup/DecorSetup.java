package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.block.decor.RopeDispenseBehavior;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Common (both sides) setup of the decor subsystem: game event listeners, capabilities,
 * dispenser behaviours... Called from {@link CommonSetup}. Flammability is declared by the blocks themselves
 * ({@code IBlockExtension#getFlammability}).
 */
public final class DecorSetup {
	private DecorSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} listeners here. */
	public static void init(IEventBus modBus) {
	}

	/** Called from {@link RegisterCapabilitiesEvent}. */
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
	}

	/** Called during FMLCommonSetupEvent (already inside enqueueWork: not thread safe work is fine). */
	public static void commonSetup() {
		DispenserBlock.registerBehavior(ModBlocks.ROPE, new RopeDispenseBehavior());
	}
}
