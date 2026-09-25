package nadiendev.rusticrevived.client.misc;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Draws a metallic skin over living entities under Iron Skin or Fullmetal (legacy LayerIronSkin).
 * The model is rendered a second time with the {@link IronSkinTextures metal texture}; the more
 * powerful the effect, the more opaque the metal.
 */
public class IronSkinLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final RenderLayerParent<T, M> renderer;

	public IronSkinLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		float opacity = opacity(entity);
		if (opacity <= 0F || isDisabled(entity.getType())) return;
		ResourceLocation metal = IronSkinTextures.INSTANCE.get(renderer.getTextureLocation(entity));
		if (metal == null) return;
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(metal)), packedLight,
				LivingEntityRenderer.getOverlayCoords(entity, 0F), FastColor.ARGB32.colorFromFloat(opacity, 1F, 1F, 1F));
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
		if (type.is(ModTags.EntityTypes.NO_IRON_SKIN_LAYER)) return true;
		return RusticConfig.CLIENT.ironSkinRenderBlacklist.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
	}
}
