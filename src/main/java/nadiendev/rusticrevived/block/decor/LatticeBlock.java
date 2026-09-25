package nadiendev.rusticrevived.block.decor;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Iron lattice (legacy BlockLattice): thin bars connecting to adjacent lattice, to ropes, chains and grape
 * leaves running towards it, and to blocks offering a center support. Using leaves on it covers it with foliage.
 */
public class LatticeBlock extends Block {
	public static final MapCodec<LatticeBlock> CODEC = simpleCodec(LatticeBlock::new);
	public static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;
	public static final BooleanProperty LEAVES = BooleanProperty.create("leaves");

	private static final VoxelShape CORE = box(6, 6, 6, 10, 10, 10);
	private static final Map<Direction, VoxelShape> BARS = Map.of(
			Direction.NORTH, box(7, 7, 0, 9, 9, 7),
			Direction.SOUTH, box(7, 7, 9, 9, 9, 16),
			Direction.WEST, box(0, 7, 7, 7, 9, 9),
			Direction.EAST, box(9, 7, 7, 16, 9, 9),
			Direction.UP, box(7, 9, 7, 9, 16, 9),
			Direction.DOWN, box(7, 0, 7, 9, 7, 9));

	private final Map<BlockState, VoxelShape> outlines;
	private final Map<BlockState, VoxelShape> collisions;

	public LatticeBlock(Properties properties) {
		super(properties);
		BlockState state = stateDefinition.any().setValue(LEAVES, false);
		for (BooleanProperty connection : CONNECTIONS.values()) {
			state = state.setValue(connection, false);
		}
		registerDefaultState(state);
		outlines = getShapeForEachState(LatticeBlock::outline);
		collisions = getShapeForEachState(LatticeBlock::collision);
	}

	@Override
	protected MapCodec<? extends LatticeBlock> codec() {
		return CODEC;
	}

	/** Single box around the core stretched to every connected side (legacy selection box). */
	private static VoxelShape outline(BlockState state) {
		return box(connected(state, Direction.WEST) ? 0 : 6, connected(state, Direction.DOWN) ? 0 : 6, connected(state, Direction.NORTH) ? 0 : 6,
				connected(state, Direction.EAST) ? 16 : 10, connected(state, Direction.UP) ? 16 : 10, connected(state, Direction.SOUTH) ? 16 : 10);
	}

	/** Core plus one thin bar per connection. */
	private static VoxelShape collision(BlockState state) {
		VoxelShape shape = CORE;
		for (Direction direction : Direction.values()) {
			if (connected(state, direction)) {
				shape = Shapes.or(shape, BARS.get(direction));
			}
		}
		return shape;
	}

	private static boolean connected(BlockState state, Direction direction) {
		return state.getValue(CONNECTIONS.get(direction));
	}

	/** Whether the lattice at {@code pos} connects to its neighbour in {@code direction}. */
	private static boolean connectsTo(BlockGetter level, BlockPos pos, Direction direction) {
		BlockPos neighborPos = pos.relative(direction);
		BlockState neighbor = level.getBlockState(neighborPos);
		if (neighbor.getBlock() instanceof LatticeBlock) {
			return true;
		}
		if (neighbor.is(ModBlocks.GRAPE_LEAVES)) {
			return RopeBaseBlock.axisOf(neighbor) == direction.getAxis();
		}
		if (neighbor.getBlock() instanceof RopeBaseBlock) {
			return neighbor.getValue(RopeBaseBlock.AXIS) == direction.getAxis();
		}
		return !isExceptionForConnection(neighbor) && neighbor.isFaceSturdy(level, neighborPos, direction.getOpposite(), SupportType.CENTER);
	}

	private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			state = state.setValue(CONNECTIONS.get(direction), connectsTo(level, pos, direction));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		return state.setValue(CONNECTIONS.get(direction), connectsTo(level, pos, direction));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hitResult) {
		if (state.getValue(LEAVES) || !(Block.byItem(stack.getItem()) instanceof LeavesBlock leaves)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!level.isClientSide) {
			level.setBlock(pos, state.setValue(LEAVES, true), Block.UPDATE_ALL);
			SoundType sound = leaves.defaultBlockState().getSoundType(level, pos, player);
			level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
			stack.consume(1, player);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return outlines.get(state);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return collisions.get(state);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		BlockState rotated = state;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			rotated = rotated.setValue(CONNECTIONS.get(rotation.rotate(direction)), connected(state, direction));
		}
		return rotated;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState mirrored = state;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			mirrored = mirrored.setValue(CONNECTIONS.get(mirror.mirror(direction)), connected(state, direction));
		}
		return mirrored;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEAVES);
		CONNECTIONS.values().forEach(builder::add);
	}
}
