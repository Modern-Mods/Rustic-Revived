package nadiendev.rusticrevived.client.storage;

import java.util.function.Consumer;

import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Cabinet item renderer (legacy CabinetRenderer.CabinetTEISR): a closed single cabinet tinted by the
 * stack's cabinet_material. Used by the cabinet client item as {@code rusticrevived:cabinet} special model.
 */
public class CabinetSpecialRenderer implements SpecialModelRenderer<Item> {
	public static final Identifier ID = RusticRevived.id("cabinet");

	private final CabinetModel model;

	public CabinetSpecialRenderer(CabinetModel model) {
		this.model = model;
	}

	@Override
	public @Nullable Item extractArgument(ItemStack stack) {
		return stack.get(ModDataComponents.CABINET_MATERIAL.get());
	}

	@Override
	public void submit(@Nullable Item material, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords,
			boolean hasFoil, int outlineColor) {
		CabinetRenderer.submitCabinet(model, false, material, Direction.SOUTH, 0.0F, poseStack, collector, lightCoords, overlayCoords,
				outlineColor, null);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		CabinetRenderer.applyTransform(poseStack, Direction.SOUTH);
		model.setupAnim(0.0F);
		model.root().getExtentsForGui(poseStack, output);
	}

	/** The special model type: no parameters. */
	public record Unbaked() implements SpecialModelRenderer.Unbaked<Item> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public CabinetSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
			return new CabinetSpecialRenderer(new CabinetModel(context.entityModelSet().bakeLayer(CabinetModel.SINGLE)));
		}
	}
}
