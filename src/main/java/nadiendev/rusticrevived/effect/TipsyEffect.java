package nadiendev.rusticrevived.effect;

import java.util.Set;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.EffectCure;

/**
 * Inebriated. Levels III and IV periodically make the drinker nauseous, slow and (at IV) blind.
 * Milk does not cure it: only water bottles sober you up (see {@link EffectEvents}).
 */
public class TipsyEffect extends RusticEffect {
	public TipsyEffect() {
		super(MobEffectCategory.HARMFUL, 7900290);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amp) {
		if (!entity.level().isClientSide && amp > 1) {
			if (amp > 2) {
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0, false, false));
			} else {
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false));
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0, false, false));
			}
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return amplifier > 1 && duration % 100 == 0;
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		// not curable with milk
	}
}
