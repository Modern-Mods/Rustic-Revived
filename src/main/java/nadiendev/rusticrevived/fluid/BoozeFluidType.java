package nadiendev.rusticrevived.fluid;

import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * An alcoholic beverage produced by the brewing barrel. Its effect depends on the
 * {@link ModDataComponents#BOOZE_QUALITY quality} of the stack: at 0.5 and above the drinker is
 * rewarded, below 0.5 punished. Drinking can make the drinker tipsy.
 */
public class BoozeFluidType extends DrinkableFluidType {

	@FunctionalInterface
	public interface QualityEffect {
		void affect(Level level, Player player, float quality);
	}

	protected final float inebriationChance;
	private final QualityEffect qualityEffect;

	public BoozeFluidType(Properties properties, float inebriationChance, QualityEffect qualityEffect) {
		super(properties, (level, player, stack, fluid) -> {
		});
		this.inebriationChance = inebriationChance;
		this.qualityEffect = qualityEffect;
	}

	public float getInebriationChance() {
		return inebriationChance;
	}

	@Override
	public void onDrank(Level level, Player player, ItemStack stack, FluidStack fluid) {
		float quality = getQuality(fluid);
		inebriate(level, player, quality);
		qualityEffect.affect(level, player, quality);
	}

	public static float getQuality(FluidStack fluid) {
		Float quality = fluid.get(ModDataComponents.BOOZE_QUALITY);
		return quality == null ? 0F : Math.max(Math.min(quality, 1F), 0F);
	}

	public static boolean hasQuality(FluidStack fluid) {
		return fluid.has(ModDataComponents.BOOZE_QUALITY);
	}

	public static FluidStack withQuality(FluidStack fluid, float quality) {
		FluidStack copy = fluid.copy();
		copy.set(ModDataComponents.BOOZE_QUALITY, Math.max(Math.min(quality, 1F), 0F));
		return copy;
	}

	protected void inebriate(Level level, Player player, float quality) {
		int duration = quality >= 0.5F
				? (int) (12000 * Math.max(1 - Math.abs(quality - 0.75F), 0.5F))
				: (int) (12000 * Math.max(1 - quality * 0.5F, 0.5F));
		float inebriationChanceMod = Math.max(Math.min(1 - Math.abs(0.67F * (quality - 0.75F)), 1), 0);
		MobEffectInstance tipsy = player.getEffect(ModEffects.TIPSY);
		if (level.random.nextFloat() < inebriationChance * inebriationChanceMod) {
			if (tipsy == null) {
				player.addEffect(new MobEffectInstance(ModEffects.TIPSY, duration, 0, false, false));
			} else if (tipsy.getAmplifier() < 3) {
				player.addEffect(new MobEffectInstance(ModEffects.TIPSY, Math.max(duration, tipsy.getDuration()), tipsy.getAmplifier() + 1, false, false));
			}
		}
	}
}
