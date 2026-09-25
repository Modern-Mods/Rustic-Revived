package nadiendev.rusticrevived.client.storage;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Cabinet item renderer (legacy CabinetRenderer.CabinetTEISR): a closed single cabinet tinted by the
 * stack's cabinet_material.
 */
public class CabinetItemRenderer extends BlockEntityWithoutLevelRenderer {
	private final CabinetModel model;

	public CabinetItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
		model = new CabinetModel(Minecraft.getInstance().getEntityModels().bakeLayer(CabinetModel.SINGLE));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			int packedOverlay) {
		CabinetRenderer.renderCabinet(model, false, stack.get(ModDataComponents.CABINET_MATERIAL.get()), Direction.SOUTH, 0.0F, poseStack, buffer,
				packedLight, packedOverlay);
	}
}
