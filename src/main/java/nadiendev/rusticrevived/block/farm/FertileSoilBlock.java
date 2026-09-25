package nadiendev.rusticrevived.block.farm;

import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.NetherFungusBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/**
 * Fertile soil (legacy BlockFertileSoil): an always hydrated, untrampleable farmland that sustains
 * every land plant except nether plants (crops, saplings, flowers, herbs, cacti, sugar cane without
 * water...) and speeds up crop growth like wet farmland.
 */
public class FertileSoilBlock extends Block {
	public static final MapCodec<FertileSoilBlock> CODEC = simpleCodec(FertileSoilBlock::new);

	public FertileSoilBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends FertileSoilBlock> codec() {
		return CODEC;
	}

	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
		if (facing != Direction.UP || !plant.getFluidState().isEmpty() || plant.getBlock() instanceof LilyPadBlock
				|| plant.getBlock() instanceof NetherWartBlock || plant.getBlock() instanceof NetherFungusBlock
				|| plant.getBlock() instanceof HerbBlock herb && herb.getPlantType() == HerbBlock.PlantType.NETHER) {
			return TriState.FALSE;
		}
		return TriState.TRUE;
	}

	@Override
	public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	/** Trees growing on fertile soil keep it instead of turning it into dirt. */
	@Override
	public boolean onTreeGrow(BlockState state, WorldGenLevel level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource,
			BlockPos pos, TreeConfiguration config) {
		return true;
	}
}
