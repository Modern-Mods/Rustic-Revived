package nadiendev.rusticrevived.world;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import nadiendev.rusticrevived.compat.dynamictrees.DynamicTreesCompat;
import nadiendev.rusticrevived.config.RusticConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Named Rustic config values the world generation reads at generation time (through the
 * {@code rusticrevived:config_chance}, {@code config_count} and {@code config_filter} placement
 * modifiers and the {@code rusticrevived:slate_vein} feature), so that changing the config does
 * not require regenerating the data pack.
 */
public final class WorldGenOptions {
	/** Chances (0-1) that a feature tries to generate in a chunk. */
	private static final Map<String, Supplier<ModConfigSpec.DoubleValue>> CHANCES = Map.of(
			"oliveGenChance", () -> RusticConfig.COMMON.oliveGenChance,
			"ironwoodGenChance", () -> RusticConfig.COMMON.ironwoodGenChance,
			"herbGenChance", () -> RusticConfig.COMMON.herbGenChance,
			"wildberryGenChance", () -> RusticConfig.COMMON.wildberryGenChance,
			"beehiveGenChance", () -> RusticConfig.COMMON.beehiveGenChance);

	/** Attempt counts and sizes. */
	private static final Map<String, Supplier<ModConfigSpec.IntValue>> COUNTS = Map.of(
			"maxOliveGenAttempts", () -> RusticConfig.COMMON.maxOliveGenAttempts,
			"maxIronwoodGenAttempts", () -> RusticConfig.COMMON.maxIronwoodGenAttempts,
			"maxHerbAttempts", () -> RusticConfig.COMMON.maxHerbAttempts,
			"maxWildberryAttempts", () -> RusticConfig.COMMON.maxWildberryAttempts,
			"maxBeehiveAttempts", () -> RusticConfig.COMMON.maxBeehiveAttempts,
			"slateVeinsPerChunk", () -> RusticConfig.COMMON.slateVeinsPerChunk,
			"slateVeinSize", () -> RusticConfig.COMMON.slateVeinSize);

	/** Boolean switches. {@code staticTrees} is false when Dynamic Trees generates the trees. */
	private static final Map<String, BooleanSupplier> FLAGS = Map.of(
			"enableSlate", () -> value(RusticConfig.COMMON.enableSlate),
			"netherSlate", () -> value(RusticConfig.COMMON.netherSlate),
			"staticTrees", () -> !DynamicTreesCompat.isActive());

	public static final Codec<String> CHANCE = optionCodec(CHANCES);
	public static final Codec<String> COUNT = optionCodec(COUNTS);
	public static final Codec<String> FLAG = optionCodec(FLAGS);

	private WorldGenOptions() {
	}

	public static double chance(String option) {
		return value(CHANCES.get(option).get());
	}

	public static int count(String option) {
		return value(COUNTS.get(option).get());
	}

	public static boolean flag(String option) {
		return FLAGS.get(option).getAsBoolean();
	}

	/** The config value, or its default while the config is not loaded (datagen). */
	private static <T> T value(ModConfigSpec.ConfigValue<T> config) {
		try {
			return config.get();
		} catch (IllegalStateException notLoaded) {
			return config.getDefault();
		}
	}

	private static Codec<String> optionCodec(Map<String, ?> options) {
		return Codec.STRING.validate(option -> options.containsKey(option) ? DataResult.success(option)
				: DataResult.error(() -> "Unknown Rustic world generation option " + option + ", expected one of " + options.keySet()));
	}
}
