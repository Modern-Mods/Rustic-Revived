package nadiendev.rusticrevived.datagen.providers;

import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Datapack registry entries: configured / placed features, biome modifiers, damage types and
 * banner patterns (generated through {@code GatherDataEvent#createDatapackRegistryObjects}).
 */
public final class RusticWorldGenProvider {

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, FarmData::configuredFeatures)
			.add(Registries.PLACED_FEATURE, FarmData::placedFeatures)
			.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, FarmData::biomeModifiers)
			.add(Registries.DAMAGE_TYPE, MiscData::damageTypes)
			.add(Registries.BANNER_PATTERN, MiscData::bannerPatterns);

	private RusticWorldGenProvider() {
	}
}
