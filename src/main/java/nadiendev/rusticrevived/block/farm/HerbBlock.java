package nadiendev.rusticrevived.block.farm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Herb that grows through 4 stages and drops extra herbs when fully grown (legacy BlockHerbBase).
 * Herbs with an edible root (cloudsbluff, core root, ginseng, marsh mallow) are planted with that
 * root item, the others with their own block item.
 */
public class HerbBlock extends VegetationBlock implements BonemealableBlock {

	/** Where the herb can be planted (legacy EnumPlantType). */
	public enum PlantType implements StringRepresentable {
		/** grass, dirt, farmland, fertile soil */
		PLAINS("plains"),
		/** sand, red sand, terracotta ({@link ModTags.Blocks#DESERT_HERB_SOIL}) */
		DESERT("desert"),
		/** any block with a sturdy top face, or {@link ModTags.Blocks#CAVE_HERB_SOIL} */
		CAVE("cave"),
		/** netherrack, soul sand, soul soil, nylium ({@link ModTags.Blocks#NETHER_HERB_SOIL}) */
		NETHER("nether");

		public static final Codec<PlantType> CODEC = StringRepresentable.fromEnum(PlantType::values);
		private final String name;

		PlantType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final MapCodec<HerbBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			propertiesCodec(),
			PlantType.CODEC.fieldOf("plant_type").forGetter(HerbBlock::getPlantType)
	).apply(i, HerbBlock::new));

	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape[] SHAPES = {
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(0, 0, 0, 16, 4, 16),
			Block.box(0, 0, 0, 16, 8, 16),
			Block.box(0, 0, 0, 16, 14, 16) };

	private final PlantType plantType;

	public HerbBlock(Properties properties, PlantType plantType) {
		super(properties);
		this.plantType = plantType;
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	public PlantType getPlantType() {
		return plantType;
	}

	@Override
	protected MapCodec<? extends HerbBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	/** The herb at full growth, as generated in the world. */
	public BlockState grownState() {
		return defaultBlockState().setValue(AGE, MAX_AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(AGE)].move(state.getOffset(pos));
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return switch (plantType) {
			case PLAINS -> super.mayPlaceOn(state, level, pos);
			case DESERT -> state.is(ModTags.Blocks.DESERT_HERB_SOIL);
			case CAVE -> state.is(ModTags.Blocks.CAVE_HERB_SOIL) || state.isFaceSturdy(level, pos, Direction.UP);
			case NETHER -> state.is(ModTags.Blocks.NETHER_HERB_SOIL);
		};
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9
				&& CommonHooks.canCropGrow(level, pos, state, random.nextInt(16) == 0)) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
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

	/**
	 * Legacy flammable herbs burn like flowers; aloe vera and the mushrooms do not burn
	 * (FireBlock#setFlammable is private in 26.1).
	 */
	private boolean isFlammable() {
		return this != ModBlocks.ALOE_VERA.get() && this != ModBlocks.DEATHSTALK_MUSHROOM.get() && this != ModBlocks.MOONCAP_MUSHROOM.get();
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return isFlammable() ? 100 : 0;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return isFlammable() ? 60 : 0;
	}
}
