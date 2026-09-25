package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chandelier ring (legacy BlockChandelier). Torches and candles can be mounted on its sides and on top of it.
 * It falls like an anvil, hurting what it lands on, unless it hangs from a sturdy face, a vertical rope or
 * chain, or a block of {@link ModTags.Blocks#CHANDELIER_SUPPORTS}.
 */
public class ChandelierBlock extends FallingBlock {
	public static final MapCodec<ChandelierBlock> CODEC = simpleCodec(ChandelierBlock::new);

	private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);
	/** Full sides for wall mounted lights, a center post on top for standing ones, nothing sturdy below. */
	private static final VoxelShape SUPPORT_SHAPE = Shapes.or(
			box(0, 0, 0, 16, 16, 1), box(0, 0, 15, 16, 16, 16), box(0, 0, 1, 1, 16, 15), box(15, 0, 1, 16, 16, 15),
			box(7, 8, 7, 9, 16, 9));
	/** Fall damage per block fallen and maximum damage (legacy values). */
	private static final float FALL_DAMAGE_PER_DISTANCE = 6.0F;
	private static final int FALL_DAMAGE_MAX = 400;

	public ChandelierBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends ChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!isSuspended(level, pos)) {
			super.tick(state, level, pos, random);
		}
	}

	private static boolean isSuspended(LevelReader level, BlockPos pos) {
		BlockPos abovePos = pos.above();
		BlockState above = level.getBlockState(abovePos);
		return above.isFaceSturdy(level, abovePos, Direction.DOWN)
				|| above.getBlock() instanceof RopeBaseBlock && above.getValue(RopeBaseBlock.AXIS) == Direction.Axis.Y
				|| above.is(ModTags.Blocks.CHANDELIER_SUPPORTS);
	}

	@Override
	protected void falling(FallingBlockEntity entity) {
		entity.setHurtsEntities(FALL_DAMAGE_PER_DISTANCE, FALL_DAMAGE_MAX);
	}

	@Override
	public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
		if (!fallingBlock.isSilent()) {
			level.levelEvent(LevelEvent.SOUND_ANVIL_LAND, pos, 0);
		}
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
		if (!fallingBlock.isSilent()) {
			level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
		}
	}

	/** Unused: chandeliers do not drip falling dust like sand does (see {@link #animateTick}). */
	@Override
	public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
		return 0xFF000000;
	}

	/** Chandeliers do not drip falling dust like sand does. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
		return SUPPORT_SHAPE;
	}
}
