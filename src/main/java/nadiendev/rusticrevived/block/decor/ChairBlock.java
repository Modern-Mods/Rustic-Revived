package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.entity.ChairSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wooden chair (legacy BlockChair). Using it makes the player sit on an invisible {@link ChairSeatEntity};
 * sneaking gets up again.
 */
public class ChairBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<ChairBlock> CODEC = simpleCodec(ChairBlock::new);

	private static final VoxelShape SHAPE = Shapes.box(0.125, 0.0, 0.125, 0.875, 1.125, 0.875);
	/** Squared distance from the chair center within which a player can sit down. */
	private static final double MAX_SIT_DISTANCE_SQR = 5.0;

	public ChairBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends ChairBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (player.distanceToSqr(Vec3.atCenterOf(pos)) >= MAX_SIT_DISTANCE_SQR || player.isShiftKeyDown() || player.isPassenger()) {
			return InteractionResult.CONSUME;
		}
		if (!level.isClientSide() && level.getEntitiesOfClass(ChairSeatEntity.class, new AABB(pos)).isEmpty()) {
			ChairSeatEntity seat = new ChairSeatEntity(level, pos, state.getValue(FACING));
			level.addFreshEntity(seat);
			player.startRiding(seat);
		}
		return InteractionResult.SUCCESS;
	}

	/** Burn chance of wooden furniture (legacy Blocks.FIRE.setFireInfo(..., 5, 20)); nether woods do not burn. */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.ignitedByLava() ? 20 : 0;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.ignitedByLava() ? 5 : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
