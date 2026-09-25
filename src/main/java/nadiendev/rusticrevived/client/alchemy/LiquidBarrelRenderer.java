package nadiendev.rusticrevived.client.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Liquid barrel renderer (legacy LiquidBarrelRenderer): the fluid surface inside the barrel.
 */
public class LiquidBarrelRenderer implements BlockEntityRenderer<LiquidBarrelBlockEntity> {

	public LiquidBarrelRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(LiquidBarrelBlockEntity barrel, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
			int packedOverlay) {
		renderFluid(barrel.getTank().getFluid(), poseStack, buffers, packedLight);
	}

	/** Fluid surface for the given content (also used by the item renderer). */
	public static void renderFluid(FluidStack fluid, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		if (fluid.isEmpty()) return;
		float fill = Math.min(fluid.getAmount() / (float) LiquidBarrelBlockEntity.CAPACITY, 1F);
		FluidRendering.renderSurface(poseStack, buffers, fluid, 0.1875F, 0.8125F, 0.125F + 0.8125F * fill, packedLight);
	}
}
