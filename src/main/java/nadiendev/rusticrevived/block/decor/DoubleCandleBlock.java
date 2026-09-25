package nadiendev.rusticrevived.block.decor;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Candle holder with two candles (legacy BlockCandleDouble). Standing on a block its two candles are lined up
 * along the X or Z axis depending on where the player looked; it can also be mounted on walls.
 */
public class DoubleCandleBlock extends Block {
	public static final MapCodec<DoubleCandleBlock> CODEC = simpleCodec(DoubleCandleBlock::new);
	public static final EnumProperty<Mount> FACING = EnumProperty.create("facing", Mount.class);

	private static final VoxelShape STANDING_X_SHAPE = Shapes.box(0.4, 0.0, 0.22, 0.6, 0.9375, 0.78);
	private static final VoxelShape STANDING_Z_SHAPE = Shapes.box(0.22, 0.0, 0.4, 0.78, 0.9375, 0.6);
	private static final VoxelShape NORTH_SHAPE = Shapes.box(0.175, 0.0, 0.6375, 0.825, 0.8, 1.0);
	private static final VoxelShape SOUTH_SHAPE = Shapes.box(0.175, 0.0, 0.0, 0.825, 0.8, 0.3625);
	private static final VoxelShape WEST_SHAPE = Shapes.box(0.6375, 0.0, 0.175, 1.0, 0.8, 0.825);
	private static final VoxelShape EAST_SHAPE = Shapes.box(0.0, 0.0, 0.175, 0.3625, 0.8, 0.825);

	public DoubleCandleBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Mount.UP_X));
	}

	@Override
	protected MapCodec<? extends DoubleCandleBlock> codec() {
		return CODEC;
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction clicked = context.getClickedFace();
		if (clicked == Direction.UP && MountedLightBlock.canMountTorchLike(level, pos, Direction.UP)) {
			boolean lookingAlongZ = context.getPlayer() != null && context.getHorizontalDirection().getAxis() == Direction.Axis.Z;
			return defaultBlockState().setValue(FACING, lookingAlongZ ? Mount.UP_Z : Mount.UP_X);
		}
		if (clicked.getAxis().isHorizontal() && MountedLightBlock.canMountTorchLike(level, pos, clicked)) {
			return defaultBlockState().setValue(FACING, Mount.onWall(clicked));
		}
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (MountedLightBlock.canMountTorchLike(level, pos, direction)) {
				return defaultBlockState().setValue(FACING, Mount.onWall(direction));
			}
		}
		return MountedLightBlock.canMountTorchLike(level, pos, Direction.UP) ? defaultBlockState() : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return MountedLightBlock.canMountTorchLike(level, pos, state.getValue(FACING).facing());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case UP_X -> STANDING_X_SHAPE;
			case UP_Z -> STANDING_Z_SHAPE;
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
		};
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		Mount mount = state.getValue(FACING);
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.7;
		double z = pos.getZ() + 0.5;
		double spread;
		if (mount.isWall()) {
			Direction back = mount.facing().getOpposite();
			x += 0.2609375 * back.getStepX();
			y += 0.25;
			z += 0.2609375 * back.getStepZ();
			spread = 0.23125;
		} else {
			y += 0.33;
			spread = 0.1875;
		}
		// the candles stand side by side, across the axis the holder faces
		boolean alongZ = mount.axis() == Direction.Axis.X;
		double dx = alongZ ? 0.0 : spread;
		double dz = alongZ ? spread : 0.0;
		RusticCandleBlock.flame(level, x + dx, y, z + dz);
		RusticCandleBlock.flame(level, x - dx, y, z - dz);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		Mount mount = state.getValue(FACING);
		if (mount.isWall()) {
			return state.setValue(FACING, Mount.onWall(rotation.rotate(mount.facing())));
		}
		boolean quarterTurn = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
		return quarterTurn ? state.setValue(FACING, mount == Mount.UP_X ? Mount.UP_Z : Mount.UP_X) : state;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		Mount mount = state.getValue(FACING);
		return mount.isWall() ? state.rotate(mirror.getRotation(mount.facing())) : state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** Where a double candle is mounted: standing (candles along Z or X) or on a wall. */
	public enum Mount implements StringRepresentable {
		UP_X("up_x", Direction.UP, Direction.Axis.X),
		UP_Z("up_z", Direction.UP, Direction.Axis.Z),
		NORTH("north", Direction.NORTH, Direction.Axis.Z),
		SOUTH("south", Direction.SOUTH, Direction.Axis.Z),
		WEST("west", Direction.WEST, Direction.Axis.X),
		EAST("east", Direction.EAST, Direction.Axis.X);

		private final String name;
		private final Direction facing;
		private final Direction.Axis axis;

		Mount(String name, Direction facing, Direction.Axis axis) {
			this.name = name;
			this.facing = facing;
			this.axis = axis;
		}

		/** Direction pointing away from the supporting block. */
		public Direction facing() {
			return facing;
		}

		/** Axis the holder faces; its candles are lined up across it. */
		public Direction.Axis axis() {
			return axis;
		}

		public boolean isWall() {
			return facing != Direction.UP;
		}

		public static Mount onWall(Direction facing) {
			return switch (facing) {
				case NORTH -> NORTH;
				case SOUTH -> SOUTH;
				case WEST -> WEST;
				case EAST -> EAST;
				default -> throw new IllegalArgumentException("Not a wall facing: " + facing);
			};
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
