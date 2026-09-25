package nadiendev.rusticrevived.client.storage;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.storage.CabinetBlock;
import nadiendev.rusticrevived.blockentity.storage.CabinetBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Draws cabinets (legacy CabinetRenderer): the lower block of a double cabinet draws both halves, the
 * upper one nothing. Cabinets made from a single kind of planks use the grey texture tinted with the
 * average colour of those planks; the others use the coloured default texture.
 */
public class CabinetRenderer implements BlockEntityRenderer<CabinetBlockEntity> {
	private static final ResourceLocation TEXTURE = RusticRevived.id("textures/models/cabinet.png");
	private static final ResourceLocation TEXTURE_DOUBLE = RusticRevived.id("textures/models/cabinet_double.png");
	private static final ResourceLocation TEXTURE_COLOR = RusticRevived.id("textures/models/cabinet_color.png");
	private static final ResourceLocation TEXTURE_DOUBLE_COLOR = RusticRevived.id("textures/models/cabinet_double_color.png");

	private final CabinetModel single;
	private final CabinetModel singleMirror;
	private final CabinetModel doubleModel;
	private final CabinetModel doubleMirror;

	public CabinetRenderer(BlockEntityRendererProvider.Context context) {
		single = new CabinetModel(context.bakeLayer(CabinetModel.SINGLE));
		singleMirror = new CabinetModel(context.bakeLayer(CabinetModel.SINGLE_MIRROR));
		doubleModel = new CabinetModel(context.bakeLayer(CabinetModel.DOUBLE));
		doubleMirror = new CabinetModel(context.bakeLayer(CabinetModel.DOUBLE_MIRROR));
	}

	@Override
	public void render(CabinetBlockEntity cabinet, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			int packedOverlay) {
		BlockState state = cabinet.getBlockState();
		if (!(state.getBlock() instanceof CabinetBlock) || state.getValue(CabinetBlock.TOP)) return;
		boolean mirror = state.getValue(CabinetBlock.MIRROR);
		boolean isDouble = state.getValue(CabinetBlock.BOTTOM);
		CabinetModel model = isDouble ? mirror ? doubleMirror : doubleModel : mirror ? singleMirror : single;

		float closed = 1.0F - cabinet.getOpenness(partialTick);
		float openness = 1.0F - closed * closed * closed;
		float doorAngle = openness * Mth.HALF_PI * (mirror ? -1 : 1);
		renderCabinet(model, isDouble, cabinet.getMaterial(), state.getValue(CabinetBlock.FACING), doorAngle, poseStack, buffer, packedLight,
				packedOverlay);
	}

	/** Renders a cabinet model in the unit cube at the pose origin (also used for the item). */
	static void renderCabinet(CabinetModel model, boolean isDouble, @Nullable Item material, Direction facing, float doorAngle,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ResourceLocation texture;
		int color;
		if (material == null) {
			texture = isDouble ? TEXTURE_DOUBLE_COLOR : TEXTURE_COLOR;
			color = 0xFFFFFFFF;
		} else {
			texture = isDouble ? TEXTURE_DOUBLE : TEXTURE;
			color = MaterialColors.get(material);
		}
		poseStack.pushPose();
		// legacy model space: y down, origin at the model centre, front facing south when unrotated
		poseStack.translate(0.0F, 1.0F, 1.0F);
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotation(facing)));
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		poseStack.translate(0.5F, -0.5F, 0.5F);
		model.render(poseStack, buffer.getBuffer(RenderType.entityCutout(texture)), doorAngle, packedLight, packedOverlay, color);
		poseStack.popPose();
	}

	private static float rotation(Direction facing) {
		return switch (facing) {
			case WEST -> 90;
			case NORTH -> 180;
			case EAST -> 270;
			default -> 0;
		};
	}

	@Override
	public AABB getRenderBoundingBox(CabinetBlockEntity cabinet) {
		AABB box = new AABB(cabinet.getBlockPos());
		BlockState state = cabinet.getBlockState();
		return state.getBlock() instanceof CabinetBlock && state.getValue(CabinetBlock.BOTTOM) ? box.expandTowards(0, 1, 0) : box;
	}
}
