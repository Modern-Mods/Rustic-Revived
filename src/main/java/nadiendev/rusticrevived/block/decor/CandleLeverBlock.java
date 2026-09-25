package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Candle that works like a button: using it tilts the candle and emits a redstone signal for four seconds
 * (legacy BlockCandleLever). Strongly powers the block it is mounted on.
 */
public class CandleLeverBlock extends RusticCandleBlock {
	public static final MapCodec<CandleLeverBlock> CODEC = simpleCodec(CandleLeverBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	/** Ticks the lever stays pulled (legacy tick rate). */
	private static final int PULL_DURATION = 80;

	public CandleLeverBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected MapCodec<? extends CandleLeverBlock> codec() {
		return CODEC;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (state.getValue(POWERED)) {
			return InteractionResult.CONSUME;
		}
		level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
		updateNeighbours(state, level, pos);
		level.scheduleTick(pos, this, PULL_DURATION);
		level.playSound(player, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
		level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED)) {
			level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
			updateNeighbours(state, level, pos);
			level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.5F);
			level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		if (!movedByPiston && state.getValue(POWERED)) {
			updateNeighbours(state, level, pos);
		}
	}

	private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
		level.updateNeighborsAt(pos, this);
		level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) && state.getValue(FACING) == direction ? 15 : 0;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		Direction facing = state.getValue(FACING);
		boolean pulled = state.getValue(POWERED);
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.7 + (pulled ? -0.125 : 0.0);
		double z = pos.getZ() + 0.5;
		if (facing.getAxis().isHorizontal()) {
			Direction back = facing.getOpposite();
			double offset = pulled ? 0.0 : 0.27;
			flame(level, x + offset * back.getStepX(), y + 0.25, z + offset * back.getStepZ());
		} else {
			flame(level, x, y + 0.33, z + (pulled ? -0.35 : 0.0));
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POWERED);
	}
}
