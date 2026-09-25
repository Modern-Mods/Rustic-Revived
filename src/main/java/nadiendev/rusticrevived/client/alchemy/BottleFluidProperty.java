package nadiendev.rusticrevived.client.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.item.FluidBottleItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Select item model property: id of the fluid held by a fluid bottle (null when empty). Selects
 * the labelled alcohol bottle model of each booze (legacy {@code rusticrevived:booze} item
 * property).
 */
public record BottleFluidProperty() implements SelectItemModelProperty<Identifier> {
	public static final Identifier ID = RusticRevived.id("bottle_fluid");
	public static final SelectItemModelProperty.Type<BottleFluidProperty, Identifier> TYPE = SelectItemModelProperty.Type.create(
			MapCodec.unit(new BottleFluidProperty()), Identifier.CODEC);

	@Override
	public @Nullable Identifier get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
		FluidStack fluid = FluidBottleItem.getFluid(stack);
		return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
	}

	@Override
	public Codec<Identifier> valueCodec() {
		return Identifier.CODEC;
	}

	@Override
	public SelectItemModelProperty.Type<BottleFluidProperty, Identifier> type() {
		return TYPE;
	}
}
