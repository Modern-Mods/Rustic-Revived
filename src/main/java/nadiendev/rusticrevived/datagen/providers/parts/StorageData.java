package nadiendev.rusticrevived.datagen.providers.parts;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.storage.VaseBlock;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticBlockStateProvider;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.recipe.CabinetRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
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
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data generation for the storage and bees (vase, barrel, cabinet, apiary, beehive) subsystem.
 */
public final class StorageData {
	private StorageData() {
	}

	public static void blockStates(RusticBlockStateProvider p) {
		// vase: one model per design; designs 8+ use the alternative shape (legacy blockstates/vase.json)
		ModelFile[] designs = new ModelFile[VaseBlock.MAX_DESIGN + 1];
		for (int design = VaseBlock.MIN_DESIGN; design <= VaseBlock.MAX_DESIGN; design++) {
			designs[design] = p.models().withExistingParent("vases/vase_" + design, RusticRevived.id("block/vase_" + (design >= 8 ? 1 : 0)))
					.texture("side", p.blockTex("vases/vase_side_" + design))
					.texture("top", p.blockTex("vases/vase_top_" + design))
					.texture("bottom", p.blockTex("vases/vase_bottom_" + design))
					.renderType("cutout");
		}
		p.getVariantBuilder(ModBlocks.VASE.get())
				.forAllStates(state -> ConfiguredModel.builder().modelFile(designs[state.getValue(VaseBlock.DESIGN)]).build());
		ItemModelBuilder vaseItem = p.itemModels().getBuilder("vase").parent(designs[VaseBlock.MIN_DESIGN]);
		for (int design = VaseBlock.MIN_DESIGN + 1; design <= VaseBlock.MAX_DESIGN; design++) {
			vaseItem.override().predicate(RusticRevived.id("design"), design).model(designs[design]).end();
		}

		p.simpleBlock(ModBlocks.BARREL.get(), existing(p, "barrel"));
		p.blockItem(ModBlocks.BARREL.get());
		p.horizontalBlock(ModBlocks.APIARY.get(), existing(p, "apiary"));
		p.blockItem(ModBlocks.APIARY.get());
		p.horizontalBlock(ModBlocks.BEEHIVE.get(), existing(p, "beehive"));
		p.blockItem(ModBlocks.BEEHIVE.get());

		// the cabinet is drawn by its block entity renderer; the block model only provides the particles
		p.simpleBlock(ModBlocks.CABINET.get(), existing(p, "cabinet"));
		p.itemModels().getBuilder("cabinet").parent(new ModelFile.UncheckedModelFile("builtin/entity"))
				.texture("particle", p.blockTex("cabinet"))
				.transforms()
				.transform(ItemDisplayContext.GUI).rotation(30, 45, 0).scale(0.625F).end()
				.transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
				.transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).scale(0.5F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 225, 0).translation(0, 2.5F, 0).scale(0.375F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 225, 0).scale(0.4F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 45, 0).scale(0.4F).end()
				.end();
	}

	private static ModelFile existing(RusticBlockStateProvider p, String model) {
		return p.models().getExistingFile(RusticRevived.id("block/" + model));
	}

	public static void recipes(RusticRecipeProvider p, RecipeOutput out) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.VASE.get(), 6)
				.pattern(" # ").pattern("# #").pattern("###")
				.define('#', Items.TERRACOTTA)
				.unlockedBy("has_terracotta", RusticRecipeProvider.hasItem(Items.TERRACOTTA))
				.save(out, RusticRevived.id("vase"));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BARREL.get(), 2)
				.pattern("PSP").pattern("I I").pattern("PSP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", RusticRecipeProvider.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRevived.id("barrel"));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.APIARY.get())
				.pattern("LLL").pattern("P P").pattern("LLL")
				.define('L', ItemTags.LOGS).define('P', ItemTags.PLANKS)
				.unlockedBy("has_bee", RusticRecipeProvider.hasItem(ModItems.BEE.get()))
				.save(out, RusticRevived.id("apiary"));
		SpecialRecipeBuilder.special(CabinetRecipe::new).save(out, RusticRevived.id("cabinet"));
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
		return CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME);
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
		t.tagOf(ModTags.Blocks.BEE_GROWABLES).addTags(BlockTags.CROPS, BlockTags.SAPLINGS)
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
