package nadiendev.rusticrevived.util;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Rustic's damage types (data driven, see the datagen damage type bootstrap).
 */
public final class RusticDamage {
	/** Low quality ambrosia: "%1$s tasted tainted divinity". */
	public static final ResourceKey<DamageType> BAD_AMBROSIA = ResourceKey.create(Registries.DAMAGE_TYPE, RusticRevived.id("bad_ambrosia"));

	private RusticDamage() {
	}

	public static DamageSource badAmbrosia(Level level) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(BAD_AMBROSIA));
	}
}
