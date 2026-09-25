package nadiendev.rusticrevived.block.alchemy;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Brewing barrel (legacy BlockBrewingBarrel): opens the brewing GUI.
 */
public class BrewingBarrelBlock extends Block implements EntityBlock {
	public static final MapCodec<BrewingBarrelBlock> CODEC = simpleCodec(BrewingBarrelBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BrewingBarrelBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BrewingBarrelBlock> codec() {
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
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BrewingBarrelBlockEntity(pos, state);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != ModBlockEntities.BREWING_BARREL.get()) return null;
		BlockEntityTicker<BrewingBarrelBlockEntity> ticker = BrewingBarrelBlockEntity::serverTick;
		return (BlockEntityTicker<T>) ticker;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof BrewingBarrelBlockEntity barrel)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			player.openMenu(barrel, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BrewingBarrelBlockEntity barrel) {
			barrel.dropContents();
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
