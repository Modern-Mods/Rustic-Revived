package nadiendev.rusticrevived.fluid;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Placed Rustic fluid with an optional effect on entities inside it (legacy BlockFluidRustic).
 */
public class RusticLiquidBlock extends LiquidBlock {

	@FunctionalInterface
	public interface EntityInside {
		void apply(BlockState state, Level level, BlockPos pos, Entity entity);
	}

	private final EntityInside entityInside;

	public RusticLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties, EntityInside entityInside) {
		super(fluid.get(), properties);
		this.entityInside = entityInside;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		super.entityInside(state, level, pos, entity);
		if (entityInside != null) {
			entityInside.apply(state, level, pos, entity);
		}
	}
}
