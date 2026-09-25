package nadiendev.rusticrevived.compat.dynamictrees;

import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.tree.species.Species;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Code that talks to Dynamic Trees (legacy DynamicTreesCompat / SaplingReplacer). Only loaded when
 * Dynamic Trees is installed.
 * <p>
 * Registers the {@value RusticRevived#NAMESPACE} registry handler so the tree pack's branches,
 * dynamic leaves, dynamic saplings and seeds get registered. Olive and ironwood saplings are
 * replaced by their dynamic counterparts by Dynamic Trees itself (the species declare them as
 * {@code primitive_sapling}); apple seeds and apple saplings are replaced here by Dynamic Trees'
 * own apple tree.
 */
final class DynamicTreesIntegration {
	private static final ResourceLocation APPLE_SPECIES = ResourceLocation.fromNamespaceAndPath(DynamicTreesCompat.MOD_ID, "apple_oak");

	private DynamicTreesIntegration() {
	}

	static void init(IEventBus modBus) {
		NeoForgeRegistryHandler.setup(RusticRevived.NAMESPACE, modBus);
		NeoForge.EVENT_BUS.addListener(DynamicTreesIntegration::onBlockPlaced);
	}

	private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
		BlockState placed = event.getPlacedBlock();
		if (!placed.is(ModBlocks.APPLE_SEEDS.get()) && !placed.is(ModBlocks.APPLE_SAPLING.get())
				|| !(event.getLevel() instanceof Level level) || level.isClientSide || !DynamicTreesCompat.isActive()) {
			return;
		}
		Species species = Species.findSpecies(APPLE_SPECIES);
		if (!species.isValid()) {
			return;
		}
		BlockPos pos = event.getPos();
		level.removeBlock(pos, false);
		if (!species.plantSapling(level, pos, false)) {
			Block.popResource(level, pos, species.getSeedStack(1));
		}
	}
}
