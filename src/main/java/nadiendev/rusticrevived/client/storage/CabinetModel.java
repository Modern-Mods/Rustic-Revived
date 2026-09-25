package nadiendev.rusticrevived.client.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Cabinet geometry (legacy ModelCabinet / ModelCabinetDouble, same boxes and UVs). Coordinates use
 * the legacy y-down model space; {@link CabinetRenderer#renderCabinet} applies the legacy transform.
 */
public class CabinetModel {
	public static final ModelLayerLocation SINGLE = new ModelLayerLocation(RusticRevived.id("cabinet"), "main");
	public static final ModelLayerLocation SINGLE_MIRROR = new ModelLayerLocation(RusticRevived.id("cabinet"), "mirror");
	public static final ModelLayerLocation DOUBLE = new ModelLayerLocation(RusticRevived.id("cabinet_double"), "main");
	public static final ModelLayerLocation DOUBLE_MIRROR = new ModelLayerLocation(RusticRevived.id("cabinet_double"), "mirror");

	private final ModelPart root;
	private final ModelPart door;
	private final ModelPart handle;

	public CabinetModel(ModelPart root) {
		this.root = root;
		this.door = root.getChild("door");
		this.handle = root.getChild("handle");
	}

	public static LayerDefinition createSingle(boolean mirror) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 16, 1, 16), PartPose.offset(-8, 23, -8));
		root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(64, 0).addBox(0, 0, 0, 16, 1, 16), PartPose.offset(-8, 8, -8));
		root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 17).addBox(0, 0, 0, 16, 14, 1), PartPose.offset(-8, 9, 7));
		root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 32).addBox(0, 0, 0, 1, 14, 15), PartPose.offset(-8, 9, -8));
		root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(32, 32).addBox(0, 0, 0, 1, 14, 15), PartPose.offset(7, 9, -8));
		addDoor(root, mirror, 64, 17, 94, 17, 14, 16);
		return LayerDefinition.create(mesh, 128, 64);
	}

	public static LayerDefinition createDouble(boolean mirror) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 16, 1, 16), PartPose.offset(-8, 23, -8));
		root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(64, 0).addBox(0, 0, 0, 16, 1, 16), PartPose.offset(-8, -8, -8));
		root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 17).addBox(0, 0, 0, 16, 30, 1), PartPose.offset(-8, -7, 7));
		root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 48).addBox(0, 0, 0, 1, 30, 15), PartPose.offset(-8, -7, -8));
		root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(32, 48).addBox(0, 0, 0, 1, 30, 15), PartPose.offset(7, -7, -8));
		addDoor(root, mirror, 0, 93, 0, 124, 30, 8);
		return LayerDefinition.create(mesh, 128, 128);
	}

	/** Door and handle, hinged on the left (or on the right when mirrored). */
	private static void addDoor(PartDefinition root, boolean mirror, int doorU, int doorV, int handleU, int handleV, int doorHeight, float hingeY) {
		PartPose hinge = PartPose.offset(mirror ? 7 : -7, hingeY, -6.5F);
		root.addOrReplaceChild("door", CubeListBuilder.create().texOffs(doorU, doorV)
				.addBox(mirror ? -14 : 0, -doorHeight / 2F, -0.5F, 14, doorHeight, 1), hinge);
		root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(handleU, handleV)
				.addBox(mirror ? -13 : 12, -1, -1.5F, 1, 2, 1), hinge);
	}

	/** @param doorAngle door (and handle) rotation around the hinge, in radians */
	public void render(PoseStack poseStack, VertexConsumer consumer, float doorAngle, int packedLight, int packedOverlay, int color) {
		door.yRot = doorAngle;
		handle.yRot = doorAngle;
		root.render(poseStack, consumer, packedLight, packedOverlay, color);
	}
}
