package nadiendev.rusticrevived.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;

/**
 * Blazing Trail: the entity leaves fire behind it while walking.
 */
public class BlazingTrailEffect extends RusticEffect {
	public BlazingTrailEffect() {
		super(MobEffectCategory.BENEFICIAL, 16738816);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		BlockPos pos = entity.blockPosition();
		if (entity.onGround() && level.isEmptyBlock(pos)
				&& level.getBlockState(pos.below()).isRedstoneConductor(level, pos.below())) {
			level.setBlock(pos, BaseFireBlock.getState(level, pos), 3);
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 10 == 0;
	}
}
