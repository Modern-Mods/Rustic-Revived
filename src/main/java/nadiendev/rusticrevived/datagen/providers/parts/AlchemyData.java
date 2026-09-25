package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.alchemy.AbstractCondenserBlock;
import nadiendev.rusticrevived.client.alchemy.BoozeLabelTint;
import nadiendev.rusticrevived.client.alchemy.BottleFluidProperty;
import nadiendev.rusticrevived.client.alchemy.LiquidBarrelItemRenderer;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.item.ElixirItem;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.recipe.NoRemainderShapelessRecipe;
import nadiendev.rusticrevived.recipe.OliveOilRecipe;
import nadiendev.rusticrevived.recipe.VantaOilRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.color.item.Potion;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data generation for the alchemy and brewing (condensers, retorts, brewing barrel, crushing tub,
 * drying basin, liquid barrel, fluid bottles, elixirs, oils) subsystem. Machine recipes are the
 * legacy Recipes.addCrushingTubRecipes / addEvaporatingRecipes / addCondenserRecipes /
 * addBrewingRecipes.
 */
public final class AlchemyData {
	/** Legacy duration of the short and long elixirs (ticks). */
	private static final int SHORT = 3600;
	private static final int LONG = 9600;
	/** Elixir colour without effects (legacy ElixirUtils.getColor). */
	private static final int NO_EFFECT_COLOR = 0xF800F8;

	private AlchemyData() {
	}

	// ================================================================ block states, models & client items

	/** Block states, block models, item models and client item definitions. */
	public static void models(RusticModelProvider p, BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		condenser(blockModels, ModBlocks.CONDENSER.get(), "condenser", "condenser_bottom", "condenser_front", "condenser_side", "condenser_top");
		condenser(blockModels, ModBlocks.CONDENSER_ADVANCED.get(), "condenser_adv", "condenser_adv_top", "condenser_adv_front", "condenser_adv_side",
				"condenser_adv_top");
		horizontal(blockModels, ModBlocks.RETORT.get(), "retort");
		horizontal(blockModels, ModBlocks.RETORT_ADVANCED.get(), "retort_adv");
		horizontal(blockModels, ModBlocks.BREWING_BARREL.get(), "brewing_barrel");
		simple(blockModels, ModBlocks.CRUSHING_TUB.get(), "crushing_tub");
		simple(blockModels, ModBlocks.EVAPORATING_BASIN.get(), "evaporating_basin");

		// the liquid barrel item draws its fluid over the block model
		Identifier liquidBarrel = RusticModelProvider.existingBlockModel("liquid_barrel");
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.LIQUID_BARREL.get(), BlockModelGenerators.plainVariant(liquidBarrel)));
		itemModels.itemModelOutput.accept(ModBlocks.LIQUID_BARREL.get().asItem(), ItemModelUtils.composite(ItemModelUtils.plainModel(liquidBarrel),
				ItemModelUtils.specialModel(liquidBarrel, new LiquidBarrelItemRenderer.Unbaked())));

		fluidBottle(itemModels);
		Identifier elixir = ModelTemplates.TWO_LAYERED_ITEM.create(ModItems.ELIXIR.get(),
				TextureMapping.layered(material(RusticModelProvider.itemTex("elixir_overlay")), material(RusticModelProvider.itemTex("elixir_bottle"))),
				itemModels.modelOutput);
		itemModels.itemModelOutput.accept(ModItems.ELIXIR.get(), ItemModelUtils.tintedModel(elixir, new Potion(NO_EFFECT_COLOR)));

		// materials produced by the alchemy recipes
		flatItem(itemModels, ModItems.TINY_IRON_DUST.get(), "dust_tiny_iron");
		flatItem(itemModels, ModItems.GOLD_DUST.get(), "dust_gold");
		flatItem(itemModels, ModItems.TALLOW.get(), "tallow");
		flatItem(itemModels, ModItems.BEESWAX.get(), "beeswax");
		flatItem(itemModels, ModItems.HONEYCOMB.get(), "honeycomb");
	}

	private static Material material(Identifier texture) {
		return new Material(texture);
	}

	/** Bottom half: orientable cube, top half: the hand-made model; both turned towards FACING. */
	private static void condenser(BlockModelGenerators blockModels, Block block, String bottomName, String topTexture, String front, String side,
			String topModel) {
		Identifier bottom = ModelTemplates.CUBE_ORIENTABLE.create(RusticRevived.id("block/" + bottomName), new TextureMapping()
				.put(TextureSlot.SIDE, material(RusticModelProvider.blockTex(side)))
				.put(TextureSlot.FRONT, material(RusticModelProvider.blockTex(front)))
				.put(TextureSlot.TOP, material(RusticModelProvider.blockTex(topTexture))), blockModels.modelOutput);
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
				.with(BlockModelGenerators.createBooleanModelDispatch(AbstractCondenserBlock.BOTTOM, BlockModelGenerators.plainVariant(bottom),
						BlockModelGenerators.plainVariant(RusticModelProvider.existingBlockModel(topModel))))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
		blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(bottom));
	}

	/** Hand-made model turned towards the horizontal FACING. */
	private static void horizontal(BlockModelGenerators blockModels, Block block, String model) {
		Identifier id = RusticModelProvider.existingBlockModel(model);
		blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(id))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
		blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(id));
	}

	private static void simple(BlockModelGenerators blockModels, Block block, String model) {
		Identifier id = RusticModelProvider.existingBlockModel(model);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(id)));
		blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(id));
	}

	private static void flatItem(ItemModelGenerators itemModels, Item item, String texture) {
		Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item),
				TextureMapping.layer0(material(RusticModelProvider.itemTex(texture))), itemModels.modelOutput);
		itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
	}

	/**
	 * Fluid bottle: a NeoForge dynamic fluid container (potion bottle + fluid masked by the potion
	 * overlay). Booze uses the alcohol bottle with a label tinted by the booze quality, selected by
	 * the {@link BottleFluidProperty} of the bottle.
	 */
	private static void fluidBottle(ItemModelGenerators itemModels) {
		ItemModel.Unbaked bottle = new DynamicFluidContainerModel.Unbaked(new DynamicFluidContainerModel.Textures(Optional.empty(),
				Optional.of(material(Identifier.withDefaultNamespace("item/potion"))),
				Optional.of(material(Identifier.withDefaultNamespace("item/potion_overlay"))), Optional.empty()),
				Fluids.EMPTY, false, true, true);
		Identifier label = ModelTemplates.FLAT_ITEM.create(RusticModelProvider.itemTex("alcohol_label"),
				TextureMapping.layer0(material(RusticModelProvider.itemTex("alcohol_label"))), itemModels.modelOutput);
		List<SelectItemModel.SwitchCase<Identifier>> cases = new ArrayList<>();
		for (ModFluids.FluidEntry booze : ModFluids.BOOZE) {
			ItemModel.Unbaked boozeBottle = new DynamicFluidContainerModel.Unbaked(new DynamicFluidContainerModel.Textures(Optional.empty(),
					Optional.of(material(RusticModelProvider.itemTex("alcohol_bottle"))),
					Optional.of(material(RusticModelProvider.itemTex("alcohol_overlay"))), Optional.empty()),
					booze.get(), false, true, true);
			cases.add(ItemModelUtils.when(BuiltInRegistries.FLUID.getKey(booze.get()),
					ItemModelUtils.composite(boozeBottle, ItemModelUtils.tintedModel(label, BoozeLabelTint.INSTANCE))));
		}
		itemModels.itemModelOutput.accept(ModItems.FLUID_BOTTLE.get(), ItemModelUtils.select(new BottleFluidProperty(), bottle, cases));
	}

	// ================================================================ recipes

	public static void recipes(RusticRecipeProvider p) {
		craftingRecipes(p);
		smeltingRecipes(p);
		crushingRecipes(p.output());
		evaporatingRecipes(p.output());
		condenserRecipes(p.output());
		brewingRecipes(p);
		p.whenConfig("enableOliveOiling").accept(RusticRecipeProvider.key("olive_oiling"), OliveOilRecipe.INSTANCE, null);
		p.output().accept(RusticRecipeProvider.key("vanta_oiling"), VantaOilRecipe.INSTANCE, null);
	}

	/** Legacy assets/rustic/recipes/*.json of the alchemy blocks. */
	private static void craftingRecipes(RusticRecipeProvider p) {
		RecipeOutput out = p.output();
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.CONDENSER.get(), 1)
				.pattern(" B ").pattern("BEB").pattern("BCB")
				.define('B', Items.BRICK).define('E', Items.BUCKET).define('C', Items.WHITE_TERRACOTTA)
				.group(RusticRevived.NAMESPACE + ":condenser")
				.unlockedBy("has_brick", p.hasItem(Items.BRICK))
				.save(out, RusticRecipeProvider.key("condenser"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.CONDENSER_ADVANCED.get(), 1)
				.pattern(" B ").pattern("BEB").pattern("BIB")
				.define('B', Items.NETHER_BRICK).define('E', Items.BUCKET).define('I', Items.IRON_BLOCK)
				.group(RusticRevived.NAMESPACE + ":condenser")
				.unlockedBy("has_nether_brick", p.hasItem(Items.NETHER_BRICK))
				.save(out, RusticRecipeProvider.key("condenser_advanced"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.RETORT.get(), 1)
				.pattern(" B").pattern("IE").pattern(" B")
				.define('B', Items.BRICK).define('E', Items.BUCKET).define('I', Tags.Items.INGOTS_IRON)
				.group(RusticRevived.NAMESPACE + ":retort")
				.unlockedBy("has_brick", p.hasItem(Items.BRICK))
				.save(out, RusticRecipeProvider.key("retort"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.RETORT_ADVANCED.get(), 1)
				.pattern(" B").pattern("IE").pattern(" B")
				.define('B', Items.NETHER_BRICK).define('E', Items.BUCKET).define('I', Tags.Items.INGOTS_IRON)
				.group(RusticRevived.NAMESPACE + ":retort")
				.unlockedBy("has_nether_brick", p.hasItem(Items.NETHER_BRICK))
				.save(out, RusticRecipeProvider.key("retort_advanced"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.BREWING_BARREL.get(), 1)
				.pattern("PIP").pattern("S S").pattern("PIP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", p.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRecipeProvider.key("brewing_barrel"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.CRUSHING_TUB.get(), 1)
				.pattern("P P").pattern("I I").pattern("SSS")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", p.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRecipeProvider.key("crushing_tub"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.LIQUID_BARREL.get(), 2)
				.pattern("P P").pattern("I I").pattern("PSP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", p.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRecipeProvider.key("liquid_barrel"));
		p.shapedRecipe(RecipeCategory.BREWING, ModBlocks.EVAPORATING_BASIN.get(), 1)
				.pattern("# #").pattern(" # ")
				.define('#', Items.TERRACOTTA)
				.unlockedBy("has_terracotta", p.hasItem(Items.TERRACOTTA))
				.save(out, RusticRecipeProvider.key("evaporating_basin"));

		// legacy RecipeNonIngredientReturn: the water bucket / emptied container is not given back
		noRemainder(out, "ale_wort", ModFluids.ALE_WORT.bucket.get(), Items.BREAD, Items.SUGAR, Items.WATER_BUCKET);
		noRemainder(p.whenConfig("enableBottleEmptying"), "bottle_emptying", Items.GLASS_BOTTLE, ModItems.FLUID_BOTTLE.get());
		noRemainder(out, "barrel_emptying", ModBlocks.LIQUID_BARREL.get(), ModBlocks.LIQUID_BARREL.get());
	}

	private static void noRemainder(RecipeOutput out, String name, ItemLike result, ItemLike... ingredients) {
		List<Ingredient> list = Arrays.stream(ingredients).map(Ingredient::of).toList();
		out.accept(RusticRecipeProvider.key(name), new NoRemainderShapelessRecipe(new Recipe.CommonInfo(true),
				new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), new ItemStackTemplate(result.asItem()), list), null);
	}

	/** Legacy Recipes.addSmeltingRecipes (the olive log charcoal recipe belongs to the farm subsystem). */
	private static void smeltingRecipes(RusticRecipeProvider p) {
		smelting(p, p.output(), "beeswax", ModItems.HONEYCOMB.get(), ModItems.BEESWAX.get(), 0.3F);
		smelting(p, p.output(), "iron_nugget_from_tiny_iron_dust", ModItems.TINY_IRON_DUST.get(), Items.IRON_NUGGET, 0.15F);
		smelting(p, p.output(), "gold_ingot_from_gold_dust", ModItems.GOLD_DUST.get(), Items.GOLD_INGOT, 0.5F);
		smelting(p, p.whenConfig("fleshSmelting"), "tallow", Items.ROTTEN_FLESH, ModItems.TALLOW.get(), 0.3F);
	}

	private static void smelting(RusticRecipeProvider p, RecipeOutput out, String name, ItemLike input, ItemLike result, float experience) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, CookingBookCategory.MISC, result, experience, 200)
				.unlockedBy(RusticRecipeProvider.hasName(input), p.hasItem(input))
				.save(out, RusticRecipeProvider.key(name));
	}

	private static void crushingRecipes(RecipeOutput out) {
		ItemStackTemplate appleSeeds = new ItemStackTemplate(ModBlocks.APPLE_SEEDS.get().asItem());
		crushing(out, "olives", ModItems.OLIVES.get(), ModFluids.OLIVE_OIL.get(), 250, null);
		crushing(out, "ironberries", ModItems.IRONBERRIES.get(), ModFluids.IRONBERRY_JUICE.get(), 250, null);
		crushing(out, "sugar_cane", Items.SUGAR_CANE, Fluids.WATER, 250, new ItemStackTemplate(Items.SUGAR, 2));
		crushing(out, "wildberries", ModItems.WILDBERRIES.get(), ModFluids.WILDBERRY_JUICE.get(), 250, null);
		crushing(out, "grapes", ModItems.GRAPES.get(), ModFluids.GRAPE_JUICE.get(), 250, null);
		crushing(out, "apple", Items.APPLE, ModFluids.APPLE_JUICE.get(), 250, appleSeeds);
		crushing(out, "honeycomb", ModItems.HONEYCOMB.get(), ModFluids.HONEY.get(), 250, null);
		crushing(out, "golden_apple", Items.GOLDEN_APPLE, ModFluids.GOLDEN_APPLE_JUICE.get(), 100, appleSeeds);
		crushing(out, "enchanted_golden_apple", Items.ENCHANTED_GOLDEN_APPLE, ModFluids.GOLDEN_APPLE_JUICE.get(), 1000, appleSeeds);
		crushing(out, "vanta_lily", ModBlocks.VANTA_LILY.get(), ModFluids.VANTA_OIL.get(), 250, null);
	}

	private static void crushing(RecipeOutput out, String name, ItemLike input, Fluid fluid, int amount, @Nullable ItemStackTemplate byproduct) {
		out.accept(RusticRecipeProvider.key("crushing_tub/" + name),
				new CrushingTubRecipe(Ingredient.of(input), new FluidStackTemplate(fluid, amount), Optional.ofNullable(byproduct)), null);
	}

	private static void evaporatingRecipes(RecipeOutput out) {
		out.accept(RusticRecipeProvider.key("evaporating_basin/tiny_iron_dust"), new EvaporatingBasinRecipe(
				SizedFluidIngredient.of(ModFluids.IRONBERRY_JUICE.get(), 500), new ItemStackTemplate(ModItems.TINY_IRON_DUST.get()), 0), null);
		out.accept(RusticRecipeProvider.key("evaporating_basin/gold_dust"), new EvaporatingBasinRecipe(
				SizedFluidIngredient.of(ModFluids.GOLDEN_APPLE_JUICE.get(), 100), new ItemStackTemplate(ModItems.GOLD_DUST.get()), 0), null);
	}

	private static void condenserRecipes(RecipeOutput out) {
		Item horsetail = ModBlocks.HORSETAIL.get().asItem();
		Item marshMallow = ModItems.MARSH_MALLOW.get();

		basic(out, "instant_health", new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1), ModBlocks.CHAMOMILE.get(), Items.BEEF);
		advanced(out, "instant_health_strong", new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 1), marshMallow, ModBlocks.CHAMOMILE.get(), Items.BEEF);

		basic(out, "regeneration", new MobEffectInstance(MobEffects.REGENERATION, 900), ModBlocks.COHOSH.get(), ModItems.HONEYCOMB.get());
		advanced(out, "regeneration_long", new MobEffectInstance(MobEffects.REGENERATION, 1800), horsetail, ModBlocks.COHOSH.get(),
				ModItems.HONEYCOMB.get());
		advanced(out, "regeneration_strong", new MobEffectInstance(MobEffects.REGENERATION, 450, 1), marshMallow, ModBlocks.COHOSH.get(),
				ModItems.HONEYCOMB.get());

		basic(out, "wither", new MobEffectInstance(MobEffects.WITHER, 900), ModBlocks.DEATHSTALK_MUSHROOM.get(), Items.SOUL_SAND);
		advanced(out, "wither_long", new MobEffectInstance(MobEffects.WITHER, 1800), horsetail, ModBlocks.DEATHSTALK_MUSHROOM.get(), Items.SOUL_SAND);
		advanced(out, "wither_strong", new MobEffectInstance(MobEffects.WITHER, 450, 1), marshMallow, ModBlocks.DEATHSTALK_MUSHROOM.get(),
				Items.SOUL_SAND);

		basic(out, "night_vision", new MobEffectInstance(MobEffects.NIGHT_VISION, SHORT), ModBlocks.MOONCAP_MUSHROOM.get(), Items.SPIDER_EYE);
		advanced(out, "night_vision_long", new MobEffectInstance(MobEffects.NIGHT_VISION, LONG), horsetail, ModBlocks.MOONCAP_MUSHROOM.get(),
				Items.SPIDER_EYE);

		basic(out, "speed", new MobEffectInstance(MobEffects.SPEED, SHORT), ModBlocks.WIND_THISTLE.get(), Items.SUGAR);
		advanced(out, "speed_long", new MobEffectInstance(MobEffects.SPEED, LONG), horsetail, ModBlocks.WIND_THISTLE.get(), Items.SUGAR);
		advanced(out, "speed_strong", new MobEffectInstance(MobEffects.SPEED, 1800, 1), marshMallow, ModBlocks.WIND_THISTLE.get(),
				Items.SUGAR);

		advancedFamily(out, "fire_resistance", MobEffects.FIRE_RESISTANCE, false, ModBlocks.ALOE_VERA.get(), Items.BRICK, Items.COAL);
		advancedFamily(out, "health_boost", MobEffects.HEALTH_BOOST, true, ModBlocks.BLOOD_ORCHID.get(), Items.ROTTEN_FLESH, Items.REDSTONE);
		advancedFamily(out, "haste", MobEffects.HASTE, true, ModItems.CORE_ROOT.get(), Items.IRON_NUGGET, Items.REDSTONE);
		advancedFamily(out, "strength", MobEffects.STRENGTH, true, ModItems.GINSENG.get(), Items.BONE, Items.GUNPOWDER);
		advancedFamily(out, "iron_skin", ModEffects.IRON_SKIN, true, ModItems.IRONBERRIES.get(), Items.LEATHER, Items.CLAY_BALL);
		advancedFamily(out, "feather", ModEffects.FEATHER, false, ModItems.CLOUDSBLUFF.get(), Items.FEATHER, Items.PAPER);
		advancedFamily(out, "blazing_trail", ModEffects.BLAZING_TRAIL, false, ModItems.CHILI_PEPPER.get(), Items.BLAZE_POWDER, Items.NETHERRACK);
	}

	/**
	 * Advanced-only elixir: 3 minutes without modifier, 8 minutes with horsetail and (optionally)
	 * 1:30 of level II with marsh mallow.
	 */
	private static void advancedFamily(RecipeOutput out, String name, Holder<MobEffect> effect, boolean strong, ItemLike... ingredients) {
		advanced(out, name, new MobEffectInstance(effect, SHORT), null, ingredients);
		advanced(out, name + "_long", new MobEffectInstance(effect, LONG), ModBlocks.HORSETAIL.get(), ingredients);
		if (strong) {
			advanced(out, name + "_strong", new MobEffectInstance(effect, 1800, 1), ModItems.MARSH_MALLOW.get(), ingredients);
		}
	}

	private static void basic(RecipeOutput out, String name, MobEffectInstance effect, ItemLike... ingredients) {
		condenserRecipe(out, "condenser/" + name, false, effect, null, ingredients);
	}

	private static void advanced(RecipeOutput out, String name, MobEffectInstance effect, @Nullable ItemLike modifier, ItemLike... ingredients) {
		condenserRecipe(out, "condenser/advanced/" + name, true, effect, modifier, ingredients);
	}

	private static void condenserRecipe(RecipeOutput out, String id, boolean advanced, MobEffectInstance effect, @Nullable ItemLike modifier,
			ItemLike... ingredients) {
		List<Ingredient> inputs = Arrays.stream(ingredients).map(Ingredient::of).toList();
		out.accept(RusticRecipeProvider.key(id), new CondenserRecipe(advanced, inputs, Optional.ofNullable(modifier).map(Ingredient::of),
				CondenserRecipe.defaultBottle(), CondenserRecipe.defaultFluid(), 0, ElixirItem.template(ModItems.ELIXIR.get(), List.of(effect))), null);
	}

	private static void brewingRecipes(RusticRecipeProvider p) {
		RecipeOutput out = p.output();
		brewing(out, "ale", FluidIngredient.of(ModFluids.ALE_WORT.get()), ModFluids.ALE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "cider", FluidIngredient.of(ModFluids.APPLE_JUICE.get()), ModFluids.CIDER, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "iron_wine", FluidIngredient.of(ModFluids.IRONBERRY_JUICE.get()), ModFluids.IRON_WINE, BrewingRecipe.QualityMode.STANDARD);
		// any honey (legacy also brewed Forestry's "for.honey")
		brewing(out, "mead", FluidIngredient.of(p.registries().lookupOrThrow(Registries.FLUID).getOrThrow(ModTags.Fluids.HONEY)), ModFluids.MEAD,
				BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "wildberry_wine", FluidIngredient.of(ModFluids.WILDBERRY_JUICE.get()), ModFluids.WILDBERRY_WINE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "wine", FluidIngredient.of(ModFluids.GRAPE_JUICE.get()), ModFluids.WINE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "ambrosia", FluidIngredient.of(ModFluids.GOLDEN_APPLE_JUICE.get()), ModFluids.AMBROSIA, BrewingRecipe.QualityMode.AMBROSIA);
	}

	private static void brewing(RecipeOutput out, String name, FluidIngredient input, ModFluids.FluidEntry result, BrewingRecipe.QualityMode quality) {
		out.accept(RusticRecipeProvider.key("brewing/" + name), new BrewingRecipe(input, new FluidStackTemplate(result.get(), 1), quality), null);
	}

	// ================================================================ loot, tags, data maps

	public static void blockLoot(RusticBlockLoot l) {
		condenserLoot(l, ModBlocks.CONDENSER.get());
		condenserLoot(l, ModBlocks.CONDENSER_ADVANCED.get());
		l.selfDrop(ModBlocks.RETORT.get());
		l.selfDrop(ModBlocks.RETORT_ADVANCED.get());
		l.selfDrop(ModBlocks.BREWING_BARREL.get());
		l.selfDrop(ModBlocks.CRUSHING_TUB.get());
		l.selfDrop(ModBlocks.EVAPORATING_BASIN.get());
		// the barrel keeps its fluid (legacy BlockLiquidBarrel#onBlockHarvested)
		l.table(ModBlocks.LIQUID_BARREL.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(ModBlocks.LIQUID_BARREL.get())
						.apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(ModDataComponents.FLUID.get())))
				.when(ExplosionCondition.survivesExplosion())));
	}

	/** Only the bottom half drops the condenser. */
	private static void condenserLoot(RusticBlockLoot l, Block condenser) {
		l.table(condenser, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(condenser).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(condenser)
						.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(AbstractCondenserBlock.BOTTOM, true))))
				.when(ExplosionCondition.survivesExplosion())));
	}

	public static void blockTags(RusticTagProviders.Blocks t) {
		t.tagOf(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.CONDENSER.get(), ModBlocks.CONDENSER_ADVANCED.get(), ModBlocks.RETORT.get(),
				ModBlocks.RETORT_ADVANCED.get(), ModBlocks.EVAPORATING_BASIN.get());
		t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.BREWING_BARREL.get(), ModBlocks.CRUSHING_TUB.get(), ModBlocks.LIQUID_BARREL.get());
	}

	public static void itemTags(RusticTagProviders.Items t) {
		// legacy "Vanta Oil Whitelist" defaults (the config list still applies on top of the tag)
		t.tagOf(ModTags.Items.VANTA_OILABLE).addTag(ItemTags.SWORDS).addTag(ItemTags.AXES).add(Items.STICK, Items.BONE);
		t.tagOf(ModTags.Items.OLIVE_OIL_BLACKLIST);
	}

	/** 1.12 burned every wooden block item for 300 ticks. */
	public static void dataMaps(RusticDataMapProvider p) {
		p.map(NeoForgeDataMaps.FURNACE_FUELS)
				.add(ModBlocks.BREWING_BARREL.getId(), new FurnaceFuel(300), false)
				.add(ModBlocks.CRUSHING_TUB.getId(), new FurnaceFuel(300), false)
				.add(ModBlocks.LIQUID_BARREL.getId(), new FurnaceFuel(300), false);
	}

	public static void fluidTags(RusticTagProviders.Fluids t) {
	}
}
