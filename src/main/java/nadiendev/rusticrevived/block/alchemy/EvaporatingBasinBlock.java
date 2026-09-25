package nadiendev.rusticrevived.block.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.EvaporatingBasinBlockEntity;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Drying basin (legacy BlockEvaporatingBasin): filled with fluid containers, dries the fluid
 * into items that are taken out with an empty hand.
 */
public class EvaporatingBasinBlock extends Block implements EntityBlock {
	public static final MapCodec<EvaporatingBasinBlock> CODEC = simpleCodec(EvaporatingBasinBlock::new);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 4, 15);

	public EvaporatingBasinBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends EvaporatingBasinBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EvaporatingBasinBlockEntity(pos, state);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || type != ModBlockEntities.EVAPORATING_BASIN.get()) return null;
		BlockEntityTicker<EvaporatingBasinBlockEntity> ticker = EvaporatingBasinBlockEntity::serverTick;
		return (BlockEntityTicker<T>) ticker;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof EvaporatingBasinBlockEntity basin && AlchemyInteractions.isFluidContainer(stack)) {
			return AlchemyInteractions.useFluidContainerConsuming(level, player, hand, basin.getTank());
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof EvaporatingBasinBlockEntity basin)) {
			return InteractionResult.PASS;
		}
		if (player.isSecondaryUseActive() && !basin.getTank().isEmpty()) {
			return AlchemyInteractions.voidTank(level, pos, basin.getTank());
		}
		if (!basin.getItems().getStackInSlot(0).isEmpty()) {
			return AlchemyInteractions.takeItem(level, player, basin.getItems(), 0);
		}
		return InteractionResult.PASS;
	}

}
