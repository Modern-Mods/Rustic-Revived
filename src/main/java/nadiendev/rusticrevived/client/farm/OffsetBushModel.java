package nadiendev.rusticrevived.client.farm;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.math.Transformation;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Shifts a block model horizontally by the same position based offset vanilla uses for flowers
 * (legacy {@code EnumOffsetType.XZ} of wildberry bushes, toggled by the client config
 * {@code offsetWildberryBushes}). Only the rendering moves; the block's shape stays centred.
 */
public class OffsetBushModel extends BakedModelWrapper<BakedModel> {
	private static final ModelProperty<Vec3> OFFSET = new ModelProperty<>();
	private static final float MAX_OFFSET = 0.25F;

	public OffsetBushModel(BakedModel original) {
		super(original);
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
		return super.getModelData(level, pos, state, modelData).derive().with(OFFSET, offset(pos)).build();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData,
			@Nullable RenderType renderType) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData, renderType);
		Vec3 offset = extraData.get(OFFSET);
		if (offset == null || quads.isEmpty()) {
			return quads;
		}
		Transformation translation = new Transformation(new Vector3f((float) offset.x, 0.0F, (float) offset.z), null, null, null);
		return QuadTransformers.applying(translation).process(quads);
	}

	private static Vec3 offset(BlockPos pos) {
		long seed = Mth.getSeed(pos.getX(), 0, pos.getZ());
		double x = Mth.clamp(((seed & 15L) / 15.0F - 0.5) * 0.5, -MAX_OFFSET, MAX_OFFSET);
		double z = Mth.clamp(((seed >> 8 & 15L) / 15.0F - 0.5) * 0.5, -MAX_OFFSET, MAX_OFFSET);
		return new Vec3(x, 0.0, z);
	}
}
