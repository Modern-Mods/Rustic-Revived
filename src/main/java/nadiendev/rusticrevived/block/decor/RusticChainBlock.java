package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Iron, golden or silver chain (legacy BlockChain): a climbable line held by lattice or by anything offering a
 * center support (full faces, fences, walls, lanterns...).
 */
public class RusticChainBlock extends RopeBaseBlock {
	public static final MapCodec<RusticChainBlock> CODEC = simpleCodec(RusticChainBlock::new);

	public RusticChainBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends RusticChainBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean isSupport(BlockState other, LevelReader level, BlockPos otherPos, Direction face, Direction.Axis axis) {
		return other.getBlock() instanceof LatticeBlock || Block.canSupportCenter(level, otherPos, face);
	}
}
