package nadiendev.rusticrevived.client.misc;

import java.lang.reflect.Field;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Client side of the Fullmetal effect: the player can not move or jump, speed effects do not change
 * the field of view and the screen is covered by a metal overlay that flickers when the effect ends.
 */
public final class FullmetalClient {
	public static final Identifier OVERLAY_LAYER = RusticRevived.id("fullmetal_overlay");
	private static final Identifier OVERLAY_TEXTURE = RusticRevived.id("textures/misc/fullmetal_overlay.png");
	/** GUI textured pipeline with the legacy overlay blending (source color, one minus source alpha). */
	private static final RenderPipeline OVERLAY_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(RusticRevived.id("pipeline/fullmetal_overlay"))
			.withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE,
					DestFactor.ZERO)))
			.build();
	/** Virtual texture size used to pass fractional (and mirrored) UVs to the integer based blit. */
	private static final int UV_SCALE = 1 << 16;
	private static final float BASE_ALPHA = 0.9625F;
	/** Half height of the legacy overlay quad as seen through the 70 degree hand camera, in texture units. */
	private static final float HALF_VIEW = 0.5F * (float) Math.tan(Math.toRadians(35));
	/** The movement vector computed from the keys has no setter. */
	private static final Field MOVE_VECTOR = moveVectorField();

	private FullmetalClient() {
	}

	public static void registerPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(OVERLAY_PIPELINE);
	}

	/** Only the bow zoom changes the field of view (movement speed modifiers are ignored). */
	public static void onComputeFov(ComputeFovModifierEvent event) {
		Player player = event.getPlayer();
		if (!player.hasEffect(ModEffects.FULLMETAL)) return;
		float fov = 1.0F;
		if (player.isUsingItem() && player.getUseItem().is(Items.BOW)) {
			float draw = player.getTicksUsingItem() / 20.0F;
			draw = draw > 1.0F ? 1.0F : draw * draw;
			fov *= 1.0F - draw * 0.15F;
		}
		event.setNewFovModifier(fov);
	}

	/** Movement and jump keys are ignored (sneaking and sprinting are kept). */
	public static void onMovementInput(MovementInputUpdateEvent event) {
		if (!event.getEntity().hasEffect(ModEffects.FULLMETAL)) return;
		ClientInput input = event.getInput();
		Input keys = input.keyPresses;
		input.keyPresses = new Input(false, false, false, false, false, keys.shift(), keys.sprint());
		try {
			MOVE_VECTOR.set(input, Vec2.ZERO);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** GUI layer (below everything else) drawing the metal overlay over the view. */
	public static void renderOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;
		MobEffectInstance effect = minecraft.player.getEffect(ModEffects.FULLMETAL);
		if (effect == null) return;

		float alpha = BASE_ALPHA * durationFade(effect, deltaTracker.getGameTimeDeltaPartialTick(false));
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		// legacy UVs: the texture spans the middle half of a quad seen through the hand camera, mirrored horizontally
		float halfU = HALF_VIEW * width / height;
		float uLeft = 0.5F + halfU;
		float vTop = 0.5F - HALF_VIEW;
		graphics.blit(OVERLAY_PIPELINE, OVERLAY_TEXTURE, 0, 0, uLeft * UV_SCALE, vTop * UV_SCALE, width, height,
				Math.round(-2F * halfU * UV_SCALE), Math.round(2F * HALF_VIEW * UV_SCALE), UV_SCALE, UV_SCALE, ARGB.white(alpha));
	}

	/** During the last 5 seconds the overlay flickers faster and faster while fading out. */
	private static float durationFade(MobEffectInstance effect, float partialTick) {
		if (effect.isInfiniteDuration() || effect.getDuration() >= 100) return 1.0F;
		float duration = (effect.getDuration() + partialTick) * 2F;
		float remaining = 10 - duration / 20;
		float fade = 0.5F + Mth.clamp(duration / 400F, 0.0F, 0.9375F - 0.5F)
				+ Mth.cos((200 - duration) * Mth.PI / 200F * 9F) * Mth.clamp(remaining / 10.0F * 0.25F, 0.0625F, 0.25F);
		fade = Mth.sqrt(fade);
		if (duration < 20) {
			fade *= Mth.sqrt(duration / 20F);
		}
		return fade;
	}

	private static Field moveVectorField() {
		try {
			Field field = ClientInput.class.getDeclaredField("moveVector");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}
}
