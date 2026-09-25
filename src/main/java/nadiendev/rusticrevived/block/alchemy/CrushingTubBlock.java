package nadiendev.rusticrevived.block.alchemy;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.CrushingTubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crushing tub (legacy BlockCrushingTub): put items in with a right click, jump on it to crush
 * them into fluid, drain it with empty containers.
 */
public class CrushingTubBlock extends Block implements EntityBlock {
	public static final MapCodec<CrushingTubBlock> CODEC = simpleCodec(CrushingTubBlock::new);
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);

	public CrushingTubBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends CrushingTubBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrushingTubBlockEntity(pos, state);
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		if (entity instanceof LivingEntity && level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub) {
			tub.crush();
		}
		super.fallOn(level, state, pos, entity, fallDistance);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (AlchemyInteractions.isFluidContainer(stack) && AlchemyInteractions.getFluidContained(stack).isEmpty()) {
			// empty containers take fluid out of the tub
			return AlchemyInteractions.useFluidContainer(level, player, hand, tub.getFluidHandler())
					? InteractionResult.SUCCESS
					: InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			player.setItemInHand(hand, tub.getItems().insertItem(0, stack, false));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub)) {
			return InteractionResult.PASS;
		}
		if (player.isSecondaryUseActive() && !tub.getTank().isEmpty()) {
			return AlchemyInteractions.voidTank(level, pos, tub.getTank());
		}
		if (!tub.getItems().getStackInSlot(0).isEmpty()) {
			return AlchemyInteractions.takeItem(level, player, tub.getItems(), 0);
		}
		return InteractionResult.PASS;
	}

}
