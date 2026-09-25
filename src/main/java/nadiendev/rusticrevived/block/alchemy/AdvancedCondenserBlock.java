package nadiendev.rusticrevived.block.alchemy;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.AdvancedCondenserBlockEntity;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Advanced alchemic condenser (legacy BlockCondenserAdvanced): needs advanced retorts on its
 * left, right and back sides. Its top half exposes the capabilities of the bottom half.
 */
public class AdvancedCondenserBlock extends AbstractCondenserBlock {
	public static final MapCodec<AdvancedCondenserBlock> CODEC = simpleCodec(AdvancedCondenserBlock::new);

	public AdvancedCondenserBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends AdvancedCondenserBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean isAdvanced() {
		return true;
	}

	@Override
	protected Block retort() {
		return ModBlocks.RETORT_ADVANCED.get();
	}

	@Override
	protected VoxelShape topShape() {
		return Shapes.block();
	}

	@Override
	protected AbstractCondenserBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AdvancedCondenserBlockEntity(pos, state);
	}

	@Override
	protected BlockEntityType<? extends AbstractCondenserBlockEntity> blockEntityType() {
		return ModBlockEntities.CONDENSER_ADVANCED.get();
	}
}
