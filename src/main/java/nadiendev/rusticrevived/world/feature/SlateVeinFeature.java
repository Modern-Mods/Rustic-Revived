package nadiendev.rusticrevived.world.feature;

import com.mojang.serialization.Codec;

import nadiendev.rusticrevived.world.WorldGenOptions;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/**
 * Slate vein: a vanilla ore vein whose size comes from the {@code slateVeinSize} config value at generation time
 * instead of the configured feature (the configured {@code size} is ignored).
 */
public class SlateVeinFeature extends OreFeature {
	public SlateVeinFeature(Codec<OreConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<OreConfiguration> context) {
		int size = WorldGenOptions.count("slateVeinSize");
		if (size <= 0) {
			return false;
		}
		OreConfiguration config = context.config();
		return super.place(new FeaturePlaceContext<>(context.topFeature(), context.level(), context.chunkGenerator(), context.random(),
				context.origin(), new OreConfiguration(config.targetStates, size, config.discardChanceOnAirExposure)));
	}
}
