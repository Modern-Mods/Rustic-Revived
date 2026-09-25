package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Olive / ironwood leaves (legacy BlockLeavesRustic). They decay like vanilla leaves; their drops
 * (sapling, olives / ironberries, no sticks) come from the block loot tables and their colour
 * from the biome foliage colour.
 */
public class RusticLeavesBlock extends TintedParticleLeavesBlock {
	public static final MapCodec<RusticLeavesBlock> CODEC = simpleCodec(RusticLeavesBlock::new);

	public RusticLeavesBlock(Properties properties) {
		super(0.01F, properties);
	}

	@Override
	public MapCodec<? extends RusticLeavesBlock> codec() {
		return CODEC;
	}

	/** Burns like vanilla leaves (FireBlock#setFlammable is private in 26.1). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 60;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 30;
	}
}
