package nadiendev.rusticrevived.client.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Liquid barrel renderer (legacy LiquidBarrelRenderer): the fluid surface inside the barrel.
 */
public class LiquidBarrelRenderer implements BlockEntityRenderer<LiquidBarrelBlockEntity, AlchemyTankRenderState> {

	public LiquidBarrelRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public AlchemyTankRenderState createRenderState() {
		return new AlchemyTankRenderState();
	}

	@Override
	public void extractRenderState(LiquidBarrelBlockEntity barrel, AlchemyTankRenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(barrel, state, partialTicks, cameraPosition, breakProgress);
		state.fluid = barrel.getTank().getFluid();
	}

	@Override
	public void submit(AlchemyTankRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		submitFluid(state.fluid, poseStack, collector, state.lightCoords);
	}

	/** Fluid surface for the given content (also used by the item renderer). */
	public static void submitFluid(FluidStack fluid, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
		if (fluid.isEmpty()) return;
		float fill = Math.min(fluid.getAmount() / (float) LiquidBarrelBlockEntity.CAPACITY, 1F);
		FluidRendering.renderSurface(poseStack, collector, fluid, 0.1875F, 0.8125F, 0.125F + 0.8125F * fill, lightCoords);
	}
}
