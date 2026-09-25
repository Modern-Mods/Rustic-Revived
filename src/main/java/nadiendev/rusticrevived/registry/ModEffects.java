package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.effect.BlazingTrailEffect;
import nadiendev.rusticrevived.effect.FeatherEffect;
import nadiendev.rusticrevived.effect.FullmetalEffect;
import nadiendev.rusticrevived.effect.IronSkinEffect;
import nadiendev.rusticrevived.effect.RusticEffect;
import nadiendev.rusticrevived.effect.ShameEffect;
import nadiendev.rusticrevived.effect.TipsyEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, RusticRevived.NAMESPACE);

	public static final DeferredHolder<MobEffect, MobEffect> IRON_SKIN = EFFECTS.register("iron_skin", IronSkinEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> FEATHER = EFFECTS.register("feather", FeatherEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLAZING_TRAIL = EFFECTS.register("blazing_trail", BlazingTrailEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SHAME = EFFECTS.register("shame", ShameEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> FULLMETAL = EFFECTS.register("fullmetal", FullmetalEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> FIRE_POWER = EFFECTS.register("fire_power", () -> new RusticEffect(MobEffectCategory.BENEFICIAL, 0xFFCE6D));
	public static final DeferredHolder<MobEffect, MobEffect> FULL = EFFECTS.register("full", () -> new RusticEffect(MobEffectCategory.BENEFICIAL, 6563840));
	public static final DeferredHolder<MobEffect, MobEffect> MAGIC_RESISTANCE = EFFECTS.register("magic_resistance", () -> new RusticEffect(MobEffectCategory.BENEFICIAL, 10511560));
	public static final DeferredHolder<MobEffect, MobEffect> WITHER_WARD = EFFECTS.register("wither_ward", () -> new RusticEffect(MobEffectCategory.BENEFICIAL, 11842760));
	public static final DeferredHolder<MobEffect, MobEffect> UNDYING = EFFECTS.register("undying", () -> new RusticEffect(MobEffectCategory.BENEFICIAL, 0xEADB84));
	public static final DeferredHolder<MobEffect, MobEffect> TIPSY = EFFECTS.register("tipsy", TipsyEffect::new);

	private ModEffects() {
	}
}
