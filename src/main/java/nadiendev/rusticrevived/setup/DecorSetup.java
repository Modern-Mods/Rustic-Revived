package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.block.WoodVariant;
import nadiendev.rusticrevived.block.decor.RopeDispenseBehavior;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Common (both sides) setup of the decor subsystem: game event listeners, capabilities,
 * dispenser behaviours, flammability... Called from {@link CommonSetup}.
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

		FireBlock fire = (FireBlock) Blocks.FIRE;
		fire.setFlammable(ModBlocks.ROPE.get(), 20, 60);
		ModBlocks.PAINTED_WOOD.values().forEach(block -> fire.setFlammable(block.get(), 5, 20));
		for (WoodVariant wood : WoodVariant.values()) {
			if (wood.isFlammable()) {
				fire.setFlammable(ModBlocks.CHAIRS.get(wood).get(), 5, 20);
				fire.setFlammable(ModBlocks.TABLES.get(wood).get(), 5, 20);
			}
		}
	}
}
