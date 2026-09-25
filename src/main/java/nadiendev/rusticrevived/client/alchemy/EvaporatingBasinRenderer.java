package nadiendev.rusticrevived.client.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.blockentity.alchemy.EvaporatingBasinBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Drying basin renderer (legacy EvaporatingBasinRenderer): the dried items and the fluid.
 */
public class EvaporatingBasinRenderer implements BlockEntityRenderer<EvaporatingBasinBlockEntity, AlchemyTankRenderState> {
	private static final int ITEMS_PER_LAYER = 16;

	private final ItemModelResolver itemModelResolver;

	public EvaporatingBasinRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public AlchemyTankRenderState createRenderState() {
		return new AlchemyTankRenderState();
	}

	@Override
	public void extractRenderState(EvaporatingBasinBlockEntity basin, AlchemyTankRenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(basin, state, partialTicks, cameraPosition, breakProgress);
		CrushingTubRenderer.extractItemPile(itemModelResolver, basin, basin.getItems(), ITEMS_PER_LAYER, state);
		CrushingTubRenderer.extractTank(basin.getTank(), state);
	}

	@Override
	public void submit(AlchemyTankRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		CrushingTubRenderer.submitItemPile(state, poseStack, collector);
		if (!state.fluid.isEmpty()) {
			FluidRendering.renderSurface(poseStack, collector, state.fluid, 0.1875F, 0.8125F, 0.0625F + 0.1875F * state.fill, state.lightCoords);
		}
	}
}
