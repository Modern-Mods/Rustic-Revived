package nadiendev.rusticrevived.client.decor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import nadiendev.rusticrevived.block.decor.ClayWallDiagBlock;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client setup of the decor subsystem: the (invisible) chair seat renderer, the foliage tint of leafy lattice and
 * the placement cross drawn on floors and ceilings while holding a diagonal clay wall (legacy EventHandlerClient).
 */
public final class DecorClientSetup {
	/** Tint index of the leaves on lattice models. */
	private static final int LATTICE_LEAVES_TINT = 1;

	private DecorClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(DecorClientSetup::registerRenderers);
		modBus.addListener(DecorClientSetup::registerBlockColors);
		NeoForge.EVENT_BUS.addListener(DecorClientSetup::onBlockHighlight);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.CHAIR.get(), NoopRenderer::new);
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, tintIndex) -> {
			if (tintIndex != LATTICE_LEAVES_TINT) {
				return -1;
			}
			return level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
		}, ModBlocks.IRON_LATTICE.get());
	}

	/**
	 * While a diagonal clay wall is held, draws a cross on the targeted floor or ceiling face: the four triangles
	 * are the zones choosing each facing (see {@link ClayWallDiagBlock#getStateForPlacement}).
	 */
	private static void onBlockHighlight(RenderHighlightEvent.Block event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;
		BlockHitResult hit = event.getTarget();
		if (player == null || level == null || hit.getDirection().getAxis() != Direction.Axis.Y
				|| !(Block.byItem(placedStack(player.getMainHandItem(), player.getOffhandItem()).getItem()) instanceof ClayWallDiagBlock)) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		VoxelShape shape = state.getShape(level, pos, CollisionContext.of(player));
		if (state.isAir() || shape.isEmpty() || !level.getWorldBorder().isWithinBounds(pos)) {
			return;
		}
		AABB box = shape.bounds().inflate(0.002);
		if (Math.abs(box.getCenter().x - 0.5) > 1.0E-4 || Math.abs(box.getCenter().z - 0.5) > 1.0E-4) {
			return;
		}
		float min = (float) Math.max(box.minX, box.minZ);
		float max = (float) Math.min(box.maxX, box.maxZ);
		float y = (float) (hit.getDirection() == Direction.UP ? box.maxY : box.minY);
		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
		VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
		line(lines, poseStack.last(), min, y, min, max, max);
		line(lines, poseStack.last(), min, y, max, max, min);
		poseStack.popPose();
	}

	/** The stack the player would place: the main hand block item, else the off hand item (legacy logic). */
	private static ItemStack placedStack(ItemStack mainHand, ItemStack offHand) {
		boolean mainHandPlaces = !mainHand.isEmpty() && mainHand.getItem() instanceof BlockItem;
		return !mainHandPlaces && !offHand.isEmpty() ? offHand : mainHand;
	}

	/** Horizontal line from (x1, y, z1) to (x2, y, z2). */
	private static void line(VertexConsumer consumer, PoseStack.Pose pose, float x1, float y, float z1, float x2, float z2) {
		float length = Mth.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
		float normalX = (x2 - x1) / length;
		float normalZ = (z2 - z1) / length;
		consumer.addVertex(pose, x1, y, z1).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, normalX, 0.0F, normalZ);
		consumer.addVertex(pose, x2, y, z2).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, normalX, 0.0F, normalZ);
	}
}
