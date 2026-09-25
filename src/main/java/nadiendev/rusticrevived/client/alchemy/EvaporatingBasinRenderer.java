package nadiendev.rusticrevived.client.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.blockentity.alchemy.EvaporatingBasinBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Drying basin renderer (legacy EvaporatingBasinRenderer): the dried items and the fluid.
 */
public class EvaporatingBasinRenderer implements BlockEntityRenderer<EvaporatingBasinBlockEntity> {

	public EvaporatingBasinRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(EvaporatingBasinBlockEntity basin, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
			int packedOverlay) {
		CrushingTubRenderer.renderItemPile(basin, basin.getItems().getStackInSlot(0), 16, poseStack, buffers, packedLight, packedOverlay);
		if (!basin.getTank().isEmpty()) {
			float fill = basin.getTank().getFluidAmount() / (float) basin.getTank().getCapacity();
			FluidRendering.renderSurface(poseStack, buffers, basin.getTank().getFluid(), 0.1875F, 0.8125F, 0.0625F + 0.1875F * fill, packedLight);
		}
	}
}
