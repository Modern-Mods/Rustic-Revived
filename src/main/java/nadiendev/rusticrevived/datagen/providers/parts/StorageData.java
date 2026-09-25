package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.storage.VaseBlock;
import nadiendev.rusticrevived.client.storage.CabinetSpecialRenderer;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.recipe.CabinetRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data generation for the storage and bees (vase, barrel, cabinet, apiary, beehive) subsystem.
 */
public final class StorageData {
	/** Vase design model: one of the two hand-made vase shapes with the design textures. */
	private static final ModelTemplate[] VASE_TEMPLATES = {vaseTemplate(0), vaseTemplate(1)};
	/** Cabinet item base model: display transforms and particle of the special cabinet renderer. */
	private static final ModelTemplate CABINET_ITEM = ExtendedModelTemplateBuilder.builder()
			.requiredTextureSlot(TextureSlot.PARTICLE)
			.transform(ItemDisplayContext.GUI, t -> t.rotation(30, 45, 0).scale(0.625F))
			.transform(ItemDisplayContext.GROUND, t -> t.translation(0, 3, 0).scale(0.25F))
			.transform(ItemDisplayContext.FIXED, t -> t.rotation(0, 180, 0).scale(0.5F))
			.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(75, 225, 0).translation(0, 2.5F, 0).scale(0.375F))
			.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, 225, 0).scale(0.4F))
			.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 45, 0).scale(0.4F))
			.build();

	private StorageData() {
	}

	private static ModelTemplate vaseTemplate(int shape) {
		return new ModelTemplate(Optional.of(RusticModelProvider.existingBlockModel("vase_" + shape)), Optional.empty(), TextureSlot.SIDE,
				TextureSlot.TOP, TextureSlot.BOTTOM);
	}

	/** Block states, block models, item models and client item definitions. */
	public static void models(RusticModelProvider p, BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		// vase: one model per design; designs 8+ use the alternative shape (legacy blockstates/vase.json)
		Identifier[] designs = new Identifier[VaseBlock.MAX_DESIGN + 1];
		for (int design = VaseBlock.MIN_DESIGN; design <= VaseBlock.MAX_DESIGN; design++) {
			TextureMapping textures = new TextureMapping()
					.put(TextureSlot.SIDE, new Material(RusticModelProvider.blockTex("vases/vase_side_" + design)))
					.put(TextureSlot.TOP, new Material(RusticModelProvider.blockTex("vases/vase_top_" + design)))
					.put(TextureSlot.BOTTOM, new Material(RusticModelProvider.blockTex("vases/vase_bottom_" + design)));
			designs[design] = VASE_TEMPLATES[design >= 8 ? 1 : 0].create(RusticRevived.id("vases/vase_" + design), textures, blockModels.modelOutput);
		}
		Block vase = ModBlocks.VASE.get();
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(vase)
				.with(PropertyDispatch.initial(VaseBlock.DESIGN).generate(design -> BlockModelGenerators.plainVariant(designs[design]))));
		// the item selects the model of the design stored in its block_state component
		Map<Integer, ItemModel.Unbaked> designItems = new HashMap<>();
		for (int design = VaseBlock.MIN_DESIGN + 1; design <= VaseBlock.MAX_DESIGN; design++) {
			designItems.put(design, ItemModelUtils.plainModel(designs[design]));
		}
		itemModels.itemModelOutput.accept(vase.asItem(), ItemModelUtils.selectBlockItemProperty(VaseBlock.DESIGN,
				ItemModelUtils.plainModel(designs[VaseBlock.MIN_DESIGN]), designItems));

		simpleBlock(blockModels, ModBlocks.BARREL.get(), "barrel");
		horizontalBlock(blockModels, ModBlocks.APIARY.get(), "apiary");
		horizontalBlock(blockModels, ModBlocks.BEEHIVE.get(), "beehive");

		// the cabinet is drawn by its block entity renderer; the block model only provides the particles
		Block cabinet = ModBlocks.CABINET.get();
		TextureMapping cabinetParticle = new TextureMapping().put(TextureSlot.PARTICLE, new Material(RusticModelProvider.blockTex("cabinet")));
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(cabinet,
				BlockModelGenerators.plainVariant(ModelTemplates.PARTICLE_ONLY.create(cabinet, cabinetParticle, blockModels.modelOutput))));
		Identifier cabinetItem = CABINET_ITEM.create(ModelLocationUtils.getModelLocation(cabinet.asItem()), cabinetParticle, blockModels.modelOutput);
		itemModels.itemModelOutput.accept(cabinet.asItem(), ItemModelUtils.specialModel(cabinetItem, new CabinetSpecialRenderer.Unbaked()));
	}

	/** A block and its item using the hand-made model {@code rusticrevived:block/<model>}. */
	private static void simpleBlock(BlockModelGenerators blockModels, Block block, String model) {
		Identifier id = RusticModelProvider.existingBlockModel(model);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(id)));
		blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(id));
	}

	/** Like {@link #simpleBlock}, rotated by the horizontal facing (north unrotated). */
	private static void horizontalBlock(BlockModelGenerators blockModels, Block block, String model) {
		Identifier id = RusticModelProvider.existingBlockModel(model);
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(id))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
		blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(id));
	}

	public static void recipes(RusticRecipeProvider p) {
		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.VASE.get(), 6)
				.pattern(" # ").pattern("# #").pattern("###")
				.define('#', Items.TERRACOTTA)
				.unlockedBy("has_terracotta", p.hasItem(Items.TERRACOTTA))
				.save(p.output(), RusticRecipeProvider.key("vase"));
		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.BARREL.get(), 2)
				.pattern("PSP").pattern("I I").pattern("PSP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", p.hasTag(Tags.Items.INGOTS_IRON))
				.save(p.output(), RusticRecipeProvider.key("barrel"));
		p.shapedRecipe(RecipeCategory.MISC, ModBlocks.APIARY.get(), 1)
				.pattern("LLL").pattern("P P").pattern("LLL")
				.define('L', ItemTags.LOGS).define('P', ItemTags.PLANKS)
				.unlockedBy("has_bee", p.hasItem(ModItems.BEE.get()))
				.save(p.output(), RusticRecipeProvider.key("apiary"));
		SpecialRecipeBuilder.special(() -> CabinetRecipe.INSTANCE).save(p.output(), RusticRecipeProvider.key("cabinet"));
	}

	public static void blockLoot(RusticBlockLoot l) {
		Block vase = ModBlocks.VASE.get();
		// the default design carries no block_state component so it stacks with crafted vases
		LootItemFunction.Builder copyDesign = CopyBlockState.copyState(vase).copy(VaseBlock.DESIGN)
				.when(InvertedLootItemCondition.invert(LootItemBlockStatePropertyCondition.hasBlockStateProperties(vase)
						.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VaseBlock.DESIGN, VaseBlock.MIN_DESIGN))));
		l.table(vase, selfWithComponents(vase, copyName(), copyDesign));
		l.table(ModBlocks.BARREL.get(), selfWithComponents(ModBlocks.BARREL.get(), copyName()));
		l.table(ModBlocks.CABINET.get(), selfWithComponents(ModBlocks.CABINET.get(),
				copyName().include(ModDataComponents.CABINET_MATERIAL.get())));
		l.selfDrop(ModBlocks.APIARY.get());
		// legacy BlockBeehive#getDrops: 0-4 honeycomb and 1-2 bees
		l.table(ModBlocks.BEEHIVE.get(), LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ModItems.HONEYCOMB.get())
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 4)))))
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ModItems.BEE.get())
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))));
	}

	private static CopyComponentsFunction.Builder copyName() {
		return CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME);
	}

	private static LootTable.Builder selfWithComponents(Block block, LootItemFunction.Builder... functions) {
		LootItem.Builder<?> entry = LootItem.lootTableItem(block);
		for (LootItemFunction.Builder function : functions) {
			entry.apply(function);
		}
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry)
				.when(ExplosionCondition.survivesExplosion()));
	}

	public static void blockTags(RusticTagProviders.Blocks t) {
		t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.BARREL.get(), ModBlocks.CABINET.get(), ModBlocks.APIARY.get(), ModBlocks.BEEHIVE.get());
		t.tagOf(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.VASE.get());
		// legacy IGrowable / IPlantable blocks; any other BonemealableBlock is aged as well
		t.tagOf(ModTags.Blocks.BEE_GROWABLES).addTag(BlockTags.CROPS).addTag(BlockTags.SAPLINGS)
				.add(Blocks.CACTUS, Blocks.SUGAR_CANE, Blocks.NETHER_WART, Blocks.COCOA, Blocks.SWEET_BERRY_BUSH, Blocks.BAMBOO,
						Blocks.BAMBOO_SAPLING, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
	}

	public static void itemTags(RusticTagProviders.Items t) {
		t.tagOf(ModTags.Items.CABINET_MATERIALS).addTag(ItemTags.PLANKS);
	}

	/** 1.12 burned every wooden block item for 300 ticks. */
	public static void dataMaps(RusticDataMapProvider p) {
		p.map(NeoForgeDataMaps.FURNACE_FUELS)
				.add(ModBlocks.BARREL.getId(), new FurnaceFuel(300), false)
				.add(ModBlocks.CABINET.getId(), new FurnaceFuel(300), false)
				.add(ModBlocks.APIARY.getId(), new FurnaceFuel(300), false);
	}
}
