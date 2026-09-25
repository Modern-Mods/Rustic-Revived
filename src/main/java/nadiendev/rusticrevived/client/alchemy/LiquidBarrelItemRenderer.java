package nadiendev.rusticrevived.client.alchemy;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.item.LiquidBarrelItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Liquid barrel item renderer (legacy LiquidBarrelFilledItemModel): the fluid held by the barrel
 * item, drawn over the barrel block model by the {@code rusticrevived:liquid_barrel} client item.
 */
public class LiquidBarrelItemRenderer implements SpecialModelRenderer<FluidStack> {
	/** Id of the special model type. */
	public static final Identifier ID = RusticRevived.id("liquid_barrel");

	@Override
	public @Nullable FluidStack extractArgument(ItemStack stack) {
		FluidStack fluid = LiquidBarrelItem.getFluid(stack);
		return fluid.isEmpty() ? null : fluid;
	}

	@Override
	public void submit(@Nullable FluidStack fluid, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords,
			boolean hasFoil, int outlineColor) {
		if (fluid != null) {
			LiquidBarrelRenderer.submitFluid(fluid, poseStack, collector, lightCoords);
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(0.1875F, 0.125F, 0.1875F));
		output.accept(new Vector3f(0.8125F, 0.9375F, 0.8125F));
	}

	/** Unbaked form, referenced by the client item definition. */
	public record Unbaked() implements SpecialModelRenderer.Unbaked<FluidStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public LiquidBarrelItemRenderer bake(SpecialModelRenderer.BakingContext context) {
			return new LiquidBarrelItemRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
