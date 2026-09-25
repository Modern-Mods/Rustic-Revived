package nadiendev.rusticrevived.block.farm;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crop stake with rope tied to it (legacy BlockStakeTied). It supports horizontal ropes and grape
 * vines, shows a knot towards every connected rope, drops the rope and turns back into a plain
 * crop stake when broken.
 */
public class StakeTiedBlock extends Block {
	public static final MapCodec<StakeTiedBlock> CODEC = simpleCodec(StakeTiedBlock::new);
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	private static final Map<Direction, BooleanProperty> PROPERTIES = Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST,
			Direction.SOUTH, SOUTH, Direction.WEST, WEST);
	private static final VoxelShape KNOT_SHAPE = Block.box(5, 0, 5, 11, 16, 11);
	private static final VoxelShape COLLISION_SHAPE = Block.box(6, 0, 6, 10, 16, 10);

	public StakeTiedBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
	}

	@Override
	protected MapCodec<? extends StakeTiedBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST);
	}

	/** {@code state} with a rope knot towards every connected neighbour. */
	public static BlockState withConnections(BlockState state, LevelAccessor level, BlockPos pos) {
		for (Map.Entry<Direction, BooleanProperty> entry : PROPERTIES.entrySet()) {
			Direction direction = entry.getKey();
			state = state.setValue(entry.getValue(), connectsTo(level.getBlockState(pos.relative(direction)), direction));
		}
		return state;
	}

	private static boolean connectsTo(BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() instanceof StakeTiedBlock) {
			return true;
		}
		if (neighbor.is(ModBlocks.GRAPE_LEAVES.get())) {
			return neighbor.getValue(GrapeLeavesBlock.AXIS) == direction.getAxis();
		}
		return FarmHelper.horizontalRopeAxis(neighbor).map(axis -> axis == direction.getAxis()).orElse(false);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		BooleanProperty property = PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
		return direction.getAxis().isHorizontal() ? state.setValue(property, connectsTo(neighborState, direction)) : state;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return KNOT_SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return COLLISION_SHAPE;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return new ItemStack(ModBlocks.ROPE.get());
	}

	/** Breaking the knot only removes the rope: the crop stake stays. */
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		return level.setBlock(pos, ModBlocks.CROP_STAKE.get().defaultBlockState(), level.isClientSide ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
	}

	@Override
	public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
		level.setBlock(pos, ModBlocks.CROP_STAKE.get().defaultBlockState(), Block.UPDATE_ALL);
		wasExploded(level, pos, explosion);
	}
}
