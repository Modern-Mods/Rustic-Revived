package nadiendev.rusticrevived.client.misc;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * Client side of the Fullmetal effect: the player can not move or jump, speed effects do not change
 * the field of view and the screen is covered by a metal overlay that flickers when the effect ends.
 */
public final class FullmetalClient {
	public static final ResourceLocation OVERLAY_LAYER = RusticRevived.id("fullmetal_overlay");
	private static final ResourceLocation OVERLAY_TEXTURE = RusticRevived.id("textures/misc/fullmetal_overlay.png");
	private static final float BASE_ALPHA = 0.9625F;
	/** Half height of the legacy overlay quad as seen through the 70 degree hand camera, in texture units. */
	private static final float HALF_VIEW = 0.5F * (float) Math.tan(Math.toRadians(35));

	private FullmetalClient() {
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

	public static void onMovementInput(MovementInputUpdateEvent event) {
		if (!event.getEntity().hasEffect(ModEffects.FULLMETAL)) return;
		Input input = event.getInput();
		input.jumping = false;
		input.forwardImpulse = 0F;
		input.leftImpulse = 0F;
		input.up = false;
		input.down = false;
		input.left = false;
		input.right = false;
	}

	/** GUI layer (below everything else) drawing the metal overlay over the view. */
	public static void renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;
		MobEffectInstance effect = minecraft.player.getEffect(ModEffects.FULLMETAL);
		if (effect == null) return;

		float alpha = BASE_ALPHA * durationFade(effect, deltaTracker.getGameTimeDeltaPartialTick(false));
		float width = graphics.guiWidth();
		float height = graphics.guiHeight();
		// legacy UVs: the texture spans the middle half of a quad seen through the hand camera, mirrored horizontally
		float halfU = HALF_VIEW * width / height;
		float uLeft = 0.5F + halfU;
		float uRight = 0.5F - halfU;
		float vTop = 0.5F - HALF_VIEW;
		float vBottom = 0.5F + HALF_VIEW;

		graphics.flush();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, OVERLAY_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
		Matrix4f pose = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.addVertex(pose, 0F, height, 0F).setUv(uLeft, vBottom);
		buffer.addVertex(pose, width, height, 0F).setUv(uRight, vBottom);
		buffer.addVertex(pose, width, 0F, 0F).setUv(uRight, vTop);
		buffer.addVertex(pose, 0F, 0F, 0F).setUv(uLeft, vTop);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableDepthTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
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
}
