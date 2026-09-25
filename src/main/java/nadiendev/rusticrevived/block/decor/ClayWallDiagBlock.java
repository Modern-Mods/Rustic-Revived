package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

/**
 * Clay wall with a diagonal support beam (legacy BlockClayWallDiag + IAdvancedRotationPlacement). Placed
 * against a wall it faces away from it; placed on a floor or ceiling it faces the edge of the clicked face
 * closest to the cursor (the client draws the four zones as a cross on the targeted face).
 */
public class ClayWallDiagBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<ClayWallDiagBlock> CODEC = simpleCodec(ClayWallDiagBlock::new);

	public ClayWallDiagBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends ClayWallDiagBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clicked = context.getClickedFace();
		if (clicked.getAxis().isHorizontal()) {
			return defaultBlockState().setValue(FACING, clicked.getOpposite());
		}
		Vec3 hit = context.getClickLocation();
		double x = hit.x - context.getClickedPos().getX() - 0.5;
		double z = hit.z - context.getClickedPos().getZ() - 0.5;
		Direction facing;
		if (Math.abs(x) >= Math.abs(z)) {
			facing = x > 0 ? Direction.EAST : Direction.WEST;
		} else {
			facing = z > 0 ? Direction.SOUTH : Direction.NORTH;
		}
		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
