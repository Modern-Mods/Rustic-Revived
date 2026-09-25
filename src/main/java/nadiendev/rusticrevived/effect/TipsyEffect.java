package nadiendev.rusticrevived.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Inebriated. Levels III and IV periodically make the drinker nauseous, slow and (at IV) blind.
 * Milk does not cure it and only water bottles sober you up (see {@link EffectEvents}).
 */
public class TipsyEffect extends RusticEffect {
	public TipsyEffect() {
		super(MobEffectCategory.HARMFUL, 7900290);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amp) {
		if (amp > 1) {
			if (amp > 2) {
				entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 1, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 400, 1, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0, false, false));
			} else {
				entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 0, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 400, 0, false, false));
			}
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return amplifier > 1 && duration % 100 == 0;
	}
}
