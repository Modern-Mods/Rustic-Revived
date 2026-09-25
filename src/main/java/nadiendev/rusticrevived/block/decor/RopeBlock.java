package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Rope (legacy BlockRope): a climbable line held by sturdy faces, tied crop stakes, lattice and grape leaves
 * growing along the same axis. Arrows flying through it cut it.
 */
public class RopeBlock extends RopeBaseBlock {
	public static final MapCodec<RopeBlock> CODEC = simpleCodec(RopeBlock::new);
	/** How far next to the rope an arrow still cuts it, across the rope's axis. */
	private static final double ARROW_REACH = 0.125;

	public RopeBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends RopeBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean isSupport(BlockState other, LevelReader level, BlockPos otherPos, Direction face, Direction.Axis axis) {
		return other.isFaceSturdy(level, otherPos, face)
				|| other.is(ModBlocks.STAKE_TIED)
				|| other.is(ModBlocks.GRAPE_LEAVES) && axisOf(other) == axis
				|| other.getBlock() instanceof LatticeBlock;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (!level.isClientSide && entity instanceof AbstractArrow && isCutBy(state, level, pos, entity)) {
			level.destroyBlock(pos, true);
		}
	}

	private boolean isCutBy(BlockState state, Level level, BlockPos pos, Entity arrow) {
		Direction.Axis axis = state.getValue(AXIS);
		AABB reach = state.getShape(level, pos).bounds()
				.inflate(axis == Direction.Axis.X ? 0.0 : ARROW_REACH, axis == Direction.Axis.Y ? 0.0 : ARROW_REACH, axis == Direction.Axis.Z ? 0.0 : ARROW_REACH)
				.move(pos);
		return reach.intersects(arrow.getBoundingBox());
	}
}
