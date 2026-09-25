package nadiendev.rusticrevived.client.misc;

import com.google.common.reflect.TypeToken;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/**
 * Iron Skin / Fullmetal visuals on entities: the {@link IronSkinLayer} on every living entity renderer
 * and player (and mannequin) skin, and the metal skin on the first person arm.
 */
public final class MetalSkinClient {
	private MetalSkinClient() {
	}

	/** Adds the iron skin layer to every living entity renderer (except armor stands) and player skin. */
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		for (EntityType<?> type : event.getEntityTypes()) {
			EntityRenderer<?, ?> renderer = event.getRenderer(type);
			if (renderer instanceof LivingEntityRenderer<?, ?, ?> living && !(renderer instanceof ArmorStandRenderer)) {
				addLayer(living);
			}
		}
		for (PlayerModelType skin : event.getSkins()) {
			AvatarRenderer<AbstractClientPlayer> player = event.getPlayerRenderer(skin);
			if (player != null) {
				addLayer(player);
			}
			AvatarRenderer<ClientMannequin> mannequin = event.getMannequinRenderer(skin);
			if (mannequin != null) {
				addLayer(mannequin);
			}
		}
	}

	private static <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addLayer(
			LivingEntityRenderer<T, S, M> renderer) {
		renderer.addLayer(new IronSkinLayer<>(renderer));
	}

	/** Stores the metal skin opacity of living entities in their render state. */
	public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {
		}, (LivingEntity entity, LivingEntityRenderState state) -> {
			float opacity = IronSkinLayer.opacity(entity);
			state.setRenderData(IronSkinLayer.OPACITY, opacity > 0F && !IronSkinLayer.isDisabled(entity.getType()) ? opacity : null);
		});
	}

	public static void registerReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(RusticRevived.id("iron_skin_textures"), IronSkinTextures.INSTANCE);
	}

	/**
	 * Draws the metal skin over the first person arm (and sleeve) of a player under Iron Skin or
	 * Fullmetal. The event is fired right before the vanilla arm is submitted; the metal is submitted
	 * in a later order so it is drawn over it.
	 */
	public static void onRenderArm(RenderArmEvent event) {
		AbstractClientPlayer player = event.getPlayer();
		float opacity = IronSkinLayer.opacity(player);
		if (opacity <= 0F || IronSkinLayer.isDisabled(player.getType())) return;
		Identifier metal = IronSkinTextures.INSTANCE.get(player.getSkin().body().texturePath());
		if (metal == null) return;

		PlayerModel model = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player).getModel();
		event.getSubmitNodeCollector().order(1).submitModelPart(event.getArm() == HumanoidArm.RIGHT ? model.rightArm : model.leftArm,
				event.getPoseStack(), RenderTypes.entityTranslucent(metal), event.getPackedLight(), OverlayTexture.NO_OVERLAY, null,
				ARGB.colorFromFloat(opacity, 1F, 1F, 1F), null);
	}
}
