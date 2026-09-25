package nadiendev.rusticrevived.client.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;

/**
 * Iron Skin / Fullmetal visuals on entities: the {@link IronSkinLayer} on every living entity renderer
 * and player skin, and the metal skin on the first person arm.
 */
public final class MetalSkinClient {
	private MetalSkinClient() {
	}

	/** Adds the iron skin layer to every living entity renderer (except armor stands) and player skin. */
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		for (EntityType<?> type : event.getEntityTypes()) {
			EntityRenderer<?> renderer = event.getRenderer(type);
			if (renderer instanceof LivingEntityRenderer<?, ?> living && !(renderer instanceof ArmorStandRenderer)) {
				addLayer(living);
			}
		}
		for (PlayerSkin.Model skin : event.getSkins()) {
			if (event.getSkin(skin) instanceof PlayerRenderer player) {
				addLayer(player);
			}
		}
	}

	private static <T extends LivingEntity, M extends EntityModel<T>> void addLayer(LivingEntityRenderer<T, M> renderer) {
		renderer.addLayer(new IronSkinLayer<>(renderer));
	}

	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(IronSkinTextures.INSTANCE);
	}

	/** Draws the metal skin over the first person arm (and sleeve) of a player under Iron Skin or Fullmetal. */
	public static void onRenderArm(RenderArmEvent event) {
		AbstractClientPlayer player = event.getPlayer();
		float opacity = IronSkinLayer.opacity(player);
		if (opacity <= 0F || IronSkinLayer.isDisabled(player.getType())) return;
		if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
		ResourceLocation metal = IronSkinTextures.INSTANCE.get(player.getSkin().texture());
		if (metal == null) return;

		event.setCanceled(true);
		PoseStack poseStack = event.getPoseStack();
		int light = event.getPackedLight();
		boolean right = event.getArm() == HumanoidArm.RIGHT;
		if (right) {
			renderer.renderRightHand(poseStack, event.getMultiBufferSource(), light, player);
		} else {
			renderer.renderLeftHand(poseStack, event.getMultiBufferSource(), light, player);
		}
		PlayerModel<AbstractClientPlayer> model = renderer.getModel();
		VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(metal));
		int color = FastColor.ARGB32.colorFromFloat(opacity, 1F, 1F, 1F);
		(right ? model.rightArm : model.leftArm).render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY, color);
		(right ? model.rightSleeve : model.leftSleeve).render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY, color);
	}
}
