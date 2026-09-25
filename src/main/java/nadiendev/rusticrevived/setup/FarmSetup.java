package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.compat.dynamictrees.DynamicTreesCompat;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Common (both sides) setup of the farm subsystem: game event listeners, capabilities,
 * dispenser behaviours, flammability... Called from {@link CommonSetup}.
 * <p>
 * Seeds from grass and vines are global loot modifiers and chickens eat Rustic seeds through the
 * {@code minecraft:chicken_food} tag (see {@code FarmData}), so no game event listener is needed.
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
		FireBlock fire = (FireBlock) Blocks.FIRE;
		flammable(fire, 5, 5, ModBlocks.OLIVE_LOG, ModBlocks.IRONWOOD_LOG);
		flammable(fire, 5, 20, ModBlocks.OLIVE_PLANKS, ModBlocks.IRONWOOD_PLANKS, ModBlocks.OLIVE_SLAB, ModBlocks.IRONWOOD_SLAB,
				ModBlocks.OLIVE_STAIRS, ModBlocks.IRONWOOD_STAIRS, ModBlocks.OLIVE_FENCE, ModBlocks.IRONWOOD_FENCE,
				ModBlocks.OLIVE_FENCE_GATE, ModBlocks.IRONWOOD_FENCE_GATE, ModBlocks.CARVED_OLIVE_WOOD, ModBlocks.CARVED_IRONWOOD,
				ModBlocks.CROP_STAKE);
		flammable(fire, 30, 60, ModBlocks.OLIVE_LEAVES, ModBlocks.IRONWOOD_LEAVES, ModBlocks.APPLE_LEAVES, ModBlocks.GRAPE_LEAVES);
		flammable(fire, 15, 40, ModBlocks.STAKE_TIED);
		flammable(fire, 30, 100, ModBlocks.TOMATO_CROP, ModBlocks.CHILI_CROP);
		flammable(fire, 40, 80, ModBlocks.WILDBERRY_BUSH);
		flammable(fire, 20, 100, ModBlocks.GRAPE_STEM);
		flammable(fire, 60, 100, ModBlocks.BLOOD_ORCHID, ModBlocks.CHAMOMILE, ModBlocks.CLOUDSBLUFF, ModBlocks.COHOSH, ModBlocks.CORE_ROOT,
				ModBlocks.GINSENG, ModBlocks.HORSETAIL, ModBlocks.MARSH_MALLOW, ModBlocks.WIND_THISTLE, ModBlocks.VANTA_LILY);
	}

	@SafeVarargs
	private static void flammable(FireBlock fire, int encouragement, int flammability, DeferredBlock<? extends Block>... blocks) {
		for (DeferredBlock<? extends Block> block : blocks) {
			fire.setFlammable(block.get(), encouragement, flammability);
		}
	}
}
