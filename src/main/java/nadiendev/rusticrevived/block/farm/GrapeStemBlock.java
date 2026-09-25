package nadiendev.rusticrevived.block.farm;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Grape stem, planted from grape seeds on farmland or fertile soil (legacy BlockGrapeStem). Once
 * fully grown it turns a horizontal rope right above it into grape vines ({@link GrapeLeavesBlock}).
 */
public class GrapeStemBlock extends Block implements BonemealableBlock {
	public static final MapCodec<GrapeStemBlock> CODEC = simpleCodec(GrapeStemBlock::new);
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape[] SHAPES = {
			Block.box(6, 0, 6, 10, 4, 10),
			Block.box(6, 0, 6, 10, 8, 10),
			Block.box(6, 0, 6, 10, 16, 10),
			Block.box(6, 0, 6, 10, 16, 10) };

	public GrapeStemBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected MapCodec<? extends GrapeStemBlock> codec() {
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
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos soilPos = pos.below();
		return FarmHelper.isCropSoil(level.getBlockState(soilPos), level, soilPos, state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	/** Axis of the horizontal rope right above the stem, if any. */
	private static Optional<Direction.Axis> ropeAbove(BlockGetter level, BlockPos pos) {
		return FarmHelper.horizontalRopeAxis(level.getBlockState(pos.above()));
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getRawBrightness(pos.above(), 0) < 9 || !canGrow(level, pos, state)) {
			return;
		}
		if (CommonHooks.canCropGrow(level, pos, state, random.nextInt(4) == 0)) {
			grow(level, pos, state, state.getValue(AGE) + 1);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	private static boolean canGrow(BlockGetter level, BlockPos pos, BlockState state) {
		return state.getValue(AGE) < MAX_AGE || ropeAbove(level, pos).isPresent();
	}

	/** Ages the stem up to {@code age}, or grows the vine onto the rope when already fully grown. */
	private static void grow(Level level, BlockPos pos, BlockState state, int age) {
		if (state.getValue(AGE) < MAX_AGE) {
			level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, age)), Block.UPDATE_CLIENTS);
		} else {
			ropeAbove(level, pos).ifPresent(axis -> level.setBlock(pos.above(), ModBlocks.GRAPE_LEAVES.get().defaultBlockState()
					.setValue(GrapeLeavesBlock.AXIS, axis).setValue(GrapeLeavesBlock.DISTANCE, 0), Block.UPDATE_ALL));
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return canGrow(level, pos, state);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		grow(level, pos, state, state.getValue(AGE) + Mth.nextInt(random, 2, 5));
	}
}
