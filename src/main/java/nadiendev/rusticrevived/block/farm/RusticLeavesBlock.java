package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.LeavesBlock;

/**
 * Olive / ironwood leaves (legacy BlockLeavesRustic). They decay like vanilla leaves; their drops
 * (sapling, olives / ironberries, no sticks) come from the block loot tables and their colour
 * from the biome foliage colour.
 */
public class RusticLeavesBlock extends LeavesBlock {
	public static final MapCodec<RusticLeavesBlock> CODEC = simpleCodec(RusticLeavesBlock::new);

	public RusticLeavesBlock(Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<? extends RusticLeavesBlock> codec() {
		return CODEC;
	}
}
