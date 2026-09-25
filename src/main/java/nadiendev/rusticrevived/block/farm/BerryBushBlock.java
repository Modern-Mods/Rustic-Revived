package nadiendev.rusticrevived.block.farm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Berry bush (wildberries) (legacy BlockBerryBush). Grows through 6 stages, can be walked through
 * (slowing entities down) and is harvested by right-clicking; bone meal on a ripe bush spreads it
 * to an adjacent block.
 */
public class BerryBushBlock extends VegetationBlock implements BonemealableBlock {
	public static final int MAX_AGE = 5;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	private final MapCodec<BerryBushBlock> codec;
	protected final Supplier<? extends Item> berries;

	public BerryBushBlock(Properties properties, Supplier<? extends Item> berries) {
		super(properties);
		this.berries = berries;
		this.codec = simpleCodec(p -> new BerryBushBlock(p, berries));
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected MapCodec<? extends BerryBushBlock> codec() {
		return codec;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6, 1.0, 0.6));
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9
				&& CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hitResult) {
		// bone meal on a ripe bush spreads it instead of harvesting
		return stack.is(Items.BONE_MEAL) ? InteractionResult.PASS
				: InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (state.getValue(AGE) < MAX_AGE) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_ALL);
			FarmHelper.giveOrDrop(player, level, pos.relative(player.getDirection().getOpposite()), new ItemStack(berries.get()));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		int age = state.getValue(AGE);
		if (age >= MAX_AGE) {
			growOutward(level, pos, random);
		} else {
			level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, age + Mth.nextInt(random, 2, 5))), Block.UPDATE_ALL);
		}
	}

	/** Places a young bush on a random free neighbouring block of the 3x3 ring. */
	private void growOutward(ServerLevel level, BlockPos pos, RandomSource random) {
		List<BlockPos> candidates = new ArrayList<>();
		BlockState young = defaultBlockState();
		for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
			if (!candidate.equals(pos) && level.getBlockState(candidate).isAir() && young.canSurvive(level, candidate)) {
				candidates.add(candidate.immutable());
			}
		}
		if (!candidates.isEmpty()) {
			level.setBlock(candidates.get(random.nextInt(candidates.size())), young, Block.UPDATE_ALL);
		}
	}

	/** Burns like the legacy berry bush (FireBlock#setFlammable is private in 26.1). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 80;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 40;
	}
}
