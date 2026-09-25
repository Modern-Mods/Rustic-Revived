package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.Optional;
import java.util.function.Consumer;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.advancement.BoozeItemPredicate;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticBlockStateProvider;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticLootModifierProvider;
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
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Data generation shared by everything: fluids, damage types, banner patterns, entity tags, the
 * Almanac (item model, recipes) and advancements.
 */
public final class MiscData {
	/** Face order of the almanac model elements below. */
	private static final Direction[] BOOK_FACES = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN };

	private MiscData() {
	}

	public static void blockStates(RusticBlockStateProvider p) {
		for (ModFluids.FluidEntry fluid : ModFluids.ALL) {
			if (fluid.block != null) {
				p.getVariantBuilder(fluid.block.get()).partialState().setModels(new ConfiguredModel(
						p.models().getBuilder(fluid.name).texture("particle", fluid.stillTexture())));
			}
			if (fluid.bucket != null) {
				p.itemModels().withExistingParent(fluid.name + "_bucket", ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket"))
						.customLoader(DynamicFluidContainerModelBuilder::begin).fluid(fluid.get()).end();
			}
		}
		p.generatedItem("eris_banner_pattern", "item/eris_banner_pattern");
		p.generatedItem("bee", "item/bee");
		almanacModel(p);
	}

	/**
	 * The Almanac: a small closed book (legacy models/item/book.json). The "Almanac" title that legacy
	 * BookBakedModel rendered with the font is baked into the texture.
	 */
	private static void almanacModel(RusticBlockStateProvider p) {
		ItemModelBuilder model = p.itemModels().getBuilder("almanac")
				.texture("0", RusticRevived.id("item/almanac"))
				.texture("particle", RusticRevived.id("item/almanac"));
		// back cover, pages, spine, front cover: from, to, then per face (north, east, south, west, up, down) u1, v1, u2, v2, rotation
		bookElement(model, new float[] { 4, 0, 2 }, new float[] { 12, 0.5F, 14 }, new float[][] {
				{ 0, 0, 4, 0.5F, 0 }, { 0, 0, 0.5F, 6, 90 }, { 0, 5.5F, 4, 6, 0 }, { 3.5F, 0, 4, 6, 270 }, { 4, 0, 0, 6, 0 }, { 4, 6, 0, 0, 0 } });
		bookElement(model, new float[] { 4, 0.5F, 2.5F }, new float[] { 11.5F, 3.5F, 13.5F }, new float[][] {
				{ 0, 11.5F, 4, 16, 0 }, { 4, 6, 8.5F, 11.5F, 90 }, { 0, 11.5F, 4, 16, 0 }, { 4, 6, 8.5F, 11.5F, 270 }, { 0, 6, 4, 11.5F, 0 },
				{ 0, 6, 4, 11.5F, 0 } });
		bookElement(model, new float[] { 3.5F, 0.5F, 2 }, new float[] { 4, 3.5F, 14 }, new float[][] {
				{ 4, 0, 5.5F, 0.5F, 90 }, { 4, 0, 5.5F, 6, 90 }, { 4, 5.5F, 5.5F, 6, 90 }, { 4, 0, 5.5F, 6, 270 }, { 5, 0, 5.5F, 6, 0 },
				{ 4, 0, 4.5F, 6, 0 } });
		bookElement(model, new float[] { 4, 3.5F, 2 }, new float[] { 12, 4, 14 }, new float[][] {
				{ 5.5F, 0, 9.5F, 0.5F, 0 }, { 9, 0, 9.5F, 6, 90 }, { 5.5F, 5.5F, 9.5F, 6, 0 }, { 5.5F, 0, 6, 6, 270 }, { 5.5F, 0, 9.5F, 6, 0 },
				{ 5.5F, 6, 9.5F, 0, 0 } });
		model.transforms()
				.transform(ItemDisplayContext.GUI).rotation(45, -55, 0).translation(0, 4.2F, 0).scale(1).end()
				.transform(ItemDisplayContext.GROUND).rotation(0, 0, 0).translation(0, 2, 0).scale(0.5F).end()
				.transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, 13, 7).scale(1).end()
				.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(-2, 3, 4.5F).scale(0.55F).end()
				.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(70, -10, 65).translation(-2.26F, 4.7F, 1.13F).scale(0.68F).end()
				.transform(ItemDisplayContext.FIXED).rotation(90, 0, 180).translation(-0.25F, 0, -7).scale(1).end()
				.end();
	}

	private static void bookElement(ItemModelBuilder model, float[] from, float[] to, float[][] faces) {
		var element = model.element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2]);
		for (int i = 0; i < BOOK_FACES.length; i++) {
			float[] face = faces[i];
			ModelBuilder.FaceRotation rotation = switch ((int) face[4]) {
				case 90 -> ModelBuilder.FaceRotation.CLOCKWISE_90;
				case 270 -> ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90;
				default -> ModelBuilder.FaceRotation.ZERO;
			};
			element.face(BOOK_FACES[i]).texture("#0").uvs(face[0], face[1], face[2], face[3]).rotation(rotation).end();
		}
		element.end();
	}

	public static void recipes(RusticRecipeProvider p, RecipeOutput out) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ALMANAC.get())
				.pattern(" O ")
				.pattern("IBI")
				.pattern(" I ")
				.define('O', ModItems.OLIVES.get())
				.define('I', Tags.Items.NUGGETS_IRON)
				.define('B', Items.BOOK)
				.unlockedBy(RusticRecipeProvider.hasName(ModItems.OLIVES.get()), RusticRecipeProvider.hasItem(ModItems.OLIVES.get()))
				.unlockedBy(RusticRecipeProvider.hasName(Items.BOOK), RusticRecipeProvider.hasItem(Items.BOOK))
				.save(out, RusticRevived.id("almanac"));
		// legacy applied the Eris symbol with the almanac itself; the almanac is given back (AlmanacItem remainder)
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ERIS_BANNER_PATTERN.get())
				.requires(Items.PAPER)
				.requires(ModItems.ALMANAC.get())
				.unlockedBy(RusticRecipeProvider.hasName(ModItems.ALMANAC.get()), RusticRecipeProvider.hasItem(ModItems.ALMANAC.get()))
				.save(out, RusticRevived.id("eris_banner_pattern"));
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

	public static void dataMaps(RusticDataMapProvider p) {
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
	public static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
		AdvancementHolder root = Advancement.Builder.advancement()
				.display(ModBlocks.VASE.get(), title("root"), description("root"), RusticRevived.id("textures/block/planks_ironwood.png"),
						AdvancementType.TASK, false, false, false)
				.addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem())
				.save(saver, RusticRevived.id("main/root"), helper);
		Advancement.Builder.advancement().parent(root)
				.display(ModItems.ALMANAC.get(), title("almanac"), description("almanac"), null, AdvancementType.TASK, true, true, false)
				.addCriterion("has_almanac", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ALMANAC.get()))
				.save(saver, RusticRevived.id("main/almanac"), helper);
		AdvancementHolder goodStuff = Advancement.Builder.advancement().parent(root)
				.display(ModBlocks.BREWING_BARREL.get(), title("the_good_stuff"), description("the_good_stuff"), null, AdvancementType.TASK, true, true,
						false)
				.addCriterion("has_quality_beverage", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()
						.withSubPredicate(ModCriteria.BOOZE, BoozeItemPredicate.minQuality(0.8999D))))
				.save(saver, RusticRevived.id("main/the_good_stuff"), helper);
		Advancement.Builder.advancement().parent(goodStuff)
				.display(boozeBottle(ModFluids.WINE), title("winemaxxing"), description("winemaxxing"), null, AdvancementType.CHALLENGE, true, true, false)
				.addCriterion("has_max_quality_wine", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()
						.withSubPredicate(ModCriteria.BOOZE, BoozeItemPredicate.minQuality(ModFluids.WINE.get(), 0.999D))))
				.save(saver, RusticRevived.id("main/winemaxxing"), helper);
		Advancement.Builder.advancement().parent(root)
				.display(boozeBottle(ModFluids.ALE), title("max_drunk"), description("max_drunk"), null, AdvancementType.GOAL, true, true, false)
				.addCriterion("has_max_inebriation", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects()
						.and(ModEffects.TIPSY, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.exactly(3), MinMaxBounds.Ints.ANY,
								Optional.of(false), Optional.empty()))))
				.save(saver, RusticRevived.id("main/max_drunk"), helper);
	}

	private static Component title(String name) {
		return Component.translatable("advancements." + RusticRevived.NAMESPACE + "." + name);
	}

	private static Component description(String name) {
		return Component.translatable("advancements." + RusticRevived.NAMESPACE + "." + name + ".desc");
	}

	/** Fluid bottle of perfect quality booze (advancement icon). */
	private static ItemStack boozeBottle(ModFluids.FluidEntry booze) {
		ItemStack bottle = new ItemStack(ModItems.FLUID_BOTTLE.get());
		bottle.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(BoozeFluidType.withQuality(new FluidStack(booze.get(), 250), 1.0F)));
		return bottle;
	}
}
