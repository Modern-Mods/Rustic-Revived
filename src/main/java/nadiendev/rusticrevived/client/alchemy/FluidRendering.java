package nadiendev.rusticrevived.client.alchemy;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import nadiendev.rusticrevived.fluid.BoozeFluidType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fluid drawing for the alchemy GUIs and block entity renderers (legacy FluidClientUtil and the
 * fluid parts of the tank renderers), plus the tank tooltips.
 */
public final class FluidRendering {
	private static final int TILE = 16;

	private FluidRendering() {
	}

	public static TextureAtlasSprite stillSprite(FluidStack fluid) {
		IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
		return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(fluid));
	}

	public static int tint(FluidStack fluid) {
		return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
	}

	/** Draws the fluid of a GUI tank, filling it from the bottom proportionally to its amount. */
	public static void drawTank(GuiGraphics graphics, FluidStack fluid, int capacity, int x, int y, int width, int height) {
		if (fluid.isEmpty() || capacity <= 0) return;
		int fluidHeight = Math.min(height, (int) ((long) height * fluid.getAmount() / capacity));
		if (fluidHeight <= 0) return;
		TextureAtlasSprite sprite = stillSprite(fluid);
		int color = tint(fluid);
		RenderSystem.setShaderTexture(0, sprite.atlasLocation());
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		Matrix4f pose = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		for (int drawn = 0; drawn < fluidHeight; drawn += TILE) {
			int segment = Math.min(TILE, fluidHeight - drawn);
			int bottom = y + height - drawn;
			int top = bottom - segment;
			float v0 = sprite.getV((TILE - segment) / (float) TILE);
			for (int dx = 0; dx < width; dx += TILE) {
				int segmentWidth = Math.min(TILE, width - dx);
				float u1 = sprite.getU(segmentWidth / (float) TILE);
				int left = x + dx;
				int right = left + segmentWidth;
				buffer.addVertex(pose, left, bottom, 0).setUv(sprite.getU0(), sprite.getV1()).setColor(color);
				buffer.addVertex(pose, right, bottom, 0).setUv(u1, sprite.getV1()).setColor(color);
				buffer.addVertex(pose, right, top, 0).setUv(u1, v0).setColor(color);
				buffer.addVertex(pose, left, top, 0).setUv(sprite.getU0(), v0).setColor(color);
			}
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.disableBlend();
	}

	/** Tooltip of a GUI tank: fluid name, amount / capacity and the booze quality. */
	public static List<Component> tankTooltip(FluidStack fluid, int capacity) {
		List<Component> lines = new ArrayList<>();
		if (fluid.isEmpty()) {
			lines.add(Component.translatable("tooltip.rusticrevived.empty").withStyle(ChatFormatting.GRAY));
			lines.add(Component.literal("0/" + capacity).withStyle(ChatFormatting.GRAY));
			return lines;
		}
		MutableComponent name = fluid.getHoverName().copy();
		Rarity rarity = fluid.getFluidType().getRarity(fluid);
		lines.add(rarity == Rarity.COMMON ? name : name.withStyle(rarity.getStyleModifier()));
		lines.add(Component.literal(fluid.getAmount() + "/" + capacity).withStyle(ChatFormatting.GRAY));
		if (fluid.getFluidType() instanceof BoozeFluidType && BoozeFluidType.hasQuality(fluid)) {
			lines.add(BoozeQuality.tooltip(BoozeFluidType.getQuality(fluid)));
		}
		return lines;
	}

	/**
	 * Renders the flat surface of the fluid held by a block, in block coordinates.
	 *
	 * @param min lowest x / z of the surface
	 * @param max highest x / z of the surface
	 * @param y   height of the surface
	 */
	public static void renderSurface(PoseStack poseStack, MultiBufferSource buffers, FluidStack fluid, float min, float max, float y, int packedLight) {
		TextureAtlasSprite sprite = stillSprite(fluid);
		int color = tint(fluid);
		int blockLight = Math.max(LightTexture.block(packedLight), fluid.getFluidType().getLightLevel(fluid));
		int light = LightTexture.pack(blockLight, LightTexture.sky(packedLight));
		float u0 = sprite.getU(min);
		float u1 = sprite.getU(max);
		float v0 = sprite.getV(min);
		float v1 = sprite.getV(max);
		VertexConsumer consumer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
		PoseStack.Pose pose = poseStack.last();
		vertex(consumer, pose, min, y, min, u0, v0, color, light);
		vertex(consumer, pose, min, y, max, u0, v1, color, light);
		vertex(consumer, pose, max, y, max, u1, v1, color, light);
		vertex(consumer, pose, max, y, min, u1, v0, color, light);
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light) {
		consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
