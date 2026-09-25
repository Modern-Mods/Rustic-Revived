package nadiendev.rusticrevived.block.farm;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Crop that grows up a column of crop stakes (tomatoes, chili peppers) (legacy BlockStakeCrop).
 * <p>
 * A fully grown plant climbs onto the crop stake above it until it is {@link #getMaxHeight()}
 * blocks tall. Right-clicking harvests every ripe block of the column; breaking the plant (or the
 * plant losing its soil / light) leaves the crop stake behind.
 */
public class StakeCropBlock extends Block implements BonemealableBlock {
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape CROP_SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	private final MapCodec<StakeCropBlock> codec;
	protected final Supplier<? extends Item> seed;
	protected final Supplier<? extends Item> crop;
	@Nullable
	protected final Supplier<? extends Item> rareCrop;
	protected final int rareChance;
	protected final int maxHeight;

	/**
	 * @param seed       seed item that plants this crop
	 * @param crop       harvested item
	 * @param rareCrop   optional rare harvest (ghost pepper)
	 * @param rareChance 1 in rareChance harvests yield the rare crop
	 * @param maxHeight  maximum height of the plant, in blocks
	 */
	public StakeCropBlock(Properties properties, Supplier<? extends Item> seed, Supplier<? extends Item> crop,
			@Nullable Supplier<? extends Item> rareCrop, int rareChance, int maxHeight) {
		super(properties);
		this.seed = seed;
		this.crop = crop;
		this.rareCrop = rareCrop;
		this.rareChance = rareChance;
		this.maxHeight = maxHeight;
		this.codec = simpleCodec(p -> new StakeCropBlock(p, seed, crop, rareCrop, rareChance, maxHeight));
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public Item getSeed() {
		return seed.get();
	}

	public Item getCrop() {
		return crop.get();
	}

	/** The rare harvest (ghost pepper), or null. */
	@Nullable
	public Item getRareCrop() {
		return rareCrop == null ? null : rareCrop.get();
	}

	/** 1 in {@code rareChance} harvests yield {@link #getRareCrop()}. */
	public int getRareChance() {
		return rareChance;
	}

	@Override
	protected MapCodec<? extends StakeCropBlock> codec() {
		return codec;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return CROP_SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return CropStakeBlock.STAKE_COLLISION;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(getSeed());
	}

	// ---------------------------------------------------------------- survival

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos soilPos = pos.below();
		BlockState soil = level.getBlockState(soilPos);
		return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos))
				&& (soil.is(this) || FarmHelper.isCropSoil(soil, level, soilPos, state));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			ticks.scheduleTick(pos, this, 1);
		}
		return state;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			breakToStake(state, level, pos);
		}
	}

	/** Drops the plant and leaves the bare crop stake. */
	private static void breakToStake(BlockState state, Level level, BlockPos pos) {
		dropResources(state, level, pos);
		level.setBlock(pos, ModBlocks.CROP_STAKE.get().defaultBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest,
			FluidState fluid) {
		return level.setBlock(pos, ModBlocks.CROP_STAKE.get().defaultBlockState(), level.isClientSide() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
	}

	@Override
	public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
		level.setBlock(pos, ModBlocks.CROP_STAKE.get().defaultBlockState(), Block.UPDATE_ALL);
		wasExploded(level, pos, explosion);
	}

	// ---------------------------------------------------------------- growth

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			breakToStake(state, level, pos);
			return;
		}
		if (level.getRawBrightness(pos.above(), 0) < 9) {
			return;
		}
		int age = state.getValue(AGE);
		float growthChance = getGrowthChance(level, pos);
		if (age < MAX_AGE) {
			if (CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (50.0F / growthChance) + 1) == 0)) {
				level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
				CommonHooks.fireCropGrowPost(level, pos, state);
			}
		} else if (canClimb(level, pos)
				&& CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (30.0F / growthChance) + 1) == 0)) {
			level.setBlock(pos.above(), defaultBlockState(), Block.UPDATE_ALL);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	/** Whether the plant can grow onto the crop stake above it without exceeding its maximum height. */
	private boolean canClimb(LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.above()).is(ModBlocks.CROP_STAKE.get()) && !level.getBlockState(pos.below(maxHeight - 1)).is(this);
	}

	private float getGrowthChance(Level level, BlockPos pos) {
		float growth = 0.125F * (level.getMaxLocalRawBrightness(pos) - 11);
		BlockPos soilPos = pos.below();
		BlockState soil = level.getBlockState(soilPos);
		if (soil.is(this) || soil.isFertile(level, soilPos)) {
			growth *= 1.5F;
		}
		return Math.abs(1.5F + growth);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
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

	// ---------------------------------------------------------------- harvest

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		boolean harvested = false;
		BlockPos.MutableBlockPos column = pos.mutable();
		while (level.getBlockState(column).is(this)) {
			harvested |= tryHarvest(level, column, player, hitResult.getDirection());
			column.move(Direction.UP);
		}
		column.set(pos).move(Direction.DOWN);
		while (level.getBlockState(column).is(this)) {
			harvested |= tryHarvest(level, column, player, hitResult.getDirection());
			column.move(Direction.DOWN);
		}
		return harvested ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	private boolean tryHarvest(Level level, BlockPos pos, Player player, Direction side) {
		BlockState state = level.getBlockState(pos);
		if (state.getValue(AGE) < MAX_AGE) {
			return false;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(AGE, MAX_AGE - 1), Block.UPDATE_CLIENTS);
			FarmHelper.giveOrDrop(player, level, pos.relative(side), new ItemStack(rollHarvest(level.getRandom())));
		}
		return true;
	}

	private Item rollHarvest(RandomSource random) {
		Item rare = getRareCrop();
		return rare != null && random.nextInt(rareChance) == 0 ? rare : getCrop();
	}

	/** Burns like the stake and its plant (FireBlock#setFlammable is private in 26.1). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 100;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 30;
	}
}
