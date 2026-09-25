package nadiendev.rusticrevived.client.storage;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.storage.CabinetBlock;
import nadiendev.rusticrevived.blockentity.storage.CabinetBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws cabinets (legacy CabinetRenderer): the lower block of a double cabinet draws both halves, the
 * upper one nothing. Cabinets made from a single kind of planks use the grey texture tinted with the
 * average colour of those planks; the others use the coloured default texture.
 */
public class CabinetRenderer implements BlockEntityRenderer<CabinetBlockEntity, CabinetRenderState> {
	private static final Identifier TEXTURE = RusticRevived.id("textures/models/cabinet.png");
	private static final Identifier TEXTURE_DOUBLE = RusticRevived.id("textures/models/cabinet_double.png");
	private static final Identifier TEXTURE_COLOR = RusticRevived.id("textures/models/cabinet_color.png");
	private static final Identifier TEXTURE_DOUBLE_COLOR = RusticRevived.id("textures/models/cabinet_double_color.png");

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
	public CabinetRenderState createRenderState() {
		return new CabinetRenderState();
	}

	@Override
	public void extractRenderState(CabinetBlockEntity cabinet, CabinetRenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(cabinet, state, partialTicks, cameraPosition, breakProgress);
		BlockState blockState = cabinet.getBlockState();
		state.visible = blockState.getBlock() instanceof CabinetBlock && !blockState.getValue(CabinetBlock.TOP);
		if (!state.visible) return;
		state.mirror = blockState.getValue(CabinetBlock.MIRROR);
		state.isDouble = blockState.getValue(CabinetBlock.BOTTOM);
		state.facing = blockState.getValue(CabinetBlock.FACING);
		state.material = cabinet.getMaterial();
		float closed = 1.0F - cabinet.getOpenness(partialTicks);
		float openness = 1.0F - closed * closed * closed;
		state.doorAngle = openness * Mth.HALF_PI * (state.mirror ? -1 : 1);
	}

	@Override
	public void submit(CabinetRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.visible) return;
		CabinetModel model = state.isDouble ? state.mirror ? doubleMirror : doubleModel : state.mirror ? singleMirror : single;
		submitCabinet(model, state.isDouble, state.material, state.facing, state.doorAngle, poseStack, collector, state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
	}

	/** Submits a cabinet model in the unit cube at the pose origin (also used for the item). */
	static void submitCabinet(CabinetModel model, boolean isDouble, @Nullable Item material, Direction facing, float doorAngle,
			PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, int outlineColor,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		Identifier texture;
		int color;
		if (material == null) {
			texture = isDouble ? TEXTURE_DOUBLE_COLOR : TEXTURE_COLOR;
			color = 0xFFFFFFFF;
		} else {
			texture = isDouble ? TEXTURE_DOUBLE : TEXTURE;
			color = MaterialColors.get(material);
		}
		poseStack.pushPose();
		applyTransform(poseStack, facing);
		collector.submitModel(model, doorAngle, poseStack, model.renderType(texture), lightCoords, overlayCoords, color, null, outlineColor,
				breakProgress);
		poseStack.popPose();
	}

	/** Legacy model space: y down, origin at the model centre, front facing south when unrotated. */
	static void applyTransform(PoseStack poseStack, Direction facing) {
		poseStack.translate(0.0F, 1.0F, 1.0F);
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotation(facing)));
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		poseStack.translate(0.5F, -0.5F, 0.5F);
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
