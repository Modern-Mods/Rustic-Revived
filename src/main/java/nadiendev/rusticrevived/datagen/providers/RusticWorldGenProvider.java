package nadiendev.rusticrevived.datagen.providers;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Datapack registry entries: configured / placed features, biome modifiers, damage types and
 * banner patterns.
 */
public class RusticWorldGenProvider extends DatapackBuiltinEntriesProvider {

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, FarmData::configuredFeatures)
			.add(Registries.PLACED_FEATURE, FarmData::placedFeatures)
			.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, FarmData::biomeModifiers)
			.add(Registries.DAMAGE_TYPE, MiscData::damageTypes)
			.add(Registries.BANNER_PATTERN, MiscData::bannerPatterns);

	public RusticWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of("minecraft", RusticRevived.NAMESPACE));
	}
}
