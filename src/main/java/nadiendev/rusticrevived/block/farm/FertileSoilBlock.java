package nadiendev.rusticrevived.block.farm;

import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.neoforged.neoforge.common.util.TriState;

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
		if (facing != Direction.UP || !plant.getFluidState().isEmpty() || plant.getBlock() instanceof WaterlilyBlock
				|| plant.getBlock() instanceof NetherWartBlock || plant.getBlock() instanceof FungusBlock
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
	public boolean onTreeGrow(BlockState state, LevelReader level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource,
			BlockPos pos, TreeConfiguration config) {
		return true;
	}
}
