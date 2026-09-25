package nadiendev.rusticrevived.client.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.item.FluidBottleItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Colour of the label of a booze bottle, depending on the booze quality (legacy fluid bottle
 * item colour, tint index 2).
 */
public final class BoozeLabelTint implements ItemTintSource {
	public static final Identifier ID = RusticRevived.id("booze_label");
	public static final BoozeLabelTint INSTANCE = new BoozeLabelTint();
	public static final MapCodec<BoozeLabelTint> MAP_CODEC = MapCodec.unit(INSTANCE);
	/** Label colour of booze bottles without a quality. */
	private static final float DEFAULT_LABEL_QUALITY = 0.5F;

	private BoozeLabelTint() {
	}

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
		FluidStack fluid = FluidBottleItem.getFluid(stack);
		return BoozeQuality.labelColor(BoozeFluidType.hasQuality(fluid) ? BoozeFluidType.getQuality(fluid) : DEFAULT_LABEL_QUALITY);
	}

	@Override
	public MapCodec<BoozeLabelTint> type() {
		return MAP_CODEC;
	}
}
