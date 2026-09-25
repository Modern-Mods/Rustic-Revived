package nadiendev.rusticrevived.client.farm;

import java.util.Map;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client setup of the farm subsystem: screens, renderers, colors, models, client events.
 * Called from {@link nadiendev.rusticrevived.client.ClientSetup} on the physical client only.
 * <p>
 * Leaves, grape vines and wildberry bushes take the biome foliage colour (legacy IColoredBlock);
 * render types are declared in the block models.
 */
public final class FarmClientSetup {
	private FarmClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(FarmClientSetup::registerBlockColors);
		modBus.addListener(FarmClientSetup::registerItemColors);
		modBus.addListener(FarmClientSetup::modifyBakingResult);
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos)
				: FoliageColor.getDefaultColor(),
				ModBlocks.OLIVE_LEAVES.get(), ModBlocks.IRONWOOD_LEAVES.get(), ModBlocks.APPLE_LEAVES.get(), ModBlocks.GRAPE_LEAVES.get(),
				ModBlocks.WILDBERRY_BUSH.get());
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tintIndex) -> FastColor.ARGB32.opaque(FoliageColor.getDefaultColor()),
				ModBlocks.OLIVE_LEAVES.get(), ModBlocks.IRONWOOD_LEAVES.get(), ModBlocks.APPLE_LEAVES.get(), ModBlocks.WILDBERRY_BUSH.get());
	}

	private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
		if (!RusticConfig.CLIENT.offsetWildberryBushes.get()) {
			return;
		}
		Map<ModelResourceLocation, BakedModel> models = event.getModels();
		for (BlockState state : ModBlocks.WILDBERRY_BUSH.get().getStateDefinition().getPossibleStates()) {
			models.computeIfPresent(BlockModelShaper.stateToModelLocation(state), (location, model) -> new OffsetBushModel(model));
		}
	}
}
