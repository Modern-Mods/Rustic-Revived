package nadiendev.rusticrevived.block.farm;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.block.decor.LatticeBlock;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Grape vines growing along a horizontal rope (legacy BlockGrapeLeaves).
 * <p>
 * The vine right above the grape stem ({@code distance=0}) spreads one block along the rope; the
 * outer vines ({@code distance=1}) grow grapes when there is air below them. Grapes are harvested
 * by right-clicking; breaking a vine gives the rope back.
 */
public class GrapeLeavesBlock extends Block implements BonemealableBlock {
	public static final MapCodec<GrapeLeavesBlock> CODEC = simpleCodec(GrapeLeavesBlock::new);
	/** Horizontal axis of the rope the vine grows on ({@link BlockStateProperties#AXIS}, shared with rope blocks; never Y). */
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	/** 0 for the vine above the stem, 1 for the vines it spread to. */
	public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 1);
	public static final BooleanProperty GRAPES = BooleanProperty.create("grapes");
	private static final VoxelShape BRANCH_X = Block.box(0, 3, 3, 16, 13, 13);
	private static final VoxelShape BRANCH_Z = Block.box(3, 3, 0, 13, 13, 16);

	public GrapeLeavesBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(DISTANCE, 0).setValue(GRAPES, false));
	}

	@Override
	protected MapCodec<? extends GrapeLeavesBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, DISTANCE, GRAPES);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(DISTANCE) == 0) {
			return Shapes.block();
		}
		return state.getValue(AXIS) == Direction.Axis.X ? BRANCH_X : BRANCH_Z;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(ModItems.GRAPES.get());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation != Rotation.CLOCKWISE_90 && rotation != Rotation.COUNTERCLOCKWISE_90) {
			return state;
		}
		return switch (state.getValue(AXIS)) {
			case X -> state.setValue(AXIS, Direction.Axis.Z);
			case Z -> state.setValue(AXIS, Direction.Axis.X);
			case Y -> state;
		};
	}

	// ---------------------------------------------------------------- support

	/** Vines need something to hold on to at both ends: vines, rope, a tied stake, lattice or a solid face. */
	public static boolean isSupported(BlockGetter level, BlockPos pos, BlockState state) {
		Direction.Axis axis = state.getValue(AXIS);
		return isSideSupported(level, pos, axis, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE))
				&& isSideSupported(level, pos, axis, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE));
	}

	private static boolean isSideSupported(BlockGetter level, BlockPos pos, Direction.Axis axis, Direction side) {
		BlockPos neighborPos = pos.relative(side);
		BlockState neighbor = level.getBlockState(neighborPos);
		return neighbor.getBlock() instanceof GrapeLeavesBlock && neighbor.getValue(AXIS) == axis
				|| FarmHelper.horizontalRopeAxis(neighbor).map(ropeAxis -> ropeAxis == axis).orElse(false)
				|| neighbor.getBlock() instanceof StakeTiedBlock
				|| neighbor.getBlock() instanceof LatticeBlock
				|| neighbor.isFaceSturdy(level, neighborPos, side.getOpposite());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction.getAxis() == state.getValue(AXIS) && !isSupported(level, pos, state)) {
			ticks.scheduleTick(pos, this, 1);
		}
		return state;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!isSupported(level, pos, state)) {
			collapse(state, level, pos);
		}
	}

	/** Drops the grapes and leaves the bare rope, which checks its own support. */
	private static void collapse(BlockState state, Level level, BlockPos pos) {
		dropResources(state, level, pos);
		level.setBlock(pos, FarmHelper.rope(state.getValue(AXIS)), Block.UPDATE_ALL);
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest,
			FluidState fluid) {
		return level.setBlock(pos, FarmHelper.rope(state.getValue(AXIS)), level.isClientSide() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
	}

	@Override
	public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
		level.setBlock(pos, FarmHelper.rope(state.getValue(AXIS)), Block.UPDATE_ALL);
		wasExploded(level, pos, explosion);
	}

	// ---------------------------------------------------------------- growth

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!isSupported(level, pos, state)) {
			collapse(state, level, pos);
			return;
		}
		if (level.getRawBrightness(pos.above(), 0) < 9) {
			return;
		}
		if (canGrowGrapes(level, pos, state)) {
			if (CommonHooks.canCropGrow(level, pos, state, random.nextInt(36) == 0)) {
				level.setBlock(pos, state.setValue(GRAPES, true), Block.UPDATE_ALL);
				CommonHooks.fireCropGrowPost(level, pos, state);
			}
		} else if (!spreadTargets(level, pos, state).isEmpty() && CommonHooks.canCropGrow(level, pos, state, random.nextInt(31) == 0)) {
			spread(level, pos, state, random);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	private static boolean canGrowGrapes(BlockGetter level, BlockPos pos, BlockState state) {
		return state.getValue(DISTANCE) > 0 && !state.getValue(GRAPES) && level.getBlockState(pos.below()).isAir();
	}

	/** Ropes along the vine's axis the central vine can spread onto. */
	private static List<BlockPos> spreadTargets(BlockGetter level, BlockPos pos, BlockState state) {
		List<BlockPos> targets = new ArrayList<>();
		if (state.getValue(DISTANCE) == 0) {
			Direction.Axis axis = state.getValue(AXIS);
			for (Direction.AxisDirection sign : Direction.AxisDirection.values()) {
				BlockPos target = pos.relative(Direction.fromAxisAndDirection(axis, sign));
				if (FarmHelper.horizontalRopeAxis(level.getBlockState(target)).map(ropeAxis -> ropeAxis == axis).orElse(false)) {
					targets.add(target);
				}
			}
		}
		return targets;
	}

	private void spread(Level level, BlockPos pos, BlockState state, RandomSource random) {
		List<BlockPos> targets = spreadTargets(level, pos, state);
		if (!targets.isEmpty()) {
			BlockPos target = targets.get(random.nextInt(targets.size()));
			level.setBlock(target, defaultBlockState().setValue(AXIS, state.getValue(AXIS)).setValue(DISTANCE, 1), Block.UPDATE_ALL);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return state.getValue(DISTANCE) > 0 ? canGrowGrapes(level, pos, state) : !spreadTargets(level, pos, state).isEmpty();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		if (state.getValue(DISTANCE) > 0) {
			level.setBlock(pos, state.setValue(GRAPES, true), Block.UPDATE_ALL);
		} else {
			spread(level, pos, state, random);
		}
	}

	// ---------------------------------------------------------------- harvest

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!state.getValue(GRAPES)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(GRAPES, false), Block.UPDATE_ALL);
			FarmHelper.giveOrDrop(player, level, pos.relative(hitResult.getDirection()),
					new ItemStack(ModItems.GRAPES.get(), level.getRandom().nextInt(2) + 1));
		}
		return InteractionResult.SUCCESS;
	}

	/** Burns like vanilla leaves (FireBlock#setFlammable is private in 26.1). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 60;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 30;
	}
}
