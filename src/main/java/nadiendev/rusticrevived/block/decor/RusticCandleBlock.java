package nadiendev.rusticrevived.block.decor;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Iron, golden or silver candle holder, placed on top of blocks or on walls like a torch (legacy BlockCandle).
 */
public class RusticCandleBlock extends MountedLightBlock {
	public static final MapCodec<RusticCandleBlock> CODEC = simpleCodec(RusticCandleBlock::new);

	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.UP, Shapes.box(0.4, 0.0, 0.4, 0.6, 0.9375, 0.6),
			Direction.NORTH, Shapes.box(0.35, 0.0, 0.7, 0.65, 0.8, 1.0),
			Direction.SOUTH, Shapes.box(0.35, 0.0, 0.0, 0.65, 0.8, 0.3),
			Direction.WEST, Shapes.box(0.7, 0.0, 0.35, 1.0, 0.8, 0.65),
			Direction.EAST, Shapes.box(0.0, 0.0, 0.35, 0.3, 0.8, 0.65));

	public RusticCandleBlock(Properties properties) {
		super(properties, SHAPES);
	}

	@Override
	protected MapCodec<? extends RusticCandleBlock> codec() {
		return CODEC;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		Direction facing = state.getValue(FACING);
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.7;
		double z = pos.getZ() + 0.5;
		if (facing.getAxis().isHorizontal()) {
			Direction back = facing.getOpposite();
			flame(level, x + 0.27 * back.getStepX(), y + 0.25, z + 0.27 * back.getStepZ());
		} else {
			flame(level, x, y + 0.33, z);
		}
	}

	/** Candle flame: a smoke puff and a flame particle. */
	static void flame(Level level, double x, double y, double z) {
		level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
	}
}
