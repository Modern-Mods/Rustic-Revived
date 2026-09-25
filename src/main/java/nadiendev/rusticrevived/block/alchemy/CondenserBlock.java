package nadiendev.rusticrevived.block.alchemy;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.CondenserBlockEntity;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Alchemic condenser (legacy BlockCondenser): needs a retort on its left and right sides.
 */
public class CondenserBlock extends AbstractCondenserBlock {
	public static final MapCodec<CondenserBlock> CODEC = simpleCodec(CondenserBlock::new);
	private static final VoxelShape TOP_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

	public CondenserBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends CondenserBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean isAdvanced() {
		return false;
	}

	@Override
	protected Block retort() {
		return ModBlocks.RETORT.get();
	}

	@Override
	protected VoxelShape topShape() {
		return TOP_SHAPE;
	}

	@Override
	protected AbstractCondenserBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CondenserBlockEntity(pos, state);
	}

	@Override
	protected BlockEntityType<? extends AbstractCondenserBlockEntity> blockEntityType() {
		return ModBlockEntities.CONDENSER.get();
	}
}
