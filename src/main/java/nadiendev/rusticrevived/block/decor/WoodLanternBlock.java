package nadiendev.rusticrevived.block.decor;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wooden lantern, standing on a block, hanging under one or mounted on a wall (legacy BlockWoodLantern).
 * The standing and hanging variants share {@code facing=up}.
 */
public class WoodLanternBlock extends MountedLightBlock {
	public static final MapCodec<WoodLanternBlock> CODEC = simpleCodec(WoodLanternBlock::new);

	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.UP, Shapes.box(0.25, 0.0, 0.25, 0.75, 1.0, 0.75),
			Direction.NORTH, Shapes.box(0.25, 0.0, 0.25, 0.75, 1.0, 1.0),
			Direction.SOUTH, Shapes.box(0.25, 0.0, 0.0, 0.75, 1.0, 0.75),
			Direction.WEST, Shapes.box(0.25, 0.0, 0.25, 1.0, 1.0, 0.75),
			Direction.EAST, Shapes.box(0.0, 0.0, 0.25, 0.75, 1.0, 0.75));

	public WoodLanternBlock(Properties properties) {
		super(properties, SHAPES);
	}

	@Override
	protected MapCodec<? extends WoodLanternBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean canMount(LevelReader level, BlockPos pos, Direction facing) {
		if (facing == Direction.UP) {
			return Block.canSupportCenter(level, pos.below(), Direction.UP) || Block.canSupportCenter(level, pos.above(), Direction.DOWN);
		}
		return super.canMount(level, pos, facing);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double x = pos.getX() + 0.5 + random.nextFloat() * 0.6F - 0.3F;
		double y = pos.getY() + 0.5 + random.nextFloat() * (6.0 / 16.0);
		double z = pos.getZ() + 0.5 + random.nextFloat() * 0.6F - 0.3F;
		RusticCandleBlock.flame(level, x, y, z);
	}
}
