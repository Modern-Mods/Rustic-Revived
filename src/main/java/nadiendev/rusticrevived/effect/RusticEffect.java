package nadiendev.rusticrevived.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Plain effect whose behaviour lives in {@link EffectEvents} (full stomach, magic resistance,
 * wither ward, undying, fire power).
 */
public class RusticEffect extends MobEffect {
	public RusticEffect(MobEffectCategory category, int color) {
		super(category, color);
	}
}
