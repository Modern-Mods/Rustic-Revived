package nadiendev.rusticrevived.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModWorldGen;
import nadiendev.rusticrevived.world.WorldGenOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Passes when a Rustic boolean config option (or derived switch such as {@code staticTrees}) has
 * the expected value, read at generation time.
 *
 * <pre>{"type": "rusticrevived:config_filter", "option": "netherSlate", "expected": false}</pre>
 */
public class ConfigFilterPlacement extends PlacementFilter {
	public static final MapCodec<ConfigFilterPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			WorldGenOptions.FLAG.fieldOf("option").forGetter(placement -> placement.option),
			Codec.BOOL.optionalFieldOf("expected", true).forGetter(placement -> placement.expected)
	).apply(i, ConfigFilterPlacement::new));

	private final String option;
	private final boolean expected;

	public ConfigFilterPlacement(String option, boolean expected) {
		this.option = option;
		this.expected = expected;
	}

	@Override
	protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
		return WorldGenOptions.flag(option) == expected;
	}

	@Override
	public PlacementModifierType<?> type() {
		return ModWorldGen.CONFIG_FILTER.get();
	}
}
