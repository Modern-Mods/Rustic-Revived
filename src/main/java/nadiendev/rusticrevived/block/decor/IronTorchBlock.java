package nadiendev.rusticrevived.block.decor;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wrought iron torch, placed on top of blocks or on walls (legacy BlockIronTorch).
 */
public class IronTorchBlock extends MountedLightBlock {
	public static final MapCodec<IronTorchBlock> CODEC = simpleCodec(IronTorchBlock::new);

	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.UP, Shapes.box(0.4, 0.0, 0.4, 0.6, 0.7, 0.6),
			Direction.NORTH, Shapes.box(0.375, 0.0, 0.59375, 0.625, 0.7, 1.0),
			Direction.SOUTH, Shapes.box(0.375, 0.0, 0.0, 0.625, 0.7, 0.40625),
			Direction.WEST, Shapes.box(0.59375, 0.0, 0.375, 1.0, 0.7, 0.625),
			Direction.EAST, Shapes.box(0.0, 0.0, 0.375, 0.40625, 0.7, 0.625));

	public IronTorchBlock(Properties properties) {
		super(properties, SHAPES);
	}

	@Override
	protected MapCodec<? extends IronTorchBlock> codec() {
		return CODEC;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		// standing torches have no horizontal offset: the back of an upward facing torch is DOWN
		Direction back = state.getValue(FACING).getOpposite();
		RusticCandleBlock.flame(level, pos.getX() + 0.5 + 0.21875 * back.getStepX(), pos.getY() + 0.7, pos.getZ() + 0.5 + 0.21875 * back.getStepZ());
	}
}
