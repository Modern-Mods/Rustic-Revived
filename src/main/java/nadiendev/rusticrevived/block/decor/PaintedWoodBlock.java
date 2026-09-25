package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Painted planks (legacy BlockPaintedWood): a plain block burning like vanilla planks.
 */
public class PaintedWoodBlock extends Block {
	public static final MapCodec<PaintedWoodBlock> CODEC = simpleCodec(PaintedWoodBlock::new);

	public PaintedWoodBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends PaintedWoodBlock> codec() {
		return CODEC;
	}

	/** Burns like planks (legacy Blocks.FIRE.setFireInfo(painted wood, 5, 20)). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 5;
	}
}
