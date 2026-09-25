package nadiendev.rusticrevived.client.farm;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.quad.QuadTransforms;

/**
 * Shifts a block model horizontally by the same position based offset vanilla uses for flowers
 * (legacy {@code EnumOffsetType.XZ} of wildberry bushes, toggled by the client config
 * {@code offsetWildberryBushes}). Only the rendering moves; the block's shape stays centred, which
 * is why the vanilla {@code OffsetType.XZ} block property (it also moves the outline) is not used.
 */
public class OffsetBushModel extends DelegateBlockStateModel {
	private static final float MAX_OFFSET = 0.25F;

	public OffsetBushModel(BlockStateModel delegate) {
		super(delegate);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		List<BlockStateModelPart> original = new ArrayList<>();
		delegate.collectParts(level, pos, state, random, original);
		Transformation translation = offset(pos);
		for (BlockStateModelPart part : original) {
			parts.add(new OffsetPart(part, translation));
		}
	}

	private static Transformation offset(BlockPos pos) {
		long seed = Mth.getSeed(pos.getX(), 0, pos.getZ());
		float x = Mth.clamp(((seed & 15L) / 15.0F - 0.5F) * 0.5F, -MAX_OFFSET, MAX_OFFSET);
		float z = Mth.clamp(((seed >> 8 & 15L) / 15.0F - 0.5F) * 0.5F, -MAX_OFFSET, MAX_OFFSET);
		return new Transformation(new Vector3f(x, 0.0F, z), null, null, null);
	}

	/** A model part whose quads are translated. */
	private record OffsetPart(BlockStateModelPart part, Transformation translation) implements BlockStateModelPart {
		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction) {
			List<BakedQuad> quads = part.getQuads(direction);
			List<BakedQuad> moved = new ArrayList<>(quads.size());
			for (BakedQuad quad : quads) {
				moved.add(QuadTransforms.applyTransformation(quad, translation));
			}
			return moved;
		}

		@Override
		@Deprecated
		public boolean useAmbientOcclusion() {
			return part.useAmbientOcclusion();
		}

		@Override
		public TriState ambientOcclusion() {
			return part.ambientOcclusion();
		}

		@Override
		public Material.Baked particleMaterial() {
			return part.particleMaterial();
		}

		@Override
		public int materialFlags() {
			return part.materialFlags();
		}
	}
}
