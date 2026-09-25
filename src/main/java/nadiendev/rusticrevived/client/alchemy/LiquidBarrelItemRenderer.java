package nadiendev.rusticrevived.client.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.item.LiquidBarrelItem;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Liquid barrel item renderer (legacy LiquidBarrelFilledItemModel): the barrel block model with
 * the fluid it holds.
 */
public class LiquidBarrelItemRenderer extends BlockEntityWithoutLevelRenderer {

	public LiquidBarrelItemRenderer(Minecraft minecraft) {
		super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
			int packedOverlay) {
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ModBlocks.LIQUID_BARREL.get().defaultBlockState(), poseStack, buffers,
				packedLight, packedOverlay);
		LiquidBarrelRenderer.renderFluid(LiquidBarrelItem.getFluid(stack), poseStack, buffers, packedLight);
	}
}
