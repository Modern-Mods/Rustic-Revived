package nadiendev.rusticrevived.client.alchemy;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import nadiendev.rusticrevived.blockentity.alchemy.AlchemyItems;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyTank;
import nadiendev.rusticrevived.blockentity.alchemy.CrushingTubBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Crushing tub renderer (legacy CrushingTubRenderer): a pile of the held items and the fluid.
 */
public class CrushingTubRenderer implements BlockEntityRenderer<CrushingTubBlockEntity, AlchemyTankRenderState> {
	private static final float LAYER_HEIGHT = 0.0625F;
	private static final int ITEMS_PER_LAYER = 8;

	private final ItemModelResolver itemModelResolver;

	public CrushingTubRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public AlchemyTankRenderState createRenderState() {
		return new AlchemyTankRenderState();
	}

	@Override
	public void extractRenderState(CrushingTubBlockEntity tub, AlchemyTankRenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(tub, state, partialTicks, cameraPosition, breakProgress);
		extractItemPile(itemModelResolver, tub, tub.getItems(), ITEMS_PER_LAYER, state);
		extractTank(tub.getTank(), state);
	}

	@Override
	public void submit(AlchemyTankRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		submitItemPile(state, poseStack, collector);
		if (!state.fluid.isEmpty()) {
			FluidRendering.renderSurface(poseStack, collector, state.fluid, 0.0625F, 0.9375F, 0.0625F + 0.5F * state.fill, state.lightCoords);
		}
	}

	/** Captures the stack held in the first slot, one layer per {@code itemsPerLayer} items. */
	public static void extractItemPile(ItemModelResolver resolver, BlockEntity blockEntity, AlchemyItems items, int itemsPerLayer,
			AlchemyTankRenderState state) {
		ItemStack stack = items.getStackInSlot(0);
		state.seed = blockEntity.getBlockPos().asLong();
		state.itemLayers = (stack.getCount() + itemsPerLayer - 1) / itemsPerLayer;
		resolver.updateForTopItem(state.item, stack, ItemDisplayContext.NONE, blockEntity.getLevel(), null, 0);
	}

	/** Captures the fluid and fill ratio of the tank. */
	public static void extractTank(AlchemyTank tank, AlchemyTankRenderState state) {
		state.fluid = tank.getFluid();
		state.fill = tank.getFluidAmount() / (float) tank.getCapacity();
	}

	/** Renders the items lying flat, each layer randomly turned (the same way for a given block). */
	public static void submitItemPile(AlchemyTankRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
		if (state.item.isEmpty()) return;
		RandomSource random = RandomSource.create(state.seed);
		for (int layer = 0; layer < state.itemLayers; layer++) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.062F + layer * LAYER_HEIGHT, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90F));
			poseStack.scale(0.5F, 0.5F, 0.5F);
			state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}
}
