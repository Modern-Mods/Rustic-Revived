package nadiendev.rusticrevived.block.decor;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Light source standing on top of a block or mounted on a wall, like a torch (shared behaviour of the
 * legacy BlockCandle, BlockIronTorch and BlockWoodLantern). {@link #FACING} points away from the
 * supporting block; it pops off when that support disappears.
 */
public abstract class MountedLightBlock extends Block {
	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, direction -> direction != Direction.DOWN);

	private final Map<Direction, VoxelShape> shapes;

	/**
	 * @param shapes outline shape for every {@link #FACING} value
	 */
	protected MountedLightBlock(Properties properties, Map<Direction, VoxelShape> shapes) {
		super(properties);
		this.shapes = shapes;
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected abstract MapCodec<? extends MountedLightBlock> codec();

	/**
	 * Whether a torch-like block can be mounted at {@code pos} facing {@code facing}: on a sturdy wall for
	 * horizontal facings, on anything with a center support (full faces, fences, walls, chains...) when standing.
	 */
	public static boolean canMountTorchLike(LevelReader level, BlockPos pos, Direction facing) {
		BlockPos support = pos.relative(facing.getOpposite());
		if (facing.getAxis().isHorizontal()) {
			return level.getBlockState(support).isFaceSturdy(level, support, facing);
		}
		return facing == Direction.UP && Block.canSupportCenter(level, support, Direction.UP);
	}

	/** Whether the block can stay at {@code pos} with the given facing (legacy canPlaceAt). */
	protected boolean canMount(LevelReader level, BlockPos pos, Direction facing) {
		return canMountTorchLike(level, pos, facing);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clicked = context.getClickedFace();
		Direction preferred = clicked == Direction.DOWN ? Direction.UP : clicked;
		if (canMount(context.getLevel(), context.getClickedPos(), preferred)) {
			return defaultBlockState().setValue(FACING, preferred);
		}
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (canMount(context.getLevel(), context.getClickedPos(), direction)) {
				return defaultBlockState().setValue(FACING, direction);
			}
		}
		return canMount(context.getLevel(), context.getClickedPos(), Direction.UP) ? defaultBlockState() : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return canMount(level, pos, state.getValue(FACING));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING));
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
