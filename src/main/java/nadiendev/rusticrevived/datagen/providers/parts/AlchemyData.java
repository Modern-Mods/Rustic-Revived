package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.alchemy.AbstractCondenserBlock;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticBlockStateProvider;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.item.ElixirItem;
import nadiendev.rusticrevived.item.FluidBottleItem;
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
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
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
	private static final ResourceLocation DEFAULT_ITEM_PARENT = ResourceLocation.fromNamespaceAndPath("neoforge", "item/default");

	private AlchemyData() {
	}

	// ================================================================ block states & models

	public static void blockStates(RusticBlockStateProvider p) {
		condenser(p, ModBlocks.CONDENSER.get(), "condenser", "condenser_bottom", "condenser_front", "condenser_side", "condenser_top");
		condenser(p, ModBlocks.CONDENSER_ADVANCED.get(), "condenser_adv", "condenser_adv_top", "condenser_adv_front", "condenser_adv_side",
				"condenser_adv_top");
		p.horizontalBlock(ModBlocks.RETORT.get(), existing(p, "retort"));
		p.blockItem(ModBlocks.RETORT.get(), "retort");
		p.horizontalBlock(ModBlocks.RETORT_ADVANCED.get(), existing(p, "retort_adv"));
		p.blockItem(ModBlocks.RETORT_ADVANCED.get(), "retort_adv");
		p.horizontalBlock(ModBlocks.BREWING_BARREL.get(), existing(p, "brewing_barrel"));
		p.blockItem(ModBlocks.BREWING_BARREL.get());
		p.simpleBlock(ModBlocks.CRUSHING_TUB.get(), existing(p, "crushing_tub"));
		p.blockItem(ModBlocks.CRUSHING_TUB.get());
		p.simpleBlock(ModBlocks.EVAPORATING_BASIN.get(), existing(p, "evaporating_basin"));
		p.blockItem(ModBlocks.EVAPORATING_BASIN.get());
		p.simpleBlock(ModBlocks.LIQUID_BARREL.get(), existing(p, "liquid_barrel"));
		// the filled barrel item shows its fluid: drawn by LiquidBarrelItemRenderer
		p.itemModels().getBuilder("liquid_barrel").parent(new ModelFile.UncheckedModelFile("builtin/entity"))
				.texture("particle", p.blockTex("barrel"))
				.guiLight(BlockModel.GuiLight.SIDE)
				.transforms()
				.transform(ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.625F).end()
				.transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
				.transform(ItemDisplayContext.FIXED).scale(0.5F).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.4F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.4F).end()
				.end();

		fluidBottleModels(p);
		p.itemModels().withExistingParent("elixir", p.mcLoc("item/generated"))
				.texture("layer0", RusticRevived.id("item/elixir_overlay"))
				.texture("layer1", RusticRevived.id("item/elixir_bottle"));
		// materials produced by the alchemy recipes
		p.generatedItem("tiny_iron_dust", "item/dust_tiny_iron");
		p.generatedItem("gold_dust", "item/dust_gold");
		p.generatedItem("tallow", "item/tallow");
		p.generatedItem("beeswax", "item/beeswax");
		p.generatedItem("honeycomb", "item/honeycomb");
	}

	/** Bottom half: orientable cube, top half: the migrated model; both turned towards FACING. */
	private static void condenser(RusticBlockStateProvider p, Block block, String bottomName, String topTexture, String front, String side,
			String topModel) {
		BlockModelBuilder bottom = p.models().orientable(bottomName, p.blockTex(side), p.blockTex(front), p.blockTex(topTexture));
		ModelFile upper = existing(p, topModel);
		p.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
				.modelFile(state.getValue(AbstractCondenserBlock.BOTTOM) ? bottom : upper)
				.rotationY(((int) state.getValue(AbstractCondenserBlock.FACING).toYRot() + 180) % 360)
				.build());
		p.blockItem(block, bottomName);
	}

	/**
	 * Fluid bottle: a NeoForge dynamic fluid container (potion bottle + fluid masked by the potion
	 * overlay). Booze uses the labelled alcohol bottle, one model per booze selected by the
	 * {@link FluidBottleItem#BOOZE_MODEL_PROPERTY} item property.
	 */
	private static void fluidBottleModels(RusticBlockStateProvider p) {
		ItemModelBuilder bottle = p.itemModels().withExistingParent("fluid_bottle", DEFAULT_ITEM_PARENT)
				.texture("base", p.mcLoc("item/potion"))
				.texture("fluid", p.mcLoc("item/potion_overlay"))
				.customLoader(DynamicFluidContainerModelBuilder::begin).fluid(Fluids.EMPTY).end();
		for (int i = 0; i < ModFluids.BOOZE.size(); i++) {
			ModFluids.FluidEntry booze = ModFluids.BOOZE.get(i);
			ItemModelBuilder boozeBottle = p.itemModels().withExistingParent("fluid_bottle_" + booze.name, DEFAULT_ITEM_PARENT)
					.texture("base", RusticRevived.id("item/alcohol_bottle"))
					.texture("fluid", RusticRevived.id("item/alcohol_overlay"))
					.texture("cover", RusticRevived.id("item/alcohol_label"))
					.customLoader(DynamicFluidContainerModelBuilder::begin).fluid(booze.get()).coverIsMask(false).end();
			bottle.override().predicate(FluidBottleItem.BOOZE_MODEL_PROPERTY, i + 1).model(boozeBottle).end();
		}
	}

	private static ModelFile existing(RusticBlockStateProvider p, String model) {
		return p.models().getExistingFile(RusticRevived.id("block/" + model));
	}

	// ================================================================ recipes

	public static void recipes(RusticRecipeProvider p, RecipeOutput out) {
		craftingRecipes(out);
		smeltingRecipes(out);
		crushingRecipes(out);
		evaporatingRecipes(out);
		condenserRecipes(out);
		brewingRecipes(out);
		specialRecipes(out);
	}

	/** Legacy assets/rustic/recipes/*.json of the alchemy blocks. */
	private static void craftingRecipes(RecipeOutput out) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.CONDENSER.get())
				.pattern(" B ").pattern("BEB").pattern("BCB")
				.define('B', Items.BRICK).define('E', Items.BUCKET).define('C', Items.WHITE_TERRACOTTA)
				.group(RusticRevived.NAMESPACE + ":condenser")
				.unlockedBy("has_brick", RusticRecipeProvider.hasItem(Items.BRICK))
				.save(out, RusticRevived.id("condenser"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.CONDENSER_ADVANCED.get())
				.pattern(" B ").pattern("BEB").pattern("BIB")
				.define('B', Items.NETHER_BRICK).define('E', Items.BUCKET).define('I', Items.IRON_BLOCK)
				.group(RusticRevived.NAMESPACE + ":condenser")
				.unlockedBy("has_nether_brick", RusticRecipeProvider.hasItem(Items.NETHER_BRICK))
				.save(out, RusticRevived.id("condenser_advanced"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.RETORT.get())
				.pattern(" B").pattern("IE").pattern(" B")
				.define('B', Items.BRICK).define('E', Items.BUCKET).define('I', Tags.Items.INGOTS_IRON)
				.group(RusticRevived.NAMESPACE + ":retort")
				.unlockedBy("has_brick", RusticRecipeProvider.hasItem(Items.BRICK))
				.save(out, RusticRevived.id("retort"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.RETORT_ADVANCED.get())
				.pattern(" B").pattern("IE").pattern(" B")
				.define('B', Items.NETHER_BRICK).define('E', Items.BUCKET).define('I', Tags.Items.INGOTS_IRON)
				.group(RusticRevived.NAMESPACE + ":retort")
				.unlockedBy("has_nether_brick", RusticRecipeProvider.hasItem(Items.NETHER_BRICK))
				.save(out, RusticRevived.id("retort_advanced"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.BREWING_BARREL.get())
				.pattern("PIP").pattern("S S").pattern("PIP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", RusticRecipeProvider.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRevived.id("brewing_barrel"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.CRUSHING_TUB.get())
				.pattern("P P").pattern("I I").pattern("SSS")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", RusticRecipeProvider.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRevived.id("crushing_tub"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.LIQUID_BARREL.get(), 2)
				.pattern("P P").pattern("I I").pattern("PSP")
				.define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", RusticRecipeProvider.hasTag(Tags.Items.INGOTS_IRON))
				.save(out, RusticRevived.id("liquid_barrel"));
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModBlocks.EVAPORATING_BASIN.get())
				.pattern("# #").pattern(" # ")
				.define('#', Items.TERRACOTTA)
				.unlockedBy("has_terracotta", RusticRecipeProvider.hasItem(Items.TERRACOTTA))
				.save(out, RusticRevived.id("evaporating_basin"));

		// legacy RecipeNonIngredientReturn: the water bucket / emptied container is not given back
		noRemainder(out, "ale_wort", new ItemStack(ModFluids.ALE_WORT.bucket.get()), Items.BREAD, Items.SUGAR, Items.WATER_BUCKET);
		noRemainder(RusticRecipeProvider.whenConfig(out, "enableBottleEmptying"), "bottle_emptying", new ItemStack(Items.GLASS_BOTTLE),
				ModItems.FLUID_BOTTLE.get());
		noRemainder(out, "barrel_emptying", new ItemStack(ModBlocks.LIQUID_BARREL.get()), ModBlocks.LIQUID_BARREL.get());
	}

	private static void noRemainder(RecipeOutput out, String name, ItemStack result, ItemLike... ingredients) {
		NonNullList<Ingredient> list = NonNullList.of(Ingredient.EMPTY, Arrays.stream(ingredients).map(Ingredient::of).toArray(Ingredient[]::new));
		out.accept(RusticRevived.id(name), new NoRemainderShapelessRecipe("", CraftingBookCategory.MISC, result, list), null);
	}

	/** Legacy Recipes.addSmeltingRecipes (the olive log charcoal recipe belongs to the farm subsystem). */
	private static void smeltingRecipes(RecipeOutput out) {
		smelting(out, "beeswax", ModItems.HONEYCOMB.get(), ModItems.BEESWAX.get(), 0.3F);
		smelting(out, "iron_nugget_from_tiny_iron_dust", ModItems.TINY_IRON_DUST.get(), Items.IRON_NUGGET, 0.15F);
		smelting(out, "gold_ingot_from_gold_dust", ModItems.GOLD_DUST.get(), Items.GOLD_INGOT, 0.5F);
		smelting(RusticRecipeProvider.whenConfig(out, "fleshSmelting"), "tallow", Items.ROTTEN_FLESH, ModItems.TALLOW.get(), 0.3F);
	}

	private static void smelting(RecipeOutput out, String name, ItemLike input, ItemLike result, float experience) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 200)
				.unlockedBy(RusticRecipeProvider.hasName(input), RusticRecipeProvider.hasItem(input))
				.save(out, RusticRevived.id(name));
	}

	private static void crushingRecipes(RecipeOutput out) {
		ItemStack appleSeeds = new ItemStack(ModBlocks.APPLE_SEEDS.get());
		crushing(out, "olives", ModItems.OLIVES.get(), ModFluids.OLIVE_OIL.get(), 250, ItemStack.EMPTY);
		crushing(out, "ironberries", ModItems.IRONBERRIES.get(), ModFluids.IRONBERRY_JUICE.get(), 250, ItemStack.EMPTY);
		crushing(out, "sugar_cane", Items.SUGAR_CANE, Fluids.WATER, 250, new ItemStack(Items.SUGAR, 2));
		crushing(out, "wildberries", ModItems.WILDBERRIES.get(), ModFluids.WILDBERRY_JUICE.get(), 250, ItemStack.EMPTY);
		crushing(out, "grapes", ModItems.GRAPES.get(), ModFluids.GRAPE_JUICE.get(), 250, ItemStack.EMPTY);
		crushing(out, "apple", Items.APPLE, ModFluids.APPLE_JUICE.get(), 250, appleSeeds);
		crushing(out, "honeycomb", ModItems.HONEYCOMB.get(), ModFluids.HONEY.get(), 250, ItemStack.EMPTY);
		crushing(out, "golden_apple", Items.GOLDEN_APPLE, ModFluids.GOLDEN_APPLE_JUICE.get(), 100, appleSeeds);
		crushing(out, "enchanted_golden_apple", Items.ENCHANTED_GOLDEN_APPLE, ModFluids.GOLDEN_APPLE_JUICE.get(), 1000, appleSeeds);
		crushing(out, "vanta_lily", ModBlocks.VANTA_LILY.get(), ModFluids.VANTA_OIL.get(), 250, ItemStack.EMPTY);
	}

	private static void crushing(RecipeOutput out, String name, ItemLike input, Fluid fluid, int amount, ItemStack byproduct) {
		out.accept(RusticRevived.id("crushing_tub/" + name), new CrushingTubRecipe(Ingredient.of(input), new FluidStack(fluid, amount), byproduct), null);
	}

	private static void evaporatingRecipes(RecipeOutput out) {
		out.accept(RusticRevived.id("evaporating_basin/tiny_iron_dust"), new EvaporatingBasinRecipe(
				SizedFluidIngredient.of(ModFluids.IRONBERRY_JUICE.get(), 500), new ItemStack(ModItems.TINY_IRON_DUST.get()), 0), null);
		out.accept(RusticRevived.id("evaporating_basin/gold_dust"), new EvaporatingBasinRecipe(
				SizedFluidIngredient.of(ModFluids.GOLDEN_APPLE_JUICE.get(), 100), new ItemStack(ModItems.GOLD_DUST.get()), 0), null);
	}

	private static void condenserRecipes(RecipeOutput out) {
		Item horsetail = ModBlocks.HORSETAIL.get().asItem();
		Item marshMallow = ModItems.MARSH_MALLOW.get();

		basic(out, "instant_health", new MobEffectInstance(MobEffects.HEAL, 1), ModBlocks.CHAMOMILE.get(), Items.BEEF);
		advanced(out, "instant_health_strong", new MobEffectInstance(MobEffects.HEAL, 1, 1), marshMallow, ModBlocks.CHAMOMILE.get(), Items.BEEF);

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

		basic(out, "speed", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SHORT), ModBlocks.WIND_THISTLE.get(), Items.SUGAR);
		advanced(out, "speed_long", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, LONG), horsetail, ModBlocks.WIND_THISTLE.get(), Items.SUGAR);
		advanced(out, "speed_strong", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 1), marshMallow, ModBlocks.WIND_THISTLE.get(),
				Items.SUGAR);

		advancedFamily(out, "fire_resistance", MobEffects.FIRE_RESISTANCE, false, ModBlocks.ALOE_VERA.get(), Items.BRICK, Items.COAL);
		advancedFamily(out, "health_boost", MobEffects.HEALTH_BOOST, true, ModBlocks.BLOOD_ORCHID.get(), Items.ROTTEN_FLESH, Items.REDSTONE);
		advancedFamily(out, "haste", MobEffects.DIG_SPEED, true, ModItems.CORE_ROOT.get(), Items.IRON_NUGGET, Items.REDSTONE);
		advancedFamily(out, "strength", MobEffects.DAMAGE_BOOST, true, ModItems.GINSENG.get(), Items.BONE, Items.GUNPOWDER);
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

	private static void advanced(RecipeOutput out, String name, MobEffectInstance effect, ItemLike modifier, ItemLike... ingredients) {
		condenserRecipe(out, "condenser/advanced/" + name, true, effect, modifier, ingredients);
	}

	private static void condenserRecipe(RecipeOutput out, String id, boolean advanced, MobEffectInstance effect, ItemLike modifier,
			ItemLike... ingredients) {
		List<Ingredient> inputs = Arrays.stream(ingredients).map(Ingredient::of).toList();
		ItemStack elixir = ElixirItem.withEffects(ModItems.ELIXIR.get(), List.of(effect));
		out.accept(RusticRevived.id(id), new CondenserRecipe(advanced, inputs, Optional.ofNullable(modifier).map(Ingredient::of),
				CondenserRecipe.defaultBottle(), CondenserRecipe.defaultFluid(), 0, elixir), null);
	}

	private static void brewingRecipes(RecipeOutput out) {
		brewing(out, "ale", FluidIngredient.of(ModFluids.ALE_WORT.get()), ModFluids.ALE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "cider", FluidIngredient.of(ModFluids.APPLE_JUICE.get()), ModFluids.CIDER, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "iron_wine", FluidIngredient.of(ModFluids.IRONBERRY_JUICE.get()), ModFluids.IRON_WINE, BrewingRecipe.QualityMode.STANDARD);
		// any honey (legacy also brewed Forestry's "for.honey")
		brewing(out, "mead", FluidIngredient.tag(ModTags.Fluids.HONEY), ModFluids.MEAD, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "wildberry_wine", FluidIngredient.of(ModFluids.WILDBERRY_JUICE.get()), ModFluids.WILDBERRY_WINE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "wine", FluidIngredient.of(ModFluids.GRAPE_JUICE.get()), ModFluids.WINE, BrewingRecipe.QualityMode.STANDARD);
		brewing(out, "ambrosia", FluidIngredient.of(ModFluids.GOLDEN_APPLE_JUICE.get()), ModFluids.AMBROSIA, BrewingRecipe.QualityMode.AMBROSIA);
	}

	private static void brewing(RecipeOutput out, String name, FluidIngredient input, ModFluids.FluidEntry result, BrewingRecipe.QualityMode quality) {
		out.accept(RusticRevived.id("brewing/" + name), new BrewingRecipe(input, new FluidStack(result.get(), 1), quality), null);
	}

	private static void specialRecipes(RecipeOutput out) {
		SpecialRecipeBuilder.special(OliveOilRecipe::new).save(RusticRecipeProvider.whenConfig(out, "enableOliveOiling"), RusticRevived.id("olive_oiling"));
		SpecialRecipeBuilder.special(VantaOilRecipe::new).save(out, RusticRevived.id("vanta_oiling"));
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
						.apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(ModDataComponents.FLUID.get())))
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
		t.tagOf(ModTags.Items.VANTA_OILABLE).addTags(ItemTags.SWORDS, ItemTags.AXES).add(Items.STICK, Items.BONE);
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
