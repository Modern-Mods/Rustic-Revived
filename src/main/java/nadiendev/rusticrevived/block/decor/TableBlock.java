package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wooden table (legacy BlockTable). Adjacent tables of any wood join into one big table: a corner only has a
 * leg when neither of its two sides touches another table. Only the table top has a shape.
 */
public class TableBlock extends Block {
	public static final MapCodec<TableBlock> CODEC = simpleCodec(TableBlock::new);
	public static final BooleanProperty NW = BooleanProperty.create("nw");
	public static final BooleanProperty NE = BooleanProperty.create("ne");
	public static final BooleanProperty SE = BooleanProperty.create("se");
	public static final BooleanProperty SW = BooleanProperty.create("sw");

	private static final VoxelShape SHAPE = Shapes.box(0.0, 0.875, 0.0, 1.0, 1.0, 1.0);

	public TableBlock(Properties properties) {
		super(properties);
		registerDefaultState(legs(stateDefinition.any(), true, true, true, true));
	}

	@Override
	protected MapCodec<? extends TableBlock> codec() {
		return CODEC;
	}

	private static BlockState withLegs(BlockState state, BlockGetter level, BlockPos pos) {
		boolean north = level.getBlockState(pos.north()).getBlock() instanceof TableBlock;
		boolean south = level.getBlockState(pos.south()).getBlock() instanceof TableBlock;
		boolean east = level.getBlockState(pos.east()).getBlock() instanceof TableBlock;
		boolean west = level.getBlockState(pos.west()).getBlock() instanceof TableBlock;
		return legs(state, !north && !west, !north && !east, !south && !east, !south && !west);
	}

	private static BlockState legs(BlockState state, boolean nw, boolean ne, boolean se, boolean sw) {
		return state.setValue(NW, nw).setValue(NE, ne).setValue(SE, se).setValue(SW, sw);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withLegs(defaultBlockState(), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		return direction.getAxis().isHorizontal() ? withLegs(state, level, pos) : state;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		boolean nw = state.getValue(NW);
		boolean ne = state.getValue(NE);
		boolean se = state.getValue(SE);
		boolean sw = state.getValue(SW);
		return switch (rotation) {
			case CLOCKWISE_90 -> legs(state, sw, nw, ne, se);
			case CLOCKWISE_180 -> legs(state, se, sw, nw, ne);
			case COUNTERCLOCKWISE_90 -> legs(state, ne, se, sw, nw);
			case NONE -> state;
		};
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		boolean nw = state.getValue(NW);
		boolean ne = state.getValue(NE);
		boolean se = state.getValue(SE);
		boolean sw = state.getValue(SW);
		return switch (mirror) {
			case LEFT_RIGHT -> legs(state, sw, se, ne, nw); // swaps north and south
			case FRONT_BACK -> legs(state, ne, nw, sw, se); // swaps east and west
			case NONE -> state;
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NW, NE, SE, SW);
	}
}
