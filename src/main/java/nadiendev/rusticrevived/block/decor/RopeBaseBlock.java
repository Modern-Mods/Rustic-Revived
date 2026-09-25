package nadiendev.rusticrevived.block.decor;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Climbable line (rope or chain, legacy BlockRopeBase). Vertical lines hang from a support above them,
 * horizontal lines span between two supports. Using the line's own item on it extends the line downwards,
 * and a horizontal line shows a short dangling piece when a vertical line hangs below it ({@link #DANGLE}).
 * Lines are climbable through the {@code minecraft:climbable} tag.
 */
public abstract class RopeBaseBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final BooleanProperty DANGLE = BooleanProperty.create("dangle");
	/** How far below the used block a line can be extended. */
	private static final int MAX_EXTENSION = 64;

	private static final VoxelShape Y_SHAPE = Shapes.box(0.4375, 0.0, 0.4375, 0.5625, 1.0, 0.5625);
	private static final VoxelShape X_SHAPE = Shapes.box(0.0, 0.4375, 0.4375, 1.0, 0.5625, 0.5625);
	private static final VoxelShape Z_SHAPE = Shapes.box(0.4375, 0.4375, 0.0, 0.5625, 0.5625, 1.0);
	private static final VoxelShape X_DANGLE_SHAPE = Shapes.box(0.0, 0.0, 0.4375, 1.0, 0.5625, 0.5625);
	private static final VoxelShape Z_DANGLE_SHAPE = Shapes.box(0.4375, 0.0, 0.0, 0.5625, 0.5625, 1.0);

	protected RopeBaseBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(DANGLE, false));
	}

	@Override
	protected abstract MapCodec<? extends RopeBaseBlock> codec();

	/**
	 * Whether {@code other} (not a line of this block) holds a line of this block through its face {@code face}.
	 *
	 * @param axis axis of the held line
	 */
	protected abstract boolean isSupport(BlockState other, LevelReader level, BlockPos otherPos, Direction face, Direction.Axis axis);

	/**
	 * Whether a new line can be attached at {@code pos} by clicking the face {@code side} of the block behind
	 * it (legacy canPlaceBlockOnSide). A line placed on the ground must hang from something above instead.
	 */
	public boolean canAttach(LevelReader level, BlockPos pos, Direction side) {
		if (side == Direction.UP) {
			return canAttach(level, pos, Direction.DOWN);
		}
		BlockPos otherPos = pos.relative(side.getOpposite());
		BlockState other = level.getBlockState(otherPos);
		if (other.is(this)) {
			return other.getValue(AXIS) == side.getAxis();
		}
		return isSupport(other, level, otherPos, side, side.getAxis());
	}

	/** Whether the line at {@code pos} is held by its neighbour in {@code direction}. Nothing below holds a line. */
	public boolean isSideSupported(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
		if (direction == Direction.DOWN) {
			return false;
		}
		Direction.Axis axis = state.getValue(AXIS);
		BlockPos otherPos = pos.relative(direction);
		BlockState other = level.getBlockState(otherPos);
		if (other.is(this) && ((axis == Direction.Axis.Y && direction.getAxis() == Direction.Axis.Y) || other.getValue(AXIS) == axis)) {
			return true;
		}
		return isSupport(other, level, otherPos, direction.getOpposite(), axis);
	}

	/** Vertical lines need a support above them, horizontal lines one at both ends. */
	public boolean isBlockSupported(LevelReader level, BlockPos pos, BlockState state) {
		return switch (state.getValue(AXIS)) {
			case X -> isSideSupported(level, pos, state, Direction.WEST) && isSideSupported(level, pos, state, Direction.EAST);
			case Y -> isSideSupported(level, pos, state, Direction.UP);
			case Z -> isSideSupported(level, pos, state, Direction.NORTH) && isSideSupported(level, pos, state, Direction.SOUTH);
		};
	}

	/**
	 * Hangs a new vertical line segment below the bottom of the vertical line under {@code pos}.
	 *
	 * @return whether a segment was placed
	 */
	public boolean extendDownwards(Level level, BlockPos pos) {
		BlockPos target = extensionTarget(level, pos);
		if (target == null) {
			return false;
		}
		BlockState placed = defaultBlockState();
		level.setBlock(target, placed, Block.UPDATE_ALL);
		SoundType sound = placed.getSoundType(level, target, null);
		level.playSound(null, target, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		return true;
	}

	/** First free position below the vertical line under {@code pos}, or null when the line can not be extended. */
	@Nullable
	private BlockPos extensionTarget(LevelReader level, BlockPos pos) {
		int offset = 1;
		for (; offset < MAX_EXTENSION && level.getBlockState(pos.below(offset)).is(this); offset++) {
			if (level.getBlockState(pos.below(offset)).getValue(AXIS) != Direction.Axis.Y) {
				return null;
			}
		}
		BlockPos target = pos.below(offset);
		return level.isOutsideBuildHeight(target) || !level.getBlockState(target).canBeReplaced() ? null : target;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hitResult) {
		Direction side = hitResult.getDirection();
		if (!stack.is(asItem()) || canAttach(level, pos.relative(side), side)) {
			// regular placement next to the clicked line
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!isBlockSupported(level, pos, state)) {
			if (!level.isClientSide()) {
				level.destroyBlock(pos, true);
			}
			return InteractionResult.SUCCESS;
		}
		if (extensionTarget(level, pos) == null) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide() && extendDownwards(level, pos)) {
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction side = context.getClickedFace();
		if (!canAttach(context.getLevel(), context.getClickedPos(), side)) {
			return null;
		}
		Direction.Axis axis = side.getAxis();
		return defaultBlockState().setValue(AXIS, axis).setValue(DANGLE, dangles(axis, context.getLevel().getBlockState(context.getClickedPos().below())));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		Direction.Axis axis = state.getValue(AXIS);
		if (direction.getAxis() == axis && !isSideSupported(level, pos, state, direction)
				&& (direction == Direction.UP || !isBlockSupported(level, pos, state))) {
			return Blocks.AIR.defaultBlockState();
		}
		return direction == Direction.DOWN ? state.setValue(DANGLE, dangles(axis, neighborState)) : state;
	}

	/** A horizontal line dangles when a vertical line hangs below it. */
	private static boolean dangles(Direction.Axis axis, BlockState below) {
		return axis != Direction.Axis.Y && below.getBlock() instanceof RopeBaseBlock && below.getValue(AXIS) == Direction.Axis.Y;
	}

	/** Axis of a block exposing an {@code axis} property (e.g. grape leaves growing along rope), or null. */
	@Nullable
	static Direction.Axis axisOf(BlockState state) {
		for (Property<?> property : state.getProperties()) {
			if (property.getName().equals("axis") && property.getValueClass() == Direction.Axis.class) {
				return (Direction.Axis) state.getValue(property);
			}
		}
		return null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(AXIS)) {
			case X -> state.getValue(DANGLE) ? X_DANGLE_SHAPE : X_SHAPE;
			case Y -> Y_SHAPE;
			case Z -> state.getValue(DANGLE) ? Z_DANGLE_SHAPE : Z_SHAPE;
		};
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return RotatedPillarBlock.rotatePillar(state, rotation);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, DANGLE);
	}
}
