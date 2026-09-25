package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.farm.AppleLeavesBlock;
import nadiendev.rusticrevived.block.farm.AppleSeedsBlock;
import nadiendev.rusticrevived.block.farm.BerryBushBlock;
import nadiendev.rusticrevived.block.farm.GrapeLeavesBlock;
import nadiendev.rusticrevived.block.farm.GrapeStemBlock;
import nadiendev.rusticrevived.block.farm.HerbBlock;
import nadiendev.rusticrevived.block.farm.RusticLeavesBlock;
import nadiendev.rusticrevived.block.farm.RusticSaplingBlock;
import nadiendev.rusticrevived.block.farm.StakeCropBlock;
import nadiendev.rusticrevived.block.farm.StakeTiedBlock;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticLootModifierProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import nadiendev.rusticrevived.registry.ModWorldGen;
import nadiendev.rusticrevived.world.ModTreeGrowers;
import nadiendev.rusticrevived.world.loot.GrapeSeedsModifier;
import nadiendev.rusticrevived.world.loot.GrassSeedsModifier;
import nadiendev.rusticrevived.world.placement.ConfigChancePlacement;
import nadiendev.rusticrevived.world.placement.ConfigCountPlacement;
import nadiendev.rusticrevived.world.placement.ConfigFilterPlacement;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data generation for the agriculture and trees (olive / ironwood / apple trees, crops, herbs, world generation) subsystem.
 */
public final class FarmData {
	private FarmData() {
	}

	/** One of the two Rustic woods and everything made of it. */
	private record Wood(String name, DeferredBlock<Block> planks, DeferredBlock<RotatedPillarBlock> log, DeferredBlock<RusticLeavesBlock> leaves,
			DeferredBlock<RusticSaplingBlock> sapling, DeferredBlock<FenceBlock> fence, DeferredBlock<FenceGateBlock> fenceGate,
			DeferredBlock<SlabBlock> slab, DeferredBlock<StairBlock> stairs, DeferredBlock<DoorBlock> door, DeferredBlock<Block> carved,
			String carvedTexture, TagKey<Block> logBlocks, TagKey<Item> logItems, ItemLike fruit, float[] fruitChances) {
	}

	/**
	 * Legacy BlockLeavesRustic: 1 in (6 - fortune) olives (minimum 1 in 2) and 1 in (16 - fortune)
	 * ironberries.
	 */
	private static final List<Wood> WOODS = List.of(
			new Wood("olive", ModBlocks.OLIVE_PLANKS, ModBlocks.OLIVE_LOG, ModBlocks.OLIVE_LEAVES, ModBlocks.OLIVE_SAPLING, ModBlocks.OLIVE_FENCE,
					ModBlocks.OLIVE_FENCE_GATE, ModBlocks.OLIVE_SLAB, ModBlocks.OLIVE_STAIRS, ModBlocks.OLIVE_DOOR, ModBlocks.CARVED_OLIVE_WOOD,
					"carved_olive_wood", ModTags.Blocks.OLIVE_LOGS, ModTags.Items.OLIVE_LOGS, ModItems.OLIVES,
					new float[] { 1.0F / 6, 1.0F / 5, 1.0F / 4, 1.0F / 3 }),
			new Wood("ironwood", ModBlocks.IRONWOOD_PLANKS, ModBlocks.IRONWOOD_LOG, ModBlocks.IRONWOOD_LEAVES, ModBlocks.IRONWOOD_SAPLING,
					ModBlocks.IRONWOOD_FENCE, ModBlocks.IRONWOOD_FENCE_GATE, ModBlocks.IRONWOOD_SLAB, ModBlocks.IRONWOOD_STAIRS, ModBlocks.IRONWOOD_DOOR,
					ModBlocks.CARVED_IRONWOOD, "carved_ironwood", ModTags.Blocks.IRONWOOD_LOGS, ModTags.Items.IRONWOOD_LOGS, ModItems.IRONBERRIES,
					new float[] { 1.0F / 16, 1.0F / 15, 1.0F / 14, 1.0F / 13 }));

	/** Legacy leaves: 1 sapling in (20 - (2 << fortune)), at least 1 in 10. */
	private static final float[] SAPLING_CHANCES = { 1.0F / 20, 1.0F / 16, 1.0F / 12, 1.0F / 10 };

	/** Inventory colour of foliage tinted items (vanilla default foliage colour). */
	private static final ItemTintSource FOLIAGE_TINT = ItemModelUtils.constantTint(-12012264);

	/**
	 * A herb, the texture of its young stages ({@code block/herbs/<seed>}) and the texture of its
	 * item (the block item, or the edible root item of the same name).
	 */
	private record Herb(DeferredBlock<HerbBlock> block, String seedTexture, String itemTexture) {
		String name() {
			return block.getId().getPath();
		}
	}

	private static final List<Herb> HERBS = List.of(
			herb(ModBlocks.ALOE_VERA), herb(ModBlocks.BLOOD_ORCHID), herb(ModBlocks.CHAMOMILE), herb(ModBlocks.CLOUDSBLUFF), herb(ModBlocks.COHOSH),
			herb(ModBlocks.CORE_ROOT), mushroom(ModBlocks.DEATHSTALK_MUSHROOM), herb(ModBlocks.GINSENG), herb(ModBlocks.HORSETAIL),
			herb(ModBlocks.MARSH_MALLOW), mushroom(ModBlocks.MOONCAP_MUSHROOM), herb(ModBlocks.WIND_THISTLE), herb(ModBlocks.VANTA_LILY));

	private static Herb herb(DeferredBlock<HerbBlock> block) {
		String name = block.getId().getPath();
		return new Herb(block, name + "_seed", "item/herbs/" + name);
	}

	private static Herb mushroom(DeferredBlock<HerbBlock> block) {
		String name = block.getId().getPath();
		return new Herb(block, name + "_spore", "block/herbs/" + name);
	}

	private static Material tex(String path) {
		return new Material(RusticModelProvider.blockTex(path));
	}

	private static Material mcTex(String path) {
		return new Material(Identifier.withDefaultNamespace("block/" + path));
	}

	// ================================================================ block states, models & client items

	/** Block states, block models, item models and client item definitions. */
	public static void models(RusticModelProvider p, BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		for (Wood wood : WOODS) {
			woodModels(blockModels, wood);
		}
		appleModels(blockModels);
		cropModels(blockModels);
		for (Herb herb : HERBS) {
			HerbBlock block = herb.block().get();
			MultiVariant seed = BlockModelGenerators.plainVariant(ModelTemplates.CROSS.create(RusticRevived.id("block/" + herb.seedTexture()),
					TextureMapping.cross(tex("herbs/" + herb.seedTexture())), blockModels.modelOutput));
			MultiVariant grown = BlockModelGenerators.plainVariant(ModelTemplates.CROSS.create(block,
					TextureMapping.cross(tex("herbs/" + herb.name())), blockModels.modelOutput));
			blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
					.with(PropertyDispatch.initial(HerbBlock.AGE).generate(age -> age == HerbBlock.MAX_AGE ? grown : seed)));
			flatItem(blockModels, block.asItem(), new Material(RusticRevived.id(herb.itemTexture())));
		}
		for (ItemLike food : List.of(ModItems.OLIVES, ModItems.IRONBERRIES, ModItems.TOMATO, ModItems.CHILI_PEPPER, ModItems.GHOST_PEPPER,
				ModItems.WILDBERRIES, ModItems.GRAPES, ModItems.TOMATO_SEEDS, ModItems.CHILI_PEPPER_SEEDS)) {
			itemModels.generateFlatItem(food.asItem(), ModelTemplates.FLAT_ITEM);
		}
	}

	/** A {@code item/generated} model named after the item, with the given texture. */
	private static void flatItem(BlockModelGenerators blockModels, Item item, Material texture) {
		blockModels.registerSimpleItemModel(item,
				ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(texture), blockModels.modelOutput));
	}

	private static void woodModels(BlockModelGenerators blockModels, Wood wood) {
		// planks, slab, stairs, fence and fence gate share the planks texture (like a vanilla block family)
		blockModels.new BlockFamilyProvider(TextureMapping.cube(tex("planks_" + wood.name())))
				.fullBlock(wood.planks().get(), ModelTemplates.CUBE_ALL)
				.slab(wood.slab().get())
				.stairs(wood.stairs().get())
				.fence(wood.fence().get())
				.fenceGate(wood.fenceGate().get());
		blockModels.new WoodProvider(TextureMapping.column(tex("log_" + wood.name()), tex("log_" + wood.name() + "_top")))
				.logWithHorizontal(wood.log().get());

		Block leaves = wood.leaves().get();
		Identifier leavesModel = ModelTemplates.LEAVES.create(leaves, TextureMapping.cube(tex("leaves_" + wood.name())), blockModels.modelOutput);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(leaves, BlockModelGenerators.plainVariant(leavesModel)));
		blockModels.registerSimpleTintedItemModel(leaves, leavesModel, FOLIAGE_TINT);

		Block sapling = wood.sapling().get();
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(sapling, BlockModelGenerators.plainVariant(
				ModelTemplates.CROSS.create(sapling, TextureMapping.cross(tex("sapling_" + wood.name())), blockModels.modelOutput))));
		flatItem(blockModels, sapling.asItem(), tex("sapling_" + wood.name()));

		door(blockModels, wood.door().get(), tex("door_" + wood.name() + "_upper"), tex("door_" + wood.name() + "_lower"),
				new Material(RusticModelProvider.itemTex("door_" + wood.name())));

		Block carved = wood.carved().get();
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(carved, BlockModelGenerators.plainVariant(
				ModelTemplates.CUBE_ALL.create(carved, TextureMapping.cube(tex(wood.carvedTexture())), blockModels.modelOutput))));
	}

	/** Vanilla door models with custom upper / lower textures and item texture. */
	private static void door(BlockModelGenerators blockModels, Block door, Material top, Material bottom, Material itemTexture) {
		TextureMapping mapping = TextureMapping.door(top, bottom);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createDoor(door,
				doorModel(blockModels, door, ModelTemplates.DOOR_BOTTOM_LEFT, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_BOTTOM_LEFT_OPEN, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_BOTTOM_RIGHT, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_TOP_LEFT, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_TOP_LEFT_OPEN, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_TOP_RIGHT, mapping),
				doorModel(blockModels, door, ModelTemplates.DOOR_TOP_RIGHT_OPEN, mapping)));
		flatItem(blockModels, door.asItem(), itemTexture);
	}

	private static MultiVariant doorModel(BlockModelGenerators blockModels, Block door,
			ModelTemplate template, TextureMapping mapping) {
		return BlockModelGenerators.plainVariant(template.create(door, mapping, blockModels.modelOutput));
	}

	private static void appleModels(BlockModelGenerators blockModels) {
		AppleSeedsBlock seeds = ModBlocks.APPLE_SEEDS.get();
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(seeds).with(PropertyDispatch.initial(AppleSeedsBlock.AGE)
				.generate(age -> BlockModelGenerators.plainVariant(ModelTemplates.CROSS.createWithSuffix(seeds, "_" + age,
						TextureMapping.cross(tex("apple_seeds_" + age)), blockModels.modelOutput)))));
		blockModels.registerSimpleFlatItemModel(seeds.asItem());

		RusticSaplingBlock sapling = ModBlocks.APPLE_SAPLING.get();
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(sapling, BlockModelGenerators.plainVariant(
				ModelTemplates.CROSS.create(sapling, TextureMapping.cross(mcTex("oak_sapling")), blockModels.modelOutput))));
		flatItem(blockModels, sapling.asItem(), mcTex("oak_sapling"));

		// legacy leaves_apple: oak leaves with apples of three sizes
		AppleLeavesBlock leaves = ModBlocks.APPLE_LEAVES.get();
		Identifier leavesModel = ModelTemplates.LEAVES.create(leaves, TextureMapping.cube(mcTex("oak_leaves")), blockModels.modelOutput);
		MultiPartGenerator apples = MultiPartGenerator.multiPart(leaves).with(BlockModelGenerators.plainVariant(leavesModel));
		for (int age = 1; age <= AppleLeavesBlock.MAX_AGE; age++) {
			addRotated(apples, "apple_" + (age - 1), AppleLeavesBlock.AGE, age);
			addRotated(apples, "apple_" + (age - 1) + "_1", AppleLeavesBlock.AGE, age);
		}
		blockModels.blockStateOutput.accept(apples);
		blockModels.registerSimpleTintedItemModel(leaves, leavesModel, FOLIAGE_TINT);
	}

	private static void cropModels(BlockModelGenerators blockModels) {
		Block soil = ModBlocks.FERTILE_SOIL.get();
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(soil, BlockModelGenerators.plainVariant(
				ModelTemplates.CUBE_BOTTOM_TOP.create(soil, new TextureMapping().put(TextureSlot.TOP, mcTex("farmland_moist"))
						.put(TextureSlot.SIDE, tex("fertile_soil")).put(TextureSlot.BOTTOM, tex("fertile_soil")), blockModels.modelOutput))));

		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.CROP_STAKE.get(), existing("crop_stake")));

		MultiVariant knot = existing("rope_attachment_n");
		blockModels.blockStateOutput.accept(MultiPartGenerator.multiPart(ModBlocks.STAKE_TIED.get())
				.with(existing("stake_tied"))
				.with(BlockModelGenerators.condition().term(StakeTiedBlock.NORTH, true), knot)
				.with(BlockModelGenerators.condition().term(StakeTiedBlock.EAST, true), knot.with(BlockModelGenerators.Y_ROT_90))
				.with(BlockModelGenerators.condition().term(StakeTiedBlock.SOUTH, true), knot.with(BlockModelGenerators.Y_ROT_180))
				.with(BlockModelGenerators.condition().term(StakeTiedBlock.WEST, true), knot.with(BlockModelGenerators.Y_ROT_270)));

		for (DeferredBlock<StakeCropBlock> crop : List.of(ModBlocks.TOMATO_CROP, ModBlocks.CHILI_CROP)) {
			String name = crop.getId().getPath();
			blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(crop.get())
					.with(PropertyDispatch.initial(StakeCropBlock.AGE).generate(age -> existing(name + "_" + age))));
		}

		BerryBushBlock bush = ModBlocks.WILDBERRY_BUSH.get();
		MultiPartGenerator bushParts = MultiPartGenerator.multiPart(bush).with(existing("wildberry_bush"));
		addRotated(bushParts, "wildberries_0", BerryBushBlock.AGE, BerryBushBlock.MAX_AGE);
		addRotated(bushParts, "wildberries_1", BerryBushBlock.AGE, BerryBushBlock.MAX_AGE);
		blockModels.blockStateOutput.accept(bushParts);
		blockModels.registerSimpleTintedItemModel(bush, RusticModelProvider.existingBlockModel("wildberry_bush"), FOLIAGE_TINT);

		GrapeStemBlock stem = ModBlocks.GRAPE_STEM.get();
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(stem).with(PropertyDispatch.initial(GrapeStemBlock.AGE)
				.generate(age -> age == GrapeStemBlock.MAX_AGE ? existing("grape_stem_" + age)
						: BlockModelGenerators.plainVariant(ModelTemplates.CROSS.createWithSuffix(stem, "_" + age,
								TextureMapping.cross(tex("grape_stem_" + age)), blockModels.modelOutput)))));
		flatItem(blockModels, stem.asItem(), new Material(RusticModelProvider.itemTex("grape_seeds")));

		// legacy grape_leaves multipart: vine + leaves (+ stem top above the stem) + grapes + the rope it grows on
		MultiPartGenerator vines = MultiPartGenerator.multiPart(ModBlocks.GRAPE_LEAVES.get())
				.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.DISTANCE, 0), existing("grape_leaves_0"))
				.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.DISTANCE, 0), existing("grape_stem_top"))
				.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.GRAPES, true), existing("grapes"));
		for (Direction.Axis axis : List.of(Direction.Axis.X, Direction.Axis.Z)) {
			boolean rotated = axis == Direction.Axis.X;
			vines.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.DISTANCE, 1).term(GrapeLeavesBlock.AXIS, axis),
					rotateY90(existing("grape_leaves_1"), rotated));
			vines.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.AXIS, axis), rotateY90(existing("grape_vine"), rotated));
			vines.with(BlockModelGenerators.condition().term(GrapeLeavesBlock.AXIS, axis), rotateY90(existing("rope_horizontal"), rotated));
		}
		blockModels.blockStateOutput.accept(vines);
	}

	private static MultiVariant rotateY90(MultiVariant model, boolean rotated) {
		return rotated ? model.with(BlockModelGenerators.Y_ROT_90) : model;
	}

	/** A hand-made model of {@code models/block}. */
	private static MultiVariant existing(String name) {
		return BlockModelGenerators.plainVariant(RusticModelProvider.existingBlockModel(name));
	}

	/** Adds the hand-made {@code model} four times (every horizontal rotation) when {@code property} is {@code value}. */
	private static void addRotated(MultiPartGenerator generator, String model, IntegerProperty property, int value) {
		MultiVariant variant = existing(model);
		generator.with(BlockModelGenerators.condition().term(property, value), variant)
				.with(BlockModelGenerators.condition().term(property, value), variant.with(BlockModelGenerators.Y_ROT_90))
				.with(BlockModelGenerators.condition().term(property, value), variant.with(BlockModelGenerators.Y_ROT_180))
				.with(BlockModelGenerators.condition().term(property, value), variant.with(BlockModelGenerators.Y_ROT_270));
	}

	// ================================================================ recipes

	public static void recipes(RusticRecipeProvider p) {
		RecipeOutput out = p.output();
		for (Wood wood : WOODS) {
			Block planks = wood.planks().get();
			String has = RusticRecipeProvider.hasName(planks);
			p.shapelessRecipe(RecipeCategory.BUILDING_BLOCKS, planks, 4).requires(wood.logItems()).group("planks")
					.unlockedBy("has_logs", p.hasTag(wood.logItems())).save(out, RusticRecipeProvider.key(wood.name() + "_planks"));
			p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, wood.slab().get(), 6).pattern("PPP").define('P', planks)
					.group("wooden_slab").unlockedBy(has, p.hasItem(planks)).save(out, RusticRecipeProvider.key(wood.name() + "_slab"));
			p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, wood.stairs().get(), 4).pattern("P  ").pattern("PP ").pattern("PPP")
					.define('P', planks).group("wooden_stairs").unlockedBy(has, p.hasItem(planks))
					.save(out, RusticRecipeProvider.key(wood.name() + "_stairs"));
			p.shapedRecipe(RecipeCategory.DECORATIONS, wood.fence().get(), 3).pattern("PSP").pattern("PSP").define('P', planks)
					.define('S', Tags.Items.RODS_WOODEN).group("wooden_fence").unlockedBy(has, p.hasItem(planks))
					.save(out, RusticRecipeProvider.key(wood.name() + "_fence"));
			p.shapedRecipe(RecipeCategory.REDSTONE, wood.fenceGate().get(), 1).pattern("SPS").pattern("SPS").define('P', planks)
					.define('S', Tags.Items.RODS_WOODEN).group("wooden_fence_gate").unlockedBy(has, p.hasItem(planks))
					.save(out, RusticRecipeProvider.key(wood.name() + "_fence_gate"));
			p.shapedRecipe(RecipeCategory.REDSTONE, wood.door().get(), 3).pattern("PP").pattern("PP").pattern("PP").define('P', planks)
					.group("wooden_door").unlockedBy(has, p.hasItem(planks)).save(out, RusticRecipeProvider.key(wood.name() + "_door"));
			// PR #252: Quark style carved wood, two slabs on top of each other
			p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, wood.carved().get(), 1).pattern("S").pattern("S").define('S', wood.slab().get())
					.unlockedBy(RusticRecipeProvider.hasName(wood.slab().get()), p.hasItem(wood.slab().get()))
					.save(out, RusticRecipeProvider.key(wood.carvedTexture()));
		}
		p.shapedRecipe(RecipeCategory.MISC, ModBlocks.CROP_STAKE.get(), 3).pattern("P").pattern("P").pattern("P")
				.define('P', ItemTags.PLANKS).unlockedBy("has_planks", p.hasTag(ItemTags.PLANKS))
				.save(out, RusticRecipeProvider.key("crop_stake"));
		p.shapelessRecipe(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FERTILE_SOIL.get(), 1).requires(Items.BONE_MEAL).requires(Items.DIRT)
				.unlockedBy(RusticRecipeProvider.hasName(Items.BONE_MEAL), p.hasItem(Items.BONE_MEAL))
				.save(out, RusticRecipeProvider.key("fertile_soil"));
		p.shapelessRecipe(RecipeCategory.MISC, ModBlocks.GRAPE_STEM.get(), 1).requires(ModItems.GRAPES.get())
				.unlockedBy(RusticRecipeProvider.hasName(ModItems.GRAPES.get()), p.hasItem(ModItems.GRAPES.get()))
				.save(out, RusticRecipeProvider.key("grape_seeds"));
	}

	// ================================================================ loot tables

	public static void blockLoot(RusticBlockLoot l) {
		Holder<Enchantment> fortune = l.lookup().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
		for (Wood wood : WOODS) {
			for (DeferredBlock<? extends Block> block : List.of(wood.planks(), wood.log(), wood.sapling(), wood.fence(), wood.fenceGate(),
					wood.stairs(), wood.carved())) {
				l.selfDrop(block.get());
			}
			l.table(wood.slab().get(), l.slab(wood.slab().get()));
			l.table(wood.door().get(), l.door(wood.door().get()));
			l.table(wood.leaves().get(), leaves(l, fortune, wood.leaves().get(), wood.sapling().get(), wood.fruit(), wood.fruitChances(), null));
		}

		l.selfDrop(ModBlocks.APPLE_SEEDS.get());
		l.selfDrop(ModBlocks.APPLE_SAPLING.get());
		// legacy BlockLeavesApple: shears give plain oak leaves, ripe apples always drop
		l.table(ModBlocks.APPLE_LEAVES.get(), leaves(l, fortune, Blocks.OAK_LEAVES, ModBlocks.APPLE_SAPLING.get(), Items.APPLE, new float[] { 1.0F },
				ageIs(ModBlocks.APPLE_LEAVES.get(), AppleLeavesBlock.AGE, AppleLeavesBlock.MAX_AGE)));

		l.selfDrop(ModBlocks.FERTILE_SOIL.get());
		l.selfDrop(ModBlocks.CROP_STAKE.get());
		l.otherDrop(ModBlocks.STAKE_TIED.get(), ModBlocks.ROPE.get());
		l.selfDrop(ModBlocks.GRAPE_STEM.get());
		for (DeferredBlock<StakeCropBlock> crop : List.of(ModBlocks.TOMATO_CROP, ModBlocks.CHILI_CROP)) {
			l.table(crop.get(), stakeCrop(crop.get()));
		}

		BerryBushBlock bush = ModBlocks.WILDBERRY_BUSH.get();
		l.table(bush, LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(bush)).when(ExplosionCondition.survivesExplosion()))
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(ageIs(bush, BerryBushBlock.AGE, BerryBushBlock.MAX_AGE))
						.add(LootItem.lootTableItem(ModItems.WILDBERRIES.get()).apply(ApplyExplosionDecay.explosionDecay()))));

		GrapeLeavesBlock vines = ModBlocks.GRAPE_LEAVES.get();
		l.table(vines, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(vines)
						.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GrapeLeavesBlock.GRAPES, true)))
				.add(LootItem.lootTableItem(ModItems.GRAPES.get()).apply(ApplyExplosionDecay.explosionDecay()))));

		// legacy BlockHerbBase: one herb, plus (2 + fortune) rolls of 4 in 6 when fully grown
		for (Herb herb : HERBS) {
			HerbBlock block = herb.block().get();
			l.table(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(block.asItem())
							.apply(ApplyBonusCount.addBonusBinomialDistributionCount(fortune, 4.0F / 6.0F, 2)
									.when(ageIs(block, HerbBlock.AGE, HerbBlock.MAX_AGE)))
							.apply(ApplyExplosionDecay.explosionDecay()))));
		}
	}

	private static LootItemCondition.Builder ageIs(Block block, IntegerProperty age, int value) {
		return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
				.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(age, value));
	}

	/**
	 * Legacy leaves: themselves with shears or silk touch, otherwise a sapling (no sticks) and a
	 * fruit when {@code fruitCondition} holds.
	 */
	private static LootTable.Builder leaves(RusticBlockLoot l, Holder<Enchantment> fortune, ItemLike shearedDrop, Block sapling, ItemLike fruit,
			float[] fruitChances, @Nullable LootItemCondition.Builder fruitCondition) {
		LootItemCondition.Builder shearsOrSilk = l.shears().or(l.silkTouch());
		LootPoolSingletonContainer.Builder<?> fruitEntry = LootItem.lootTableItem(fruit).when(ExplosionCondition.survivesExplosion())
				.when(BonusLevelTableCondition.bonusLevelFlatChance(fortune, fruitChances));
		if (fruitCondition != null) {
			fruitEntry.when(fruitCondition);
		}
		return LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(shearedDrop).when(shearsOrSilk)
						.otherwise(LootItem.lootTableItem(sapling).when(ExplosionCondition.survivesExplosion())
								.when(BonusLevelTableCondition.bonusLevelFlatChance(fortune, SAPLING_CHANCES)))))
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(shearsOrSilk.invert()).add(fruitEntry));
	}

	/** Legacy BlockStakeCrop: 1-2 seeds, plus the crop (or the rare crop) when fully grown. */
	private static LootTable.Builder stakeCrop(StakeCropBlock crop) {
		LootPoolSingletonContainer.Builder<?> harvest = LootItem.lootTableItem(crop.getCrop());
		Item rare = crop.getRareCrop();
		LootPool.Builder grown = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(ageIs(crop, StakeCropBlock.AGE, StakeCropBlock.MAX_AGE))
				.add(rare == null ? harvest : LootItem.lootTableItem(rare)
						.when(LootItemRandomChanceCondition.randomChance(1.0F / crop.getRareChance())).otherwise(harvest));
		return LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(crop.getSeed())
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))).apply(ApplyExplosionDecay.explosionDecay())))
				.withPool(grown);
	}

	// ================================================================ tags

	public static void blockTags(RusticTagProviders.Blocks t) {
		for (Wood wood : WOODS) {
			t.tagOf(wood.logBlocks()).add(wood.log().get());
			t.tagOf(BlockTags.LOGS_THAT_BURN).addTag(wood.logBlocks());
			t.tagOf(BlockTags.PLANKS).add(wood.planks().get());
			t.tagOf(BlockTags.LEAVES).add(wood.leaves().get());
			t.tagOf(BlockTags.SAPLINGS).add(wood.sapling().get());
			t.tagOf(BlockTags.WOODEN_FENCES).add(wood.fence().get());
			t.tagOf(Tags.Blocks.FENCES_WOODEN).add(wood.fence().get());
			t.tagOf(BlockTags.FENCE_GATES).add(wood.fenceGate().get());
			t.tagOf(Tags.Blocks.FENCE_GATES_WOODEN).add(wood.fenceGate().get());
			t.tagOf(BlockTags.WOODEN_SLABS).add(wood.slab().get());
			t.tagOf(BlockTags.WOODEN_STAIRS).add(wood.stairs().get());
			t.tagOf(BlockTags.WOODEN_DOORS).add(wood.door().get());
			t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(wood.planks().get(), wood.log().get(), wood.fence().get(), wood.fenceGate().get(),
					wood.slab().get(), wood.stairs().get(), wood.door().get(), wood.carved().get());
			t.tagOf(BlockTags.MINEABLE_WITH_HOE).add(wood.leaves().get());
		}
		t.tagOf(BlockTags.LEAVES).add(ModBlocks.APPLE_LEAVES.get());
		t.tagOf(BlockTags.SAPLINGS).add(ModBlocks.APPLE_SAPLING.get());
		t.tagOf(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.APPLE_LEAVES.get(), ModBlocks.GRAPE_LEAVES.get());
		t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.CROP_STAKE.get(), ModBlocks.STAKE_TIED.get(), ModBlocks.GRAPE_STEM.get());
		t.tagOf(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.FERTILE_SOIL.get());
		t.tagOf(BlockTags.MAINTAINS_FARMLAND).add(ModBlocks.TOMATO_CROP.get(), ModBlocks.CHILI_CROP.get(), ModBlocks.GRAPE_STEM.get(),
				ModBlocks.CROP_STAKE.get(), ModBlocks.STAKE_TIED.get(), ModBlocks.APPLE_SEEDS.get()).addTag(ModTags.Blocks.HERBS);

		var herbs = t.tagOf(ModTags.Blocks.HERBS);
		for (Herb herb : HERBS) {
			herbs.add(herb.block().get());
		}
		t.tagOf(ModTags.Blocks.BEE_GROWABLES).add(ModBlocks.TOMATO_CROP.get(), ModBlocks.CHILI_CROP.get(), ModBlocks.GRAPE_STEM.get(),
				ModBlocks.GRAPE_LEAVES.get(), ModBlocks.WILDBERRY_BUSH.get(), ModBlocks.APPLE_LEAVES.get(), ModBlocks.APPLE_SEEDS.get())
				.addTag(ModTags.Blocks.HERBS);
		t.tagOf(ModTags.Blocks.DESERT_HERB_SOIL).addTag(BlockTags.SAND).addTag(BlockTags.TERRACOTTA);
		t.tagOf(ModTags.Blocks.CAVE_HERB_SOIL).addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.DIRT).add(Blocks.GRAVEL);
		t.tagOf(ModTags.Blocks.NETHER_HERB_SOIL).add(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL).addTag(BlockTags.NYLIUM);
	}

	public static void itemTags(RusticTagProviders.Items t) {
		for (Wood wood : WOODS) {
			t.copyTag(wood.logBlocks(), wood.logItems());
		}
		t.copyTag(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
		t.copyTag(BlockTags.PLANKS, ItemTags.PLANKS);
		t.copyTag(BlockTags.LEAVES, ItemTags.LEAVES);
		t.copyTag(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
		t.copyTag(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		t.copyTag(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
		t.copyTag(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
		t.copyTag(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
		t.copyTag(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		t.copyTag(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		t.copyTag(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);

		var herbs = t.tagOf(ModTags.Items.HERBS);
		for (Herb herb : HERBS) {
			herbs.add(herb.block().get().asItem());
		}

		t.tagOf(ModTags.Items.CROPS_OLIVE).add(ModItems.OLIVES.get());
		t.tagOf(ModTags.Items.CROPS_IRONBERRY).add(ModItems.IRONBERRIES.get());
		t.tagOf(ModTags.Items.CROPS_WILDBERRY).add(ModItems.WILDBERRIES.get());
		t.tagOf(ModTags.Items.CROPS_GRAPE).add(ModItems.GRAPES.get());
		t.tagOf(ModTags.Items.CROPS_TOMATO).add(ModItems.TOMATO.get());
		t.tagOf(ModTags.Items.CROPS_CHILI_PEPPER).add(ModItems.CHILI_PEPPER.get());
		t.tagOf(Tags.Items.CROPS).addTags(ModTags.Items.CROPS_OLIVE, ModTags.Items.CROPS_IRONBERRY, ModTags.Items.CROPS_WILDBERRY,
				ModTags.Items.CROPS_GRAPE, ModTags.Items.CROPS_TOMATO, ModTags.Items.CROPS_CHILI_PEPPER);
		t.tagOf(ModTags.Items.FOODS_FRUIT).add(ModItems.OLIVES.get(), ModItems.IRONBERRIES.get(), ModItems.GRAPES.get(), ModItems.WILDBERRIES.get());
		t.tagOf(ModTags.Items.FOODS_BERRY).add(ModItems.IRONBERRIES.get(), ModItems.GRAPES.get(), ModItems.WILDBERRIES.get());
		t.tagOf(ModTags.Items.FOODS_VEGETABLE).add(ModItems.TOMATO.get());

		t.tagOf(ModTags.Items.SEEDS_TOMATO).add(ModItems.TOMATO_SEEDS.get());
		t.tagOf(ModTags.Items.SEEDS_CHILI_PEPPER).add(ModItems.CHILI_PEPPER_SEEDS.get());
		t.tagOf(ModTags.Items.SEEDS_GRAPE).add(ModBlocks.GRAPE_STEM.get().asItem());
		t.tagOf(ModTags.Items.SEEDS_APPLE).add(ModBlocks.APPLE_SEEDS.get().asItem());
		t.tagOf(Tags.Items.SEEDS).addTags(ModTags.Items.SEEDS_TOMATO, ModTags.Items.SEEDS_CHILI_PEPPER, ModTags.Items.SEEDS_GRAPE,
				ModTags.Items.SEEDS_APPLE);

		// legacy EntityChicken.TEMPTATION_ITEMS additions: vanilla tempts and breeds chickens with this tag
		t.tagOf(ModTags.Items.CHICKEN_FOOD).addTags(ModTags.Items.SEEDS_TOMATO, ModTags.Items.SEEDS_CHILI_PEPPER, ModTags.Items.SEEDS_GRAPE,
				ModTags.Items.SEEDS_APPLE);
		t.tagOf(ItemTags.CHICKEN_FOOD).addTag(ModTags.Items.CHICKEN_FOOD);

		// legacy ore dictionary dyes
		t.tagOf(Tags.Items.DYES_RED).add(ModItems.WILDBERRIES.get());
		t.tagOf(Tags.Items.DYES_PURPLE).add(ModItems.GRAPES.get());
		t.tagOf(Tags.Items.DYES_LIGHT_GRAY).add(ModItems.IRONBERRIES.get());
	}

	/**
	 * Biome tags mirroring the legacy BiomeDictionary rules. Explicit vanilla biomes keep the
	 * legacy exclusions (no olives where it snows, no ironwood in dry biomes...); the common
	 * {@code c:} tags bring in modded biomes.
	 */
	public static void biomeTags(RusticTagProviders.Biomes t) {
		// forest / plains / mountain, never snowy
		t.tagOf(ModTags.Biomes.HAS_OLIVE_TREES).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.FOREST, Biomes.FLOWER_FOREST,
				Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA,
				Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_GRAVELLY_HILLS,
				Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.CHERRY_GROVE)
				.addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_FOREST);
		// forest / plains / mountain / swamp / jungle, never dry
		t.tagOf(ModTags.Biomes.HAS_IRONWOOD_TREES).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.FOREST, Biomes.FLOWER_FOREST,
				Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.SNOWY_TAIGA,
				Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST,
				Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.GROVE, Biomes.CHERRY_GROVE, Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.JUNGLE,
				Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA)
				.addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_SWAMP)
				.addOptionalTag(Tags.Biomes.IS_JUNGLE);
		// everywhere but cold, snowy, sandy, savanna, badlands, mushroom, dead and wasteland biomes
		t.tagOf(ModTags.Biomes.HAS_WILDBERRIES).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.FOREST, Biomes.FLOWER_FOREST,
				Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST,
				Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.CHERRY_GROVE, Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE,
				Biomes.BAMBOO_JUNGLE, Biomes.RIVER)
				.addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP);
		// the beehive feature itself skips snowy climates
		t.tagOf(ModTags.Biomes.HAS_BEEHIVES).addTag(BiomeTags.IS_OVERWORLD);
		t.tagOf(ModTags.Biomes.HAS_SLATE).addTag(BiomeTags.IS_OVERWORLD);
		t.tagOf(ModTags.Biomes.HAS_NETHER_SLATE).addTag(BiomeTags.IS_NETHER);
		t.tagOf(ModTags.Biomes.HAS_CAVE_HERBS).addTag(BiomeTags.IS_OVERWORLD);
		t.tagOf(ModTags.Biomes.HAS_NETHER_HERBS).addTag(BiomeTags.IS_NETHER);

		// surface herbs (legacy Herbs.getRandomHerbForBiome, first matching group wins)
		t.tagOf(ModTags.Biomes.HAS_JUNGLE_HERBS).add(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE)
				.addOptionalTag(Tags.Biomes.IS_JUNGLE);
		t.tagOf(ModTags.Biomes.HAS_DESERT_HERBS).add(Biomes.DESERT, Biomes.BEACH, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA,
				Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS)
				.addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_SAVANNA).addOptionalTag(Tags.Biomes.IS_BADLANDS);
		t.tagOf(ModTags.Biomes.HAS_MOUNTAIN_HERBS).add(Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_GRAVELLY_HILLS,
				Biomes.MEADOW, Biomes.CHERRY_GROVE, Biomes.STONY_PEAKS);
		t.tagOf(ModTags.Biomes.HAS_SWAMP_HERBS).add(Biomes.SWAMP, Biomes.MANGROVE_SWAMP).addOptionalTag(Tags.Biomes.IS_SWAMP);
		t.tagOf(ModTags.Biomes.HAS_FOREST_HERBS).add(Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
				Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.addOptionalTag(Tags.Biomes.IS_FOREST);
		t.tagOf(ModTags.Biomes.HAS_PLAINS_HERBS).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS).addOptionalTag(Tags.Biomes.IS_PLAINS);
	}

	// ================================================================ data maps

	public static void dataMaps(RusticDataMapProvider p) {
		// legacy IFuelHandler
		var fuels = p.map(NeoForgeDataMaps.FURNACE_FUELS);
		for (DeferredBlock<RusticSaplingBlock> sapling : List.of(ModBlocks.OLIVE_SAPLING, ModBlocks.IRONWOOD_SAPLING, ModBlocks.APPLE_SAPLING)) {
			fuels.add(sapling.getId(), new FurnaceFuel(100), false);
		}
		fuels.add(ModBlocks.WILDBERRY_BUSH.getId(), new FurnaceFuel(200), false);

		var compostables = p.map(NeoForgeDataMaps.COMPOSTABLES);
		for (DeferredBlock<? extends Block> block : List.of(ModBlocks.OLIVE_LEAVES, ModBlocks.IRONWOOD_LEAVES, ModBlocks.APPLE_LEAVES,
				ModBlocks.OLIVE_SAPLING, ModBlocks.IRONWOOD_SAPLING, ModBlocks.APPLE_SAPLING, ModBlocks.APPLE_SEEDS, ModBlocks.GRAPE_STEM)) {
			compostables.add(block.getId(), new Compostable(0.3F), false);
		}
		compostables.add(ModItems.TOMATO_SEEDS, new Compostable(0.3F), false);
		compostables.add(ModItems.CHILI_PEPPER_SEEDS, new Compostable(0.3F), false);
		compostables.add(ModItems.OLIVES, new Compostable(0.3F), false);
		compostables.add(ModItems.IRONBERRIES, new Compostable(0.3F), false);
		compostables.add(ModItems.WILDBERRIES, new Compostable(0.3F), false);
		compostables.add(ModItems.GRAPES, new Compostable(0.3F), false);
		compostables.add(ModItems.TOMATO, new Compostable(0.65F), false);
		compostables.add(ModItems.CHILI_PEPPER, new Compostable(0.65F), false);
		compostables.add(ModItems.GHOST_PEPPER, new Compostable(0.65F), false);
		compostables.add(ModBlocks.WILDBERRY_BUSH.getId(), new Compostable(0.65F), false);
		compostables.add(ModTags.Items.HERBS, new Compostable(0.65F), false);
	}

	// ================================================================ global loot modifiers

	public static void lootModifiers(RusticLootModifierProvider p) {
		LootItemCondition notSheared = MatchTool.toolMatches(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, Items.SHEARS)).invert().build();
		p.modifier("grass_seeds", new GrassSeedsModifier(new LootItemCondition[] {
				AnyOfCondition.anyOf(lootTable(Blocks.SHORT_GRASS), lootTable(Blocks.TALL_GRASS), lootTable(Blocks.FERN), lootTable(Blocks.LARGE_FERN))
						.build(),
				notSheared }, IGlobalLootModifier.DEFAULT_PRIORITY, List.of(ModItems.TOMATO_SEEDS.get(), ModItems.CHILI_PEPPER_SEEDS.get())));
		p.modifier("grape_seeds", new GrapeSeedsModifier(new LootItemCondition[] { lootTable(Blocks.VINE).build(), notSheared },
				IGlobalLootModifier.DEFAULT_PRIORITY, ModBlocks.GRAPE_STEM.get().asItem(), 0.1F));
	}

	private static LootTableIdCondition.Builder lootTable(Block block) {
		return LootTableIdCondition.builder(block.getLootTable().orElseThrow().identifier());
	}

	// ================================================================ world generation

	private static final ResourceKey<ConfiguredFeature<?, ?>> WILDBERRY_BUSH = configuredKey("wildberry_bush");
	private static final ResourceKey<ConfiguredFeature<?, ?>> BEEHIVE = configuredKey("beehive");
	private static final ResourceKey<ConfiguredFeature<?, ?>> SLATE = configuredKey("slate");
	private static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_SLATE = configuredKey("nether_slate");
	private static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_HERBS = configuredKey("cave_herbs");
	private static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_HERBS = configuredKey("nether_herbs");

	/** Surface herbs of one biome group (legacy Herbs.getRandomHerbForBiome). */
	private record HerbGroup(String name, TagKey<Biome> biomes, List<DeferredBlock<HerbBlock>> herbs) {
		ResourceKey<ConfiguredFeature<?, ?>> configured() {
			return configuredKey(name + "_herbs");
		}

		ResourceKey<PlacedFeature> placed() {
			return placedKey(name + "_herbs");
		}
	}

	private static final List<HerbGroup> HERB_GROUPS = List.of(
			new HerbGroup("jungle", ModTags.Biomes.HAS_JUNGLE_HERBS,
					List.of(ModBlocks.BLOOD_ORCHID, ModBlocks.HORSETAIL, ModBlocks.MARSH_MALLOW, ModBlocks.MOONCAP_MUSHROOM)),
			new HerbGroup("desert", ModTags.Biomes.HAS_DESERT_HERBS, List.of(ModBlocks.ALOE_VERA)),
			new HerbGroup("mountain", ModTags.Biomes.HAS_MOUNTAIN_HERBS, List.of(ModBlocks.WIND_THISTLE, ModBlocks.CLOUDSBLUFF)),
			new HerbGroup("swamp", ModTags.Biomes.HAS_SWAMP_HERBS, List.of(ModBlocks.CHAMOMILE, ModBlocks.HORSETAIL, ModBlocks.MARSH_MALLOW)),
			new HerbGroup("forest", ModTags.Biomes.HAS_FOREST_HERBS,
					List.of(ModBlocks.CHAMOMILE, ModBlocks.COHOSH, ModBlocks.GINSENG, ModBlocks.HORSETAIL, ModBlocks.VANTA_LILY)),
			new HerbGroup("plains", ModTags.Biomes.HAS_PLAINS_HERBS,
					List.of(ModBlocks.CHAMOMILE, ModBlocks.GINSENG, ModBlocks.HORSETAIL, ModBlocks.WIND_THISTLE, ModBlocks.VANTA_LILY)));

	private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, RusticRevived.id(name));
	}

	private static ResourceKey<PlacedFeature> placedKey(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, RusticRevived.id(name));
	}

	public static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
		// legacy WorldGenOliveTree / WorldGenIronwoodTree / WorldGenAppleTree: the vanilla small oak shape
		context.register(ModTreeGrowers.OLIVE_TREE, new ConfiguredFeature<>(Feature.TREE,
				tree(ModBlocks.OLIVE_LOG.get(), ModBlocks.OLIVE_LEAVES.get(), 4, 2)));
		context.register(ModTreeGrowers.IRONWOOD_TREE, new ConfiguredFeature<>(Feature.TREE,
				tree(ModBlocks.IRONWOOD_LOG.get(), ModBlocks.IRONWOOD_LEAVES.get(), 7, 5)));
		context.register(ModTreeGrowers.APPLE_TREE, new ConfiguredFeature<>(Feature.TREE,
				tree(Blocks.OAK_LOG, ModBlocks.APPLE_LEAVES.get(), 5, 1)));

		for (HerbGroup group : HERB_GROUPS) {
			WeightedList.Builder<BlockState> herbs = WeightedList.builder();
			group.herbs().forEach(herb -> herbs.add(herb.get().grownState(), 1));
			context.register(group.configured(), new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(herbs))));
		}
		context.register(CAVE_HERBS, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(
				WeightedList.<BlockState>builder().add(ModBlocks.CORE_ROOT.get().grownState(), 1)
						.add(ModBlocks.MOONCAP_MUSHROOM.get().grownState(), 1)))));
		context.register(NETHER_HERBS, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
				new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.DEATHSTALK_MUSHROOM.get().grownState()))));

		context.register(WILDBERRY_BUSH, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new RandomizedIntStateProvider(
				BlockStateProvider.simple(ModBlocks.WILDBERRY_BUSH.get()), BerryBushBlock.AGE, UniformInt.of(0, BerryBushBlock.MAX_AGE)))));
		context.register(BEEHIVE, new ConfiguredFeature<>(ModWorldGen.BEEHIVE.get(), NoneFeatureConfiguration.INSTANCE));

		// the vein size is read from the slateVeinSize config by the slate_vein feature
		context.register(SLATE, new ConfiguredFeature<>(ModWorldGen.SLATE_VEIN.get(), new OreConfiguration(
				new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.SLATE.get().defaultBlockState(), 20)));
		context.register(NETHER_SLATE, new ConfiguredFeature<>(ModWorldGen.SLATE_VEIN.get(), new OreConfiguration(
				new BlockMatchTest(Blocks.NETHERRACK), ModBlocks.SLATE.get().defaultBlockState(), 20)));
	}

	private static TreeConfiguration tree(Block log, Block leaves, int baseHeight, int randomHeight) {
		return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(log), new StraightTrunkPlacer(baseHeight, randomHeight, 0),
				BlockStateProvider.simple(leaves), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))
				.ignoreVines().build();
	}

	private static final ResourceKey<PlacedFeature> OLIVE_TREES = placedKey("olive_trees");
	private static final ResourceKey<PlacedFeature> IRONWOOD_TREES = placedKey("ironwood_trees");
	private static final ResourceKey<PlacedFeature> WILDBERRIES = placedKey("wildberries");
	private static final ResourceKey<PlacedFeature> BEEHIVES = placedKey("beehives");
	private static final ResourceKey<PlacedFeature> SLATE_VEINS = placedKey("slate");
	private static final ResourceKey<PlacedFeature> NETHER_SLATE_VEINS = placedKey("nether_slate");
	private static final ResourceKey<PlacedFeature> CAVE_HERB_PATCHES = placedKey("cave_herbs");
	private static final ResourceKey<PlacedFeature> NETHER_HERB_PATCHES = placedKey("nether_herbs");

	/** Legacy generators pick positions around the chunk centre: {@code 8 + rand(7) - rand(7)}. */
	private static final PlacementModifier AROUND_CHUNK_CENTER = RandomOffsetPlacement.horizontal(UniformInt.of(2, 14));

	public static void placedFeatures(BootstrapContext<PlacedFeature> context) {
		HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

		// legacy WorldGenAllTrees: chance per chunk, then N attempts within 5 blocks of the chunk centre
		PlacementUtils.register(context, OLIVE_TREES, features.getOrThrow(ModTreeGrowers.OLIVE_TREE),
				treePlacement("oliveGenChance", "maxOliveGenAttempts", ModBlocks.OLIVE_SAPLING.get()));
		PlacementUtils.register(context, IRONWOOD_TREES, features.getOrThrow(ModTreeGrowers.IRONWOOD_TREE),
				treePlacement("ironwoodGenChance", "maxIronwoodGenAttempts", ModBlocks.IRONWOOD_SAPLING.get()));

		for (HerbGroup group : HERB_GROUPS) {
			PlacementUtils.register(context, group.placed(), features.getOrThrow(group.configured()),
					new ConfigChancePlacement("herbGenChance"), new ConfigCountPlacement("maxHerbAttempts"), AROUND_CHUNK_CENTER,
					HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), BiomeFilter.biome(),
					BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
		}
		// legacy WorldGenCaveHerbs: y 10-41, dropping down to the cave floor
		PlacementUtils.register(context, CAVE_HERB_PATCHES, features.getOrThrow(CAVE_HERBS),
				new ConfigChancePlacement("herbGenChance"), new ConfigCountPlacement("maxHerbAttempts"), AROUND_CHUNK_CENTER,
				HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(41)),
				EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.hasSturdyFace(Direction.UP), BlockPredicate.ONLY_IN_AIR_PREDICATE, 32),
				RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome());
		// legacy WorldGenNetherHerbs: y 32-127, dropping at most 24 blocks
		PlacementUtils.register(context, NETHER_HERB_PATCHES, features.getOrThrow(NETHER_HERBS),
				new ConfigChancePlacement("herbGenChance"), new ConfigCountPlacement("maxHerbAttempts"), AROUND_CHUNK_CENTER,
				HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(127)),
				EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.hasSturdyFace(Direction.UP), BlockPredicate.ONLY_IN_AIR_PREDICATE, 24),
				RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome());

		PlacementUtils.register(context, WILDBERRIES, features.getOrThrow(WILDBERRY_BUSH),
				new ConfigChancePlacement("wildberryGenChance"), new ConfigCountPlacement("maxWildberryAttempts"), AROUND_CHUNK_CENTER,
				HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), BiomeFilter.biome(),
				BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
		// legacy WorldGenBeehive: within 7 blocks of the chunk centre, under the canopy
		PlacementUtils.register(context, BEEHIVES, features.getOrThrow(BEEHIVE),
				new ConfigChancePlacement("beehiveGenChance"), new ConfigCountPlacement("maxBeehiveAttempts"),
				RandomOffsetPlacement.horizontal(UniformInt.of(1, 15)), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

		PlacementUtils.register(context, SLATE_VEINS, features.getOrThrow(SLATE),
				new ConfigFilterPlacement("enableSlate", true), new ConfigFilterPlacement("netherSlate", false),
				new ConfigCountPlacement("slateVeinsPerChunk"), InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.absolute(4), VerticalAnchor.absolute(83)), BiomeFilter.biome());
		PlacementUtils.register(context, NETHER_SLATE_VEINS, features.getOrThrow(NETHER_SLATE),
				new ConfigFilterPlacement("enableSlate", true), new ConfigFilterPlacement("netherSlate", true),
				new ConfigCountPlacement("slateVeinsPerChunk"), InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.absolute(8), VerticalAnchor.absolute(119)), BiomeFilter.biome());
	}

	private static List<PlacementModifier> treePlacement(String chance, String attempts, Block sapling) {
		return List.of(new ConfigFilterPlacement("staticTrees", true), new ConfigChancePlacement(chance), new ConfigCountPlacement(attempts),
				RandomOffsetPlacement.horizontal(UniformInt.of(3, 13)), SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
				BiomeFilter.biome(), PlacementUtils.filteredByBlockSurvival(sapling));
	}

	public static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
		HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
		HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
		addFeature(context, placed, biomes, "olive_trees", ModTags.Biomes.HAS_OLIVE_TREES, OLIVE_TREES, GenerationStep.Decoration.VEGETAL_DECORATION);
		addFeature(context, placed, biomes, "ironwood_trees", ModTags.Biomes.HAS_IRONWOOD_TREES, IRONWOOD_TREES,
				GenerationStep.Decoration.VEGETAL_DECORATION);
		addFeature(context, placed, biomes, "wildberries", ModTags.Biomes.HAS_WILDBERRIES, WILDBERRIES, GenerationStep.Decoration.VEGETAL_DECORATION);
		for (HerbGroup group : HERB_GROUPS) {
			addFeature(context, placed, biomes, group.name() + "_herbs", group.biomes(), group.placed(), GenerationStep.Decoration.VEGETAL_DECORATION);
		}
		addFeature(context, placed, biomes, "cave_herbs", ModTags.Biomes.HAS_CAVE_HERBS, CAVE_HERB_PATCHES,
				GenerationStep.Decoration.UNDERGROUND_DECORATION);
		addFeature(context, placed, biomes, "nether_herbs", ModTags.Biomes.HAS_NETHER_HERBS, NETHER_HERB_PATCHES,
				GenerationStep.Decoration.UNDERGROUND_DECORATION);
		// after every tree has been placed
		addFeature(context, placed, biomes, "beehives", ModTags.Biomes.HAS_BEEHIVES, BEEHIVES, GenerationStep.Decoration.TOP_LAYER_MODIFICATION);
		addFeature(context, placed, biomes, "slate", ModTags.Biomes.HAS_SLATE, SLATE_VEINS, GenerationStep.Decoration.UNDERGROUND_ORES);
		addFeature(context, placed, biomes, "nether_slate", ModTags.Biomes.HAS_NETHER_SLATE, NETHER_SLATE_VEINS,
				GenerationStep.Decoration.UNDERGROUND_ORES);
	}

	private static void addFeature(BootstrapContext<BiomeModifier> context, HolderGetter<PlacedFeature> placed, HolderGetter<Biome> biomes,
			String name, TagKey<Biome> biomeTag, ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
		context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RusticRevived.id("add_" + name)),
				new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(biomeTag), HolderSet.direct(placed.getOrThrow(feature)), step));
	}
}
