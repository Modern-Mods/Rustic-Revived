package nadiendev.rusticrevived.block.farm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Olive and ironwood building blocks that burn like vanilla wood. {@code FireBlock#setFlammable}
 * is private in 26.1, so the fire odds are given by the NeoForge block hooks instead: logs catch
 * fire like vanilla logs (5 / 5), planks and everything made of them like vanilla planks (5 / 20).
 */
public final class FlammableWoodBlocks {
	/** Fire spread speed (vanilla "ignite odds") of every Rustic wooden block. */
	public static final int SPREAD_SPEED = 5;
	public static final int LOG_FLAMMABILITY = 5;
	public static final int PLANKS_FLAMMABILITY = 20;

	private FlammableWoodBlocks() {
	}

	/** Planks and carved wood. */
	public static class Planks extends Block {
		public Planks(Properties properties) {
			super(properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return PLANKS_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}

	public static class Log extends RotatedPillarBlock {
		public Log(Properties properties) {
			super(properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return LOG_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}

	public static class Slab extends SlabBlock {
		public Slab(Properties properties) {
			super(properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return PLANKS_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}

	public static class Stairs extends StairBlock {
		public Stairs(BlockState baseState, Properties properties) {
			super(baseState, properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return PLANKS_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}

	public static class Fence extends FenceBlock {
		public Fence(Properties properties) {
			super(properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return PLANKS_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}

	public static class FenceGate extends FenceGateBlock {
		public FenceGate(WoodType type, Properties properties) {
			super(type, properties);
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return PLANKS_FLAMMABILITY;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
			return SPREAD_SPEED;
		}
	}
}
