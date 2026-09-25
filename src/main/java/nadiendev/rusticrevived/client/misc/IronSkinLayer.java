package nadiendev.rusticrevived.client.misc;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Draws a metallic skin over living entities under Iron Skin or Fullmetal (legacy LayerIronSkin).
 * The model is rendered a second time with the {@link IronSkinTextures metal texture}; the more
 * powerful the effect, the more opaque the metal. The opacity is extracted into the render state
 * by {@link MetalSkinClient#registerRenderStateModifiers}.
 */
public class IronSkinLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
	/** Opacity of the metal skin, absent when the entity has no (visible) metal skin. */
	public static final ContextKey<Float> OPACITY = new ContextKey<>(RusticRevived.id("iron_skin_opacity"));

	private final LivingEntityRenderer<?, S, M> renderer;

	public IronSkinLayer(LivingEntityRenderer<?, S, M> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
		Float opacity = state.getRenderData(OPACITY);
		if (opacity == null) return;
		Identifier metal = IronSkinTextures.INSTANCE.get(renderer.getTextureLocation(state));
		if (metal == null) return;
		submitNodeCollector.order(1).submitModel(getParentModel(), state, poseStack, RenderTypes.entityTranslucent(metal), lightCoords,
				LivingEntityRenderer.getOverlayCoords(state, 0F), ARGB.colorFromFloat(opacity, 1F, 1F, 1F), null, state.outlineColor, null);
	}

	/** Opacity of the metal skin of {@code entity}, 0 when it has no metal skin (invisible or no effect). */
	public static float opacity(LivingEntity entity) {
		if (entity.isInvisible()) return 0F;
		if (entity.hasEffect(ModEffects.FULLMETAL)) return 0.92F;
		MobEffectInstance ironSkin = entity.getEffect(ModEffects.IRON_SKIN);
		if (ironSkin == null) return 0F;
		return switch (ironSkin.getAmplifier()) {
			case 0 -> 0.875F;
			case 1 -> 0.89F;
			case 2 -> 0.91F;
			default -> 0.92F;
		};
	}

	/** Entities excluded by the {@code no_iron_skin_layer} tag or the client config blacklist. */
	public static boolean isDisabled(EntityType<?> type) {
		if (type.builtInRegistryHolder().is(ModTags.EntityTypes.NO_IRON_SKIN_LAYER)) return true;
		return RusticConfig.CLIENT.ironSkinRenderBlacklist.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
	}
}
