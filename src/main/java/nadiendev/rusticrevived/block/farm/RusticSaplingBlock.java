package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;

/**
 * Olive, ironwood and apple saplings (legacy BlockSaplingRustic / BlockSaplingApple). The legacy
 * saplings behaved exactly like vanilla ones (two stages, light 9, 1/7 chance per random tick, 45%
 * bone meal success); the trees come from {@link nadiendev.rusticrevived.world.ModTreeGrowers}.
 */
public class RusticSaplingBlock extends SaplingBlock {
	public static final MapCodec<RusticSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			TreeGrower.CODEC.fieldOf("tree").forGetter(b -> b.treeGrower),
			propertiesCodec()
	).apply(i, RusticSaplingBlock::new));

	public RusticSaplingBlock(TreeGrower treeGrower, Properties properties) {
		super(treeGrower, properties);
	}

	@Override
	public MapCodec<? extends RusticSaplingBlock> codec() {
		return CODEC;
	}
}
