package nadiendev.rusticrevived.datagen;

import java.util.List;
import java.util.Set;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticLanguageProvider;
import nadiendev.rusticrevived.datagen.providers.RusticLootModifierProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.datagen.providers.RusticTreepackProvider;
import nadiendev.rusticrevived.datagen.providers.RusticWorldGenProvider;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data generation entry point ({@code gradlew runClientData}). Every provider lives in
 * {@code datagen.providers}; the per-subsystem content lives in {@code datagen.providers.parts}.
 * <p>
 * The client data run generates everything (assets and data).
 */
@EventBusSubscriber(modid = RusticRevived.MODID)
public final class DataGenerators {
	private DataGenerators() {
	}

	@SubscribeEvent
	public static void gatherClientData(GatherDataEvent.Client event) {
		// datapack registries first: the other providers look them up
		event.createDatapackRegistryObjects(RusticWorldGenProvider.BUILDER, Set.of(RusticRevived.NAMESPACE));

		// assets
		event.createProvider(RusticModelProvider::new);
		for (String locale : RusticLanguageProvider.LOCALES) {
			event.createProvider(output -> new RusticLanguageProvider(output, locale));
		}

		// data
		event.createProvider(RusticRecipeProvider.Runner::new);
		event.createProvider((output, lookup) -> new LootTableProvider(output, Set.of(),
				List.of(new LootTableProvider.SubProviderEntry(RusticBlockLoot::new, LootContextParamSets.BLOCK)), lookup));
		event.createBlockAndItemTags(RusticTagProviders.Blocks::new, RusticTagProviders.Items::new);
		event.createProvider(RusticTagProviders.Fluids::new);
		event.createProvider(RusticTagProviders.EntityTypes::new);
		event.createProvider(RusticTagProviders.Biomes::new);
		event.createProvider(RusticTagProviders.BannerPatterns::new);
		event.createProvider(RusticDataMapProvider::new);
		event.createProvider(RusticLootModifierProvider::new);
		event.createProvider((output, lookup) -> new AdvancementProvider(output, lookup, List.of(MiscData::advancements)));
		event.createProvider(RusticTreepackProvider::new);
	}
}
