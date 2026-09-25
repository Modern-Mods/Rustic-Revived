package nadiendev.rusticrevived.block.storage;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.storage.RusticBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Barrel with 27 slots (legacy BlockBarrel). The block entity drops its contents when removed.
 */
public class RusticBarrelBlock extends Block implements EntityBlock {
	public static final MapCodec<RusticBarrelBlock> CODEC = simpleCodec(RusticBarrelBlock::new);
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	public RusticBarrelBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends RusticBarrelBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RusticBarrelBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RusticBarrelBlockEntity barrel) {
			player.openMenu(barrel, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}
}
