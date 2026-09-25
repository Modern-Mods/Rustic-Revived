package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Gargoyle statue facing the player who placed it (legacy BlockGargoyle).
 */
public class GargoyleBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<GargoyleBlock> CODEC = simpleCodec(GargoyleBlock::new);

	private static final VoxelShape SHAPE = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.875, 0.875);

	public GargoyleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends GargoyleBlock> codec() {
		return CODEC;
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
