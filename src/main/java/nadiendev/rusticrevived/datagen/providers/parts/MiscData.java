package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.advancement.BoozeItemPredicate;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticLootModifierProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.registry.ModBanners;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModCriteria;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import nadiendev.rusticrevived.util.RusticDamage;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EffectsChangedTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Data generation shared by everything: fluids, damage types, banner patterns, entity tags, the
 * Almanac (item model, recipes) and advancements.
 * <p>
 * Client item definitions generated here: every fluid bucket, {@code almanac}, {@code bee} and
 * {@code eris_banner_pattern}.
 */
public final class MiscData {
	/** Face names of the almanac model elements below, in the order of their UVs. */
	private static final String[] BOOK_FACES = { "north", "east", "south", "west", "up", "down" };

	private MiscData() {
	}

	/** Block states, block models, item models and client item definitions. */
	public static void models(RusticModelProvider p, BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		Material bucketBase = new Material(Identifier.withDefaultNamespace("item/bucket"));
		Material bucketMask = new Material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"));
		for (ModFluids.FluidEntry fluid : ModFluids.ALL) {
			if (fluid.block != null) {
				// placed juices are drawn by the fluid renderer: the block model only provides the particle
				blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(fluid.block.get(), BlockModelGenerators.plainVariant(
						ModelTemplates.PARTICLE_ONLY.create(fluid.block.get(), TextureMapping.particle(new Material(fluid.stillTexture())),
								blockModels.modelOutput))));
			}
			if (fluid.bucket != null) {
				itemModels.itemModelOutput.accept(fluid.bucket.get(), new DynamicFluidContainerModel.Unbaked(new DynamicFluidContainerModel.Textures(
						Optional.empty(), Optional.of(bucketBase), Optional.of(bucketMask), Optional.empty()), fluid.get(), false, true, true));
			}
		}
		itemModels.generateFlatItem(ModItems.ERIS_BANNER_PATTERN.get(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(ModItems.BEE.get(), ModelTemplates.FLAT_ITEM);
		almanacModel(itemModels);
	}

	/**
	 * The Almanac: a small closed book (legacy models/item/book.json). The "Almanac" title that legacy
	 * BookBakedModel rendered with the font is baked into the texture.
	 */
	private static void almanacModel(ItemModelGenerators itemModels) {
		Identifier id = RusticRevived.id("item/almanac");
		JsonObject textures = new JsonObject();
		textures.addProperty("0", id.toString());
		textures.addProperty("particle", id.toString());
		JsonArray elements = new JsonArray();
		// back cover, pages, spine, front cover: from, to, then per face (north, east, south, west, up, down) u1, v1, u2, v2, rotation
		elements.add(bookElement(new float[] { 4, 0, 2 }, new float[] { 12, 0.5F, 14 }, new float[][] {
				{ 0, 0, 4, 0.5F, 0 }, { 0, 0, 0.5F, 6, 90 }, { 0, 5.5F, 4, 6, 0 }, { 3.5F, 0, 4, 6, 270 }, { 4, 0, 0, 6, 0 }, { 4, 6, 0, 0, 0 } }));
		elements.add(bookElement(new float[] { 4, 0.5F, 2.5F }, new float[] { 11.5F, 3.5F, 13.5F }, new float[][] {
				{ 0, 11.5F, 4, 16, 0 }, { 4, 6, 8.5F, 11.5F, 90 }, { 0, 11.5F, 4, 16, 0 }, { 4, 6, 8.5F, 11.5F, 270 }, { 0, 6, 4, 11.5F, 0 },
				{ 0, 6, 4, 11.5F, 0 } }));
		elements.add(bookElement(new float[] { 3.5F, 0.5F, 2 }, new float[] { 4, 3.5F, 14 }, new float[][] {
				{ 4, 0, 5.5F, 0.5F, 90 }, { 4, 0, 5.5F, 6, 90 }, { 4, 5.5F, 5.5F, 6, 90 }, { 4, 0, 5.5F, 6, 270 }, { 5, 0, 5.5F, 6, 0 },
				{ 4, 0, 4.5F, 6, 0 } }));
		elements.add(bookElement(new float[] { 4, 3.5F, 2 }, new float[] { 12, 4, 14 }, new float[][] {
				{ 5.5F, 0, 9.5F, 0.5F, 0 }, { 9, 0, 9.5F, 6, 90 }, { 5.5F, 5.5F, 9.5F, 6, 0 }, { 5.5F, 0, 6, 6, 270 }, { 5.5F, 0, 9.5F, 6, 0 },
				{ 5.5F, 6, 9.5F, 0, 0 } }));
		JsonObject display = new JsonObject();
		display.add("gui", transform(new float[] { 45, -55, 0 }, new float[] { 0, 4.2F, 0 }, 1));
		display.add("ground", transform(new float[] { 0, 0, 0 }, new float[] { 0, 2, 0 }, 0.5F));
		display.add("head", transform(new float[] { 0, 180, 0 }, new float[] { 0, 13, 7 }, 1));
		display.add("thirdperson_righthand", transform(new float[] { 90, 0, 0 }, new float[] { -2, 3, 4.5F }, 0.55F));
		display.add("firstperson_righthand", transform(new float[] { 70, -10, 65 }, new float[] { -2.26F, 4.7F, 1.13F }, 0.68F));
		display.add("fixed", transform(new float[] { 90, 0, 180 }, new float[] { -0.25F, 0, -7 }, 1));

		JsonObject model = new JsonObject();
		model.add("textures", textures);
		model.add("elements", elements);
		model.add("display", display);
		itemModels.modelOutput.accept(id, () -> model);
		itemModels.itemModelOutput.accept(ModItems.ALMANAC.get(), ItemModelUtils.plainModel(id));
	}

	private static JsonObject bookElement(float[] from, float[] to, float[][] faces) {
		JsonObject element = new JsonObject();
		element.add("from", array(from));
		element.add("to", array(to));
		JsonObject faceObjects = new JsonObject();
		for (int i = 0; i < BOOK_FACES.length; i++) {
			float[] face = faces[i];
			JsonObject faceObject = new JsonObject();
			faceObject.addProperty("texture", "#0");
			faceObject.add("uv", array(new float[] { face[0], face[1], face[2], face[3] }));
			if (face[4] != 0) {
				faceObject.addProperty("rotation", (int) face[4]);
			}
			faceObjects.add(BOOK_FACES[i], faceObject);
		}
		element.add("faces", faceObjects);
		return element;
	}

	private static JsonObject transform(float[] rotation, float[] translation, float scale) {
		JsonObject transform = new JsonObject();
		transform.add("rotation", array(rotation));
		transform.add("translation", array(translation));
		transform.add("scale", array(new float[] { scale, scale, scale }));
		return transform;
	}

	private static JsonArray array(float[] values) {
		JsonArray array = new JsonArray();
		for (float value : values) {
			array.add(value);
		}
		return array;
	}

	public static void recipes(RusticRecipeProvider p) {
		p.shapedRecipe(RecipeCategory.MISC, ModItems.ALMANAC.get(), 1)
				.pattern(" O ")
				.pattern("IBI")
				.pattern(" I ")
				.define('O', ModItems.OLIVES.get())
				.define('I', Tags.Items.NUGGETS_IRON)
				.define('B', Items.BOOK)
				.unlockedBy(RusticRecipeProvider.hasName(ModItems.OLIVES.get()), p.hasItem(ModItems.OLIVES.get()))
				.unlockedBy(RusticRecipeProvider.hasName(Items.BOOK), p.hasItem(Items.BOOK))
				.save(p.output(), RusticRecipeProvider.key("almanac"));
		// legacy applied the Eris symbol with the almanac itself; the almanac is given back (AlmanacItem remainder)
		p.shapelessRecipe(RecipeCategory.MISC, ModItems.ERIS_BANNER_PATTERN.get(), 1)
				.requires(Items.PAPER)
				.requires(ModItems.ALMANAC.get())
				.unlockedBy(RusticRecipeProvider.hasName(ModItems.ALMANAC.get()), p.hasItem(ModItems.ALMANAC.get()))
				.save(p.output(), RusticRecipeProvider.key("eris_banner_pattern"));
	}

	public static void blockLoot(RusticBlockLoot l) {
	}

	public static void blockTags(RusticTagProviders.Blocks t) {
	}

	public static void itemTags(RusticTagProviders.Items t) {
		// legacy ore dictionary: wax, tallow, materialHoneycomb, dustGold, dustTinyIron
		t.tagOf(ModTags.Items.WAX).add(ModItems.BEESWAX.get(), ModItems.TALLOW.get());
		t.tagOf(ModTags.Items.WAX_COMMON).add(ModItems.BEESWAX.get(), ModItems.TALLOW.get());
		t.tagOf(ModTags.Items.HONEYCOMBS).add(ModItems.HONEYCOMB.get());
		t.tagOf(ModTags.Items.DUSTS_GOLD).add(ModItems.GOLD_DUST.get());
		t.tagOf(ModTags.Items.TINY_DUSTS_IRON).add(ModItems.TINY_IRON_DUST.get());
	}

	public static void dataMaps(RusticDataMapProvider p) {
	}

	public static void fluidTags(RusticTagProviders.Fluids t) {
		t.tagOf(ModTags.Fluids.HONEY).add(ModFluids.HONEY.get());
		t.tagOf(ModTags.Fluids.OLIVE_OIL).add(ModFluids.OLIVE_OIL.get());
		t.tagOf(ModTags.Fluids.SEED_OIL).add(ModFluids.OLIVE_OIL.get());
		t.tagOf(ModTags.Fluids.APPLE_JUICE).add(ModFluids.APPLE_JUICE.get());
		t.tagOf(ModTags.Fluids.GRAPE_JUICE).add(ModFluids.GRAPE_JUICE.get());
		t.tagOf(ModTags.Fluids.JUICES).add(ModFluids.APPLE_JUICE.get(), ModFluids.GRAPE_JUICE.get(), ModFluids.IRONBERRY_JUICE.get(),
				ModFluids.WILDBERRY_JUICE.get(), ModFluids.GOLDEN_APPLE_JUICE.get());
		var booze = t.tagOf(ModTags.Fluids.BOOZE);
		for (ModFluids.FluidEntry fluid : ModFluids.BOOZE) {
			booze.add(fluid.get());
		}
		t.tagOf(ModTags.Fluids.BREWABLE).add(ModFluids.ALE_WORT.get(), ModFluids.APPLE_JUICE.get(), ModFluids.IRONBERRY_JUICE.get(),
				ModFluids.HONEY.get(), ModFluids.WILDBERRY_JUICE.get(), ModFluids.GRAPE_JUICE.get(), ModFluids.GOLDEN_APPLE_JUICE.get());
	}

	public static void entityTypeTags(RusticTagProviders.EntityTypes t) {
		t.tagOf(ModTags.EntityTypes.NO_IRON_SKIN_LAYER).add(EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.SHULKER, EntityType.ARMOR_STAND);
	}

	public static void bannerPatternTags(RusticTagProviders.BannerPatterns t) {
		t.tagOf(ModTags.BannerPatterns.ERIS).add(ModBanners.ERIS);
	}

	public static void lootModifiers(RusticLootModifierProvider p) {
	}

	public static void damageTypes(BootstrapContext<DamageType> context) {
		context.register(RusticDamage.BAD_AMBROSIA, new DamageType(RusticRevived.NAMESPACE + ".badAmbrosia", 0.0F));
	}

	public static void bannerPatterns(BootstrapContext<BannerPattern> context) {
		context.register(ModBanners.ERIS, new BannerPattern(RusticRevived.id("eris"), "block." + RusticRevived.NAMESPACE + ".banner.eris"));
	}

	/** Legacy assets/rustic/advancements/main (the almanac recipe advancement is generated with the recipe). */
	public static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
		AdvancementHolder root = Advancement.Builder.advancement()
				.display(ModBlocks.VASE.get(), title("root"), description("root"), RusticRevived.id("block/planks_ironwood"),
						AdvancementType.TASK, false, false, false)
				.addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem())
				.save(output, name("root"));
		Advancement.Builder.advancement().parent(root)
				.display(ModItems.ALMANAC.get(), title("almanac"), description("almanac"), null, AdvancementType.TASK, true, true, false)
				.addCriterion("has_almanac", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ALMANAC.get()))
				.save(output, name("almanac"));
		AdvancementHolder goodStuff = Advancement.Builder.advancement().parent(root)
				.display(ModBlocks.BREWING_BARREL.get(), title("the_good_stuff"), description("the_good_stuff"), null, AdvancementType.TASK, true, true,
						false)
				.addCriterion("has_quality_beverage", InventoryChangeTrigger.TriggerInstance.hasItems(booze(BoozeItemPredicate.minQuality(0.8999D))))
				.save(output, name("the_good_stuff"));
		Advancement.Builder.advancement().parent(goodStuff)
				.display(boozeBottle(ModFluids.WINE), title("winemaxxing"), description("winemaxxing"), null, AdvancementType.CHALLENGE, true, true, false)
				.addCriterion("has_max_quality_wine", InventoryChangeTrigger.TriggerInstance.hasItems(
						booze(BoozeItemPredicate.minQuality(ModFluids.WINE.get(), 0.999D))))
				.save(output, name("winemaxxing"));
		Advancement.Builder.advancement().parent(root)
				.display(boozeBottle(ModFluids.ALE), title("max_drunk"), description("max_drunk"), null, AdvancementType.GOAL, true, true, false)
				.addCriterion("has_max_inebriation", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects()
						.and(ModEffects.TIPSY, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.exactly(3), MinMaxBounds.Ints.ANY,
								Optional.of(false), Optional.empty()))))
				.save(output, name("max_drunk"));
	}

	private static String name(String path) {
		return RusticRevived.id("main/" + path).toString();
	}

	private static Component title(String name) {
		return Component.translatable("advancements." + RusticRevived.NAMESPACE + "." + name);
	}

	private static Component description(String name) {
		return Component.translatable("advancements." + RusticRevived.NAMESPACE + "." + name + ".desc");
	}

	/** Any item matching the {@code rusticrevived:booze} predicate. */
	private static ItemPredicate.Builder booze(BoozeItemPredicate predicate) {
		return ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(ModCriteria.BOOZE, predicate).build());
	}

	/** Fluid bottle of perfect quality booze (advancement icon). */
	private static ItemStackTemplate boozeBottle(ModFluids.FluidEntry booze) {
		// datagen runs before fluid components are bound; fluids have no default components
		Holder.Reference<Fluid> fluid = booze.get().builtInRegistryHolder();
		if (!fluid.areComponentsBound()) {
			fluid.bindComponents(DataComponentMap.EMPTY);
		}
		SimpleFluidContent content = SimpleFluidContent.copyOf(BoozeFluidType.withQuality(new FluidStack(booze.get(), 250), 1.0F));
		return new ItemStackTemplate(ModItems.FLUID_BOTTLE.get(), DataComponentPatch.builder().set(ModDataComponents.FLUID.get(), content).build());
	}
}
