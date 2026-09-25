package nadiendev.rusticrevived.block.alchemy;

import java.util.List;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Two blocks tall alchemic condenser (legacy BlockCondenser / BlockCondenserAdvanced). The
 * bottom half holds the block entity; the machine only works with its retorts attached, all of
 * them facing away from the condenser.
 */
public abstract class AbstractCondenserBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	protected AbstractCondenserBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BOTTOM, true));
	}

	/** Whether this is the advanced condenser. */
	public abstract boolean isAdvanced();

	/** The retort block this condenser needs. */
	protected abstract Block retort();

	/** Shape of the upper half. */
	protected abstract VoxelShape topShape();

	/** Sides of the bottom half the retorts must be attached to. */
	public List<Direction> retortSides(Direction facing) {
		return isAdvanced()
				? List.of(facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite())
				: List.of(facing.getClockWise(), facing.getCounterClockWise());
	}

	/** Whether every retort is attached to the condenser whose bottom half is at {@code pos}. */
	public boolean hasRetorts(BlockGetter level, BlockPos pos, BlockState state) {
		if (!state.is(this)) return false;
		for (Direction side : retortSides(state.getValue(FACING))) {
			BlockState retort = level.getBlockState(pos.relative(side));
			if (!retort.is(retort()) || retort.getValue(RetortBlock.FACING) != side) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
			return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(BOTTOM, false), Block.UPDATE_ALL);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		boolean bottom = state.getValue(BOTTOM);
		if (bottom && direction == Direction.UP && !(neighborState.is(this) && !neighborState.getValue(BOTTOM))
				|| !bottom && direction == Direction.DOWN && !(neighborState.is(this) && neighborState.getValue(BOTTOM))) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// breaking the top half in creative must not make the bottom half drop
		if (!level.isClientSide && player.isCreative() && !state.getValue(BOTTOM)) {
			BlockPos below = pos.below();
			BlockState bottom = level.getBlockState(below);
			if (bottom.is(this) && bottom.getValue(BOTTOM)) {
				level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
				level.levelEvent(player, 2001, below, Block.getId(bottom));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		// the capabilities of the top half have no block entity to invalidate them
		level.invalidateCapabilities(pos);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && state.getValue(BOTTOM) && level.getBlockEntity(pos) instanceof AbstractCondenserBlockEntity condenser) {
			condenser.dropContents();
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
		level.invalidateCapabilities(pos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(BOTTOM) ? Shapes.block() : topShape();
	}

	@Override
	protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(BOTTOM) ? level.getMaxLightLevel() : 0;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	// ---------------------------------------------------------------- block entity

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(BOTTOM) ? createBlockEntity(pos, state) : null;
	}

	protected abstract AbstractCondenserBlockEntity createBlockEntity(BlockPos pos, BlockState state);

	protected abstract BlockEntityType<? extends AbstractCondenserBlockEntity> blockEntityType();

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type != blockEntityType()) return null;
		BlockEntityTicker<AbstractCondenserBlockEntity> ticker = level.isClientSide
				? AbstractCondenserBlockEntity::clientTick
				: AbstractCondenserBlockEntity::serverTick;
		return (BlockEntityTicker<T>) ticker;
	}

	/** The working condenser of either half, or null if it is incomplete. */
	@Nullable
	public AbstractCondenserBlockEntity getCondenser(Level level, BlockPos pos, BlockState state) {
		BlockPos bottomPos = state.getValue(BOTTOM) ? pos : pos.below();
		BlockState bottom = level.getBlockState(bottomPos);
		if (level.getBlockEntity(bottomPos) instanceof AbstractCondenserBlockEntity condenser && hasRetorts(level, bottomPos, bottom)) {
			return condenser;
		}
		return null;
	}

	// ---------------------------------------------------------------- interaction

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return useItem(stack, state, level, pos, player, hand);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return use(state, level, pos, player);
	}

	/** Fills or drains the tank with a fluid container (also used by the retorts). */
	public ItemInteractionResult useItem(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		AbstractCondenserBlockEntity condenser = getCondenser(level, pos, state);
		if (condenser == null) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (AlchemyInteractions.isFluidContainer(stack) && AlchemyInteractions.useFluidContainer(level, player, hand, condenser.getTank())) {
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/** Opens the GUI, or voids the tank when sneaking with an empty hand (also used by the retorts). */
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player) {
		AbstractCondenserBlockEntity condenser = getCondenser(level, pos, state);
		if (condenser == null) {
			return InteractionResult.PASS;
		}
		if (player.isSecondaryUseActive() && player.getMainHandItem().isEmpty() && !condenser.getTank().isEmpty()) {
			return AlchemyInteractions.voidTank(level, condenser.getBlockPos(), condenser.getTank());
		}
		if (!level.isClientSide) {
			player.openMenu(condenser, buf -> buf.writeBlockPos(condenser.getBlockPos()));
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
