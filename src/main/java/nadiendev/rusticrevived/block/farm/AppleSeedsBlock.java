package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Apple seeds: sprout once, then turn into an apple sapling (legacy BlockAppleSeeds).
 */
public class AppleSeedsBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<AppleSeedsBlock> CODEC = simpleCodec(AppleSeedsBlock::new);
	public static final int MAX_AGE = 1;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
	private static final VoxelShape[] SHAPES = { Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 0, 0, 16, 8, 16) };

	public AppleSeedsBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected MapCodec<? extends AppleSeedsBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(AGE)];
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getRawBrightness(pos.above(), 0) >= 9 && CommonHooks.canCropGrow(level, pos, state, random.nextInt(4) == 0)) {
			grow(level, pos, state);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	private void grow(Level level, BlockPos pos, BlockState state) {
		if (state.getValue(AGE) < MAX_AGE) {
			level.setBlock(pos, state.setValue(AGE, MAX_AGE), Block.UPDATE_CLIENTS);
		} else {
			level.setBlock(pos, ModBlocks.APPLE_SAPLING.get().defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		grow(level, pos, state);
	}
}
