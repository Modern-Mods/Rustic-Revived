package nadiendev.rusticrevived.advancement;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

/**
 * Data component predicate {@code rusticrevived:booze} (legacy AlcoholItemPredicate {@code rustic:alcohol}):
 * matches containers (fluid bottles, liquid barrels, ...) holding a booze, optionally of one specific
 * fluid, whose quality is within the given bounds.
 * <pre>{@code "predicates": { "rusticrevived:booze": { "fluid": "rusticrevived:wine", "quality": { "min": 0.999 } } }}</pre>
 */
public record BoozeItemPredicate(Optional<Holder<Fluid>> fluid, MinMaxBounds.Doubles quality) implements DataComponentPredicate {
	public static final Codec<BoozeItemPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
			BuiltInRegistries.FLUID.holderByNameCodec().optionalFieldOf("fluid").forGetter(BoozeItemPredicate::fluid),
			MinMaxBounds.Doubles.CODEC.optionalFieldOf("quality", MinMaxBounds.Doubles.ANY).forGetter(BoozeItemPredicate::quality)
	).apply(i, BoozeItemPredicate::new));

	/** Any booze of at least {@code minQuality}. */
	public static BoozeItemPredicate minQuality(double minQuality) {
		return new BoozeItemPredicate(Optional.empty(), MinMaxBounds.Doubles.atLeast(minQuality));
	}

	/** The given booze with a quality of at least {@code minQuality}. */
	public static BoozeItemPredicate minQuality(Fluid fluid, double minQuality) {
		return new BoozeItemPredicate(Optional.of(fluid.builtInRegistryHolder()), MinMaxBounds.Doubles.atLeast(minQuality));
	}

	@Override
	public boolean matches(DataComponentGetter components) {
		FluidStack contained = components instanceof ItemStack stack ? FluidUtil.getFirstStackContained(stack) : FluidStack.EMPTY;
		if (contained.isEmpty()) {
			contained = components.getOrDefault(ModDataComponents.FLUID.get(), SimpleFluidContent.EMPTY).copy();
		}
		if (contained.isEmpty() || !(contained.getFluid().getFluidType() instanceof BoozeFluidType)) return false;
		if (fluid.isPresent() && !contained.is(fluid.get())) return false;
		return quality.matches(BoozeFluidType.getQuality(contained));
	}
}
