package nadiendev.rusticrevived.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Feather: slows falls down to a gentle glide and cancels fall damage.
 */
public class FeatherEffect extends RusticEffect {
	public FeatherEffect() {
		super(MobEffectCategory.BENEFICIAL, 14474460);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		Vec3 motion = entity.getDeltaMovement();
		if (!entity.onGround() && motion.y < -0.4D) {
			entity.setDeltaMovement(motion.x, motion.y + 0.1D, motion.z);
			entity.hurtMarked = true;
			entity.resetFallDistance();
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}
