package nadiendev.rusticrevived.client.decor;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import nadiendev.rusticrevived.block.decor.ClayWallDiagBlock;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client setup of the decor subsystem: the (invisible) chair seat renderer, the foliage tint of leafy lattice and
 * the placement cross drawn on floors and ceilings while holding a diagonal clay wall (legacy EventHandlerClient).
 * The lattice item tint is declared by its client item definition (datagen).
 */
public final class DecorClientSetup {
	/** Color of the placement cross: the vanilla block outline color. */
	private static final int CROSS_COLOR = ARGB.black(102);

	private DecorClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(DecorClientSetup::registerRenderers);
		modBus.addListener(DecorClientSetup::registerBlockColors);
		NeoForge.EVENT_BUS.addListener(DecorClientSetup::onExtractBlockOutline);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.CHAIR.get(), NoopRenderer::new);
	}

	/** Lattice models tint their leaves with tint index 1 (index 0, the iron, is untinted). */
	private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
		event.register(List.of(BlockTintSources.constant(-1), BlockTintSources.foliage()), ModBlocks.IRON_LATTICE.get());
	}

	/**
	 * While a diagonal clay wall is held, draws a cross on the targeted floor or ceiling face: the four triangles
	 * are the zones choosing each facing (see {@link ClayWallDiagBlock#getStateForPlacement}).
	 */
	private static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		BlockHitResult hit = event.getHitResult();
		if (player == null || hit.getDirection().getAxis() != Direction.Axis.Y
				|| !(Block.byItem(placedStack(player.getMainHandItem(), player.getOffhandItem()).getItem()) instanceof ClayWallDiagBlock)) {
			return;
		}
		BlockPos pos = event.getBlockPos();
		VoxelShape shape = event.getBlockState().getShape(event.getLevel(), pos, event.getCollisionContext());
		if (shape.isEmpty()) {
			return;
		}
		AABB box = shape.bounds().inflate(0.002);
		if (Math.abs(box.getCenter().x - 0.5) > 1.0E-4 || Math.abs(box.getCenter().z - 0.5) > 1.0E-4) {
			return;
		}
		float min = (float) Math.max(box.minX, box.minZ);
		float max = (float) Math.min(box.maxX, box.maxZ);
		float y = (float) (hit.getDirection() == Direction.UP ? box.maxY : box.minY);
		event.addCustomRenderer((state, buffer, poseStack, translucentPass, levelState) -> {
			renderCross(state, buffer, poseStack, translucentPass, levelState, min, max, y);
			return false;
		});
	}

	/** Draws the cross in the same pass as the vanilla outline of the targeted block, which is still drawn. */
	private static void renderCross(BlockOutlineRenderState state, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass,
			LevelRenderState levelState, float min, float max, float y) {
		if (state.isTranslucent() != translucentPass) {
			return;
		}
		BlockPos pos = state.pos();
		Vec3 camera = levelState.cameraRenderState.pos;
		float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
		poseStack.pushPose();
		poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
		VertexConsumer lines = buffer.getBuffer(RenderTypes.lines());
		line(lines, poseStack.last(), width, min, y, min, max, max);
		line(lines, poseStack.last(), width, min, y, max, max, min);
		poseStack.popPose();
	}

	/** The stack the player would place: the main hand block item, else the off hand item (legacy logic). */
	private static ItemStack placedStack(ItemStack mainHand, ItemStack offHand) {
		boolean mainHandPlaces = !mainHand.isEmpty() && mainHand.getItem() instanceof BlockItem;
		return !mainHandPlaces && !offHand.isEmpty() ? offHand : mainHand;
	}

	/** Horizontal line from (x1, y, z1) to (x2, y, z2). */
	private static void line(VertexConsumer consumer, PoseStack.Pose pose, float width, float x1, float y, float z1, float x2, float z2) {
		float length = Mth.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
		float normalX = (x2 - x1) / length;
		float normalZ = (z2 - z1) / length;
		consumer.addVertex(pose, x1, y, z1).setColor(CROSS_COLOR).setNormal(pose, normalX, 0.0F, normalZ).setLineWidth(width);
		consumer.addVertex(pose, x2, y, z2).setColor(CROSS_COLOR).setNormal(pose, normalX, 0.0F, normalZ).setLineWidth(width);
	}
}
