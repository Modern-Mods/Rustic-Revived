package nadiendev.rusticrevived.block.alchemy;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Alchemic retort (legacy BlockRetort). It is attached to the condenser behind it when it faces
 * away from that condenser; using the retort uses the condenser.
 */
public class RetortBlock extends Block {
	public static final MapCodec<RetortBlock> CODEC = simpleCodec(RetortBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

	public RetortBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends RetortBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/** Position of the condenser this retort would be attached to. */
	private static BlockPos condenserPos(BlockState state, BlockPos pos) {
		return pos.relative(state.getValue(FACING).getOpposite());
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		invalidateCondenser(state, level, pos);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		super.onRemove(state, level, pos, newState, movedByPiston);
		invalidateCondenser(state, level, pos);
	}

	/** Condenser capabilities depend on the retorts: refresh them when a retort changes. */
	private static void invalidateCondenser(BlockState state, Level level, BlockPos pos) {
		BlockPos condenser = condenserPos(state, pos);
		level.invalidateCapabilities(condenser);
		level.invalidateCapabilities(condenser.above());
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		BlockPos target = condenserPos(state, pos);
		BlockState condenser = level.getBlockState(target);
		if (condenser.getBlock() instanceof AbstractCondenserBlock block) {
			return block.useItem(stack, condenser, level, target, player, hand);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		BlockPos target = condenserPos(state, pos);
		BlockState condenser = level.getBlockState(target);
		if (condenser.getBlock() instanceof AbstractCondenserBlock block) {
			return block.use(condenser, level, target, player);
		}
		return InteractionResult.PASS;
	}
}
