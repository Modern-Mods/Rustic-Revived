package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crop stake (legacy BlockCropStake). Tomato and chili seeds are planted on it (see
 * {@link nadiendev.rusticrevived.item.StakeCropSeedItem}) and rope can be tied to it, turning it
 * into a {@link StakeTiedBlock}.
 */
public class CropStakeBlock extends Block {
	public static final MapCodec<CropStakeBlock> CODEC = simpleCodec(CropStakeBlock::new);
	/** Outline of every stake based block. */
	public static final VoxelShape STAKE_OUTLINE = Block.box(6, 0, 6, 10, 16, 10);
	/** Collision of every stake based block (thin enough to hold torches on top). */
	public static final VoxelShape STAKE_COLLISION = Block.box(7, 0, 7, 9, 16, 9);

	public CropStakeBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends CropStakeBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return STAKE_OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return STAKE_COLLISION;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hitResult) {
		if (!stack.is(ModBlocks.ROPE.get().asItem())) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, StakeTiedBlock.withConnections(ModBlocks.STAKE_TIED.get().defaultBlockState(), level, pos), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}

	/** Burns like vanilla planks (FireBlock#setFlammable is private in 26.1). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 5;
	}
}
