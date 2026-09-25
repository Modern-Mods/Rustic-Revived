package nadiendev.rusticrevived.world.feature;

import com.mojang.serialization.Codec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Hangs a wild beehive under a tree canopy (legacy WorldGenBeehive). Placed on the
 * {@code MOTION_BLOCKING} heightmap: when the top block is leaves, walks down through the canopy
 * and places the hive in the first free block below it, never in snowy climates.
 */
public class BeehiveFeature extends Feature<NoneFeatureConfiguration> {
	public BeehiveFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos.MutableBlockPos pos = context.origin().below().mutable();
		if (!level.getBlockState(pos).is(BlockTags.LEAVES)) {
			return false;
		}
		while (level.getBlockState(pos).is(BlockTags.LEAVES) && pos.getY() > level.getMinY()) {
			pos.move(Direction.DOWN);
		}
		BlockState replaced = level.getBlockState(pos);
		if (!replaced.canBeReplaced() || !replaced.getFluidState().isEmpty()
				|| level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()) == Biome.Precipitation.SNOW) {
			return false;
		}
		BlockState hive = ModBlocks.BEEHIVE.get().defaultBlockState();
		if (hive.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			hive = hive.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.Plane.HORIZONTAL.getRandomDirection(context.random()));
		}
		level.setBlock(pos, hive, Block.UPDATE_CLIENTS);
		return true;
	}
}
