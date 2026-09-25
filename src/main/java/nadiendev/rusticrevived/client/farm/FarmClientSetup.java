package nadiendev.rusticrevived.client.farm;

import java.util.List;
import java.util.Map;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client setup of the farm subsystem: screens, renderers, colors, models, client events.
 * Called from {@link nadiendev.rusticrevived.client.ClientSetup} on the physical client only.
 * <p>
 * Leaves, grape vines and wildberry bushes take the biome foliage colour (legacy IColoredBlock);
 * their items use a constant foliage tint declared in their client item definitions
 * ({@code FarmData}). Render layers come from the textures' transparency (26.1).
 */
public final class FarmClientSetup {
	private FarmClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(FarmClientSetup::registerBlockTints);
		modBus.addListener(FarmClientSetup::modifyBakingResult);
	}

	private static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
		event.register(List.of(BlockTintSources.foliage()), ModBlocks.OLIVE_LEAVES.get(), ModBlocks.IRONWOOD_LEAVES.get(),
				ModBlocks.APPLE_LEAVES.get(), ModBlocks.GRAPE_LEAVES.get(), ModBlocks.WILDBERRY_BUSH.get());
	}

	private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
		if (!RusticConfig.CLIENT.offsetWildberryBushes.get()) {
			return;
		}
		Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
		for (BlockState state : ModBlocks.WILDBERRY_BUSH.get().getStateDefinition().getPossibleStates()) {
			models.computeIfPresent(state, (key, model) -> new OffsetBushModel(model));
		}
	}
}
