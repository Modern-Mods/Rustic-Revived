package nadiendev.rusticrevived.client.alchemy;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3x2f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import nadiendev.rusticrevived.fluid.BoozeFluidType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fluid drawing for the alchemy GUIs, block entity renderers and item renderers (legacy
 * FluidClientUtil and the fluid parts of the tank renderers), plus the tank tooltips. Sprites and
 * tints come from the fluid models registered in {@code ClientSetup#registerFluidModels}.
 */
public final class FluidRendering {
	private static final int TILE = 16;

	private FluidRendering() {
	}

	private static FluidModel model(FluidStack fluid) {
		return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
	}

	public static TextureAtlasSprite stillSprite(FluidStack fluid) {
		return model(fluid).stillMaterial().sprite();
	}

	/** Opaque ARGB tint of the fluid as a stack (white when the fluid model has no tint). */
	public static int tint(FluidStack fluid) {
		FluidModel model = model(fluid);
		return model.fluidTintSource() != null ? 0xFF000000 | model.fluidTintSource().colorAsStack(fluid) : 0xFFFFFFFF;
	}

	/** Draws the fluid of a GUI tank, filling it from the bottom proportionally to its amount. */
	public static void drawTank(GuiGraphicsExtractor graphics, FluidStack fluid, int capacity, int x, int y, int width, int height) {
		if (fluid.isEmpty() || capacity <= 0) return;
		int fluidHeight = Math.min(height, (int) ((long) height * fluid.getAmount() / capacity));
		if (fluidHeight <= 0) return;
		TextureAtlasSprite sprite = stillSprite(fluid);
		int color = tint(fluid);
		AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());
		TextureSetup texture = TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler());
		for (int drawn = 0; drawn < fluidHeight; drawn += TILE) {
			int segment = Math.min(TILE, fluidHeight - drawn);
			int bottom = y + height - drawn;
			int top = bottom - segment;
			float v0 = sprite.getV((TILE - segment) / (float) TILE);
			for (int dx = 0; dx < width; dx += TILE) {
				int segmentWidth = Math.min(TILE, width - dx);
				float u1 = sprite.getU(segmentWidth / (float) TILE);
				int left = x + dx;
				graphics.submitGuiElementRenderState(new BlitRenderState(RenderPipelines.GUI_TEXTURED, texture, new Matrix3x2f(graphics.pose()),
						left, top, left + segmentWidth, bottom, sprite.getU0(), u1, v0, sprite.getV1(), color, graphics.peekScissorStack()));
			}
		}
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
	 * Submits the flat surface of the fluid held by a block, in block coordinates.
	 *
	 * @param min lowest x / z of the surface
	 * @param max highest x / z of the surface
	 * @param y   height of the surface
	 */
	public static void renderSurface(PoseStack poseStack, SubmitNodeCollector collector, FluidStack fluid, float min, float max, float y, int lightCoords) {
		TextureAtlasSprite sprite = stillSprite(fluid);
		int color = tint(fluid);
		int light = LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(lightCoords), fluid.getFluidType().getLightLevel(fluid)),
				LightCoordsUtil.sky(lightCoords));
		float u0 = sprite.getU(min);
		float u1 = sprite.getU(max);
		float v0 = sprite.getV(min);
		float v1 = sprite.getV(max);
		collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCullItemTarget(sprite.atlasLocation()), (pose, consumer) -> {
			vertex(consumer, pose, min, y, min, u0, v0, color, light);
			vertex(consumer, pose, min, y, max, u0, v1, color, light);
			vertex(consumer, pose, max, y, max, u1, v1, color, light);
			vertex(consumer, pose, max, y, min, u1, v0, color, light);
		});
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light) {
		consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
