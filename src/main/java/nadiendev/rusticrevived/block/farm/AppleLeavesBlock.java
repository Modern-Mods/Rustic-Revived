package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Apple leaves that slowly grow apples when exposed to air below or on a side; ripe apples are
 * harvested by right-clicking (legacy BlockLeavesApple).
 */
public class AppleLeavesBlock extends LeavesBlock implements BonemealableBlock {
	public static final MapCodec<AppleLeavesBlock> CODEC = simpleCodec(AppleLeavesBlock::new);
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = IntegerProperty.create("apple_age", 0, MAX_AGE);

	public AppleLeavesBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(AGE, 0));
	}

	@Override
	public MapCodec<? extends AppleLeavesBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return super.isRandomlyTicking(state) || state.getValue(AGE) < MAX_AGE;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.randomTick(state, level, pos, random);
		if (decaying(state)) {
			return;
		}
		int age = state.getValue(AGE);
		if (age < MAX_AGE && isAirAdjacent(level, pos) && CommonHooks.canCropGrow(level, pos, state, random.nextInt(51) == 0)) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	/** Apples only grow when the leaves are open to the air below or on a horizontal side. */
	private static boolean isAirAdjacent(BlockGetter level, BlockPos pos) {
		if (level.getBlockState(pos.below()).isAir()) {
			return true;
		}
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(pos.relative(direction)).isAir()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (state.getValue(AGE) < MAX_AGE) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_ALL);
			FarmHelper.giveOrDrop(player, level, pos.relative(hitResult.getDirection()), new ItemStack(Items.APPLE));
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return state.getValue(AGE) < MAX_AGE && isAirAdjacent(level, pos);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		int age = Math.min(MAX_AGE, state.getValue(AGE) + Mth.nextInt(random, 2, 5));
		level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
	}
}
