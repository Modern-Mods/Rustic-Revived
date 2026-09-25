package nadiendev.rusticrevived.client.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import nadiendev.rusticrevived.blockentity.alchemy.CrushingTubBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Crushing tub renderer (legacy CrushingTubRenderer): a pile of the held items and the fluid.
 */
public class CrushingTubRenderer implements BlockEntityRenderer<CrushingTubBlockEntity> {
	private static final float LAYER_HEIGHT = 0.0625F;

	public CrushingTubRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(CrushingTubBlockEntity tub, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
		renderItemPile(tub, tub.getItems().getStackInSlot(0), 8, poseStack, buffers, packedLight, packedOverlay);
		if (!tub.getTank().isEmpty()) {
			float fill = tub.getTank().getFluidAmount() / (float) tub.getTank().getCapacity();
			FluidRendering.renderSurface(poseStack, buffers, tub.getTank().getFluid(), 0.0625F, 0.9375F, 0.0625F + 0.5F * fill, packedLight);
		}
	}

	/**
	 * Renders items lying flat, one layer per {@code itemsPerLayer} items, each layer randomly
	 * turned (the same way for a given block).
	 */
	public static void renderItemPile(BlockEntity blockEntity, ItemStack stack, int itemsPerLayer, PoseStack poseStack, MultiBufferSource buffers,
			int packedLight, int packedOverlay) {
		if (stack.isEmpty()) return;
		RandomSource random = RandomSource.create(blockEntity.getBlockPos().asLong());
		int layers = (stack.getCount() + itemsPerLayer - 1) / itemsPerLayer;
		for (int layer = 0; layer < layers; layer++) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.062F + layer * LAYER_HEIGHT, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90F));
			poseStack.scale(0.5F, 0.5F, 0.5F);
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffers,
					blockEntity.getLevel(), 0);
			poseStack.popPose();
		}
	}
}
