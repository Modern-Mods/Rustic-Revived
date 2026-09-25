package nadiendev.rusticrevived.recipe;

import java.util.Map;
import java.util.function.BooleanSupplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.config.RusticConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe / loot condition enabled by a boolean Rustic config option.
 *
 * <pre>{"type": "rusticrevived:config", "option": "enableSlate"}</pre>
 */
public record ConfigCondition(String option) implements ICondition {

	public static final MapCodec<ConfigCondition> CODEC = Codec.STRING.fieldOf("option").xmap(ConfigCondition::new, ConfigCondition::option);

	/** Option names usable in the condition. */
	public static final Map<String, BooleanSupplier> OPTIONS = Map.ofEntries(
			Map.entry("enableSlate", () -> RusticConfig.COMMON.enableSlate.get()),
			Map.entry("enablePillars", () -> RusticConfig.COMMON.enablePillars.get()),
			Map.entry("enableClayWalls", () -> RusticConfig.COMMON.enableClayWalls.get()),
			Map.entry("enablePaintedWood", () -> RusticConfig.COMMON.enablePaintedWood.get()),
			Map.entry("enableTables", () -> RusticConfig.COMMON.enableTables.get()),
			Map.entry("enableChairs", () -> RusticConfig.COMMON.enableChairs.get()),
			Map.entry("enableLattice", () -> RusticConfig.COMMON.enableLattice.get()),
			Map.entry("enableSilverDecor", () -> RusticConfig.COMMON.enableSilverDecor.get()),
			Map.entry("fleshSmelting", () -> RusticConfig.COMMON.fleshSmelting.get()),
			Map.entry("enableOliveOiling", () -> RusticConfig.COMMON.enableOliveOiling.get()),
			Map.entry("enableBottleEmptying", () -> RusticConfig.COMMON.enableBottleEmptying.get()));

	public static ConfigCondition of(String option) {
		if (!OPTIONS.containsKey(option)) throw new IllegalArgumentException("Unknown Rustic config option " + option);
		return new ConfigCondition(option);
	}

	@Override
	public boolean test(IContext context) {
		BooleanSupplier supplier = OPTIONS.get(option);
		if (supplier == null) return true;
		try {
			return supplier.getAsBoolean();
		} catch (IllegalStateException notLoaded) {
			// config not loaded yet (e.g. during datagen): default to enabled
			return true;
		}
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
