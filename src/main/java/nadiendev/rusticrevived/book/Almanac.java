package nadiendev.rusticrevived.book;

import java.util.List;
import java.util.function.Supplier;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.WoodVariant;
import nadiendev.rusticrevived.book.pages.CategoriesPage;
import nadiendev.rusticrevived.book.pages.TextPage;
import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * The contents of the Almanac (legacy BookManager): categories, entries and their pages. Built each
 * time the book is opened so the entries follow the current config (disabled blocks have no entry).
 */
public final class Almanac {
	/** Fluid held by the bottles shown as entry icons. */
	private static final int BOTTLE_AMOUNT = 250;

	private final List<BookCategory> categories;
	private final BookEntry index;

	private Almanac(List<BookCategory> categories, BookEntry index) {
		this.categories = categories;
		this.index = index;
	}

	public List<BookCategory> getCategories() {
		return categories;
	}

	/** The first entry of the book, listing the categories. */
	public BookEntry getIndex() {
		return index;
	}

	public static Almanac create() {
		BookCategory decoration = new BookCategory("decoration", icon("anim_decoration"));
		BookCategory agriculture = new BookCategory("agriculture", icon("anim_agriculture"));
		BookCategory production = new BookCategory("production", icon("anim_production"));
		List<BookCategory> categories = List.of(decoration, agriculture, production);
		BookEntry index = new BookEntry("categories", null);
		index.addPage(new CategoriesPage(index, categories));

		BookEntry pots = entry("pots", decoration, ModBlocks.VASE);
		BookEntry barrels = entry("barrels", decoration, ModBlocks.BARREL);
		BookEntry cabinets = entry("cabinets", decoration, ModBlocks.CABINET);
		BookEntry rope = entry("rope", decoration, ModBlocks.ROPE);
		BookEntry chain = entry("chain", decoration, ModBlocks.CHAIN);
		BookEntry candles = entry("candles", decoration, ModBlocks.CANDLE);
		BookEntry chandeliers = entry("chandeliers", decoration, ModBlocks.CHANDELIER);
		BookEntry lanterns = entry("lanterns", decoration, ModBlocks.IRON_LANTERN);
		BookEntry lattice = option(RusticConfig.COMMON.enableLattice, true) ? entry("lattice", decoration, ModBlocks.IRON_LATTICE) : null;
		BookEntry tables = option(RusticConfig.COMMON.enableTables, true) ? entry("tables", decoration, ModBlocks.TABLES.get(WoodVariant.OAK)) : null;
		BookEntry chairs = option(RusticConfig.COMMON.enableChairs, true) ? entry("chairs", decoration, ModBlocks.CHAIRS.get(WoodVariant.OAK)) : null;
		BookEntry gargoyles = entry("gargoyles", decoration, ModBlocks.GARGOYLE);
		BookEntry slate = option(RusticConfig.COMMON.enableSlate, true) ? entry("slate", decoration, ModBlocks.SLATE) : null;
		BookEntry pillars = option(RusticConfig.COMMON.enablePillars, true) ? entry("pillars", decoration, ModBlocks.STONE_PILLAR) : null;
		BookEntry clayWalls = option(RusticConfig.COMMON.enableClayWalls, true) ? entry("clay_walls", decoration, ModBlocks.CLAY_WALL) : null;
		BookEntry paintedWood = option(RusticConfig.COMMON.enablePaintedWood, true)
				? entry("painted_wood", decoration, ModBlocks.PAINTED_WOOD.get(DyeColor.RED)) : null;

		BookEntry bees = entry("bees", agriculture, ModItems.BEE);
		BookEntry fertileSoil = entry("fertile_soil", agriculture, ModBlocks.FERTILE_SOIL);
		BookEntry cropStakes = entry("crop_stakes", agriculture, ModBlocks.CROP_STAKE);
		BookEntry tomatoes = entry("tomatoes", agriculture, ModItems.TOMATO);
		BookEntry chiliPeppers = entry("chili_peppers", agriculture, ModItems.CHILI_PEPPER);
		BookEntry wildberries = entry("wildberries", agriculture, ModItems.WILDBERRIES);
		BookEntry grapes = entry("grapes", agriculture, ModItems.GRAPES);
		BookEntry appleTrees = entry("apple_trees", agriculture, () -> Items.APPLE);
		BookEntry oliveTrees = entry("olive_trees", agriculture, ModItems.OLIVES);
		BookEntry ironwoodTrees = entry("ironwood_trees", agriculture, ModItems.IRONBERRIES);
		BookEntry herbs = entry("herbs", agriculture, ModItems.MARSH_MALLOW);

		BookEntry alchemy = entry("alchemy", production, ModBlocks.CONDENSER);
		BookEntry elixirs = entry("elixirs", production, ModItems.ELIXIR);
		BookEntry vantaOil = new BookEntry("vanta_oil", production).setIcon(filledBottle(ModFluids.VANTA_OIL));
		BookEntry brewing = entry("brewing", production, ModBlocks.BREWING_BARREL);
		BookEntry alcoholicBeverages = new BookEntry("alcoholic_beverages", production).setIcon(filledBottle(ModFluids.WINE));
		BookEntry crushing = entry("crushing", production, ModBlocks.CRUSHING_TUB);
		BookEntry drying = entry("drying", production, ModBlocks.EVAPORATING_BASIN);

		text(pots, "pots");
		text(barrels, "barrel_item", "barrel_fluid");
		text(cabinets, "cabinets");
		text(rope, "rope").related(cropStakes, grapes, chain, chandeliers);
		text(rope, "rope_1").related(cropStakes, grapes, chain, chandeliers);
		text(chain, "chain").related(rope, chandeliers, lattice);
		text(candles, "candles").related(bees, chandeliers);
		text(chandeliers, "chandeliers").related(candles, rope, chain);
		text(lanterns, "lanterns").related(chain, candles, lattice);
		if (lattice != null) {
			text(lattice, "lattice").related(chain, lanterns);
		}
		if (tables != null) {
			text(tables, "tables").related(chairs);
		}
		if (chairs != null) {
			text(chairs, "chairs").related(tables);
		}
		text(gargoyles, "gargoyles");
		if (slate != null) {
			text(slate, option(RusticConfig.COMMON.netherSlate, false) ? "slate_alt_nether" : "slate").related(pillars);
		}
		if (pillars != null) {
			text(pillars, "pillars").related(slate);
		}
		if (clayWalls != null) {
			text(clayWalls, "clay_walls");
		}
		if (paintedWood != null) {
			text(paintedWood, "painted_wood");
		}

		BookEntry[] beeLinks = { candles, crushing, alchemy, brewing };
		text(bees, "bees").related(beeLinks);
		text(bees, "apiaries").related(beeLinks);
		text(bees, "honeycomb", "honey").related(beeLinks);
		text(fertileSoil, "fertile_soil");
		text(cropStakes, "crop_stakes").related(tomatoes, chiliPeppers, rope);
		text(tomatoes, "tomatoes").related(cropStakes);
		text(tomatoes, "tomatoes_1").related(cropStakes);
		for (String key : new String[] { "chili_peppers", "chili_peppers_1", "chili_peppers_2" }) {
			text(chiliPeppers, key).related(cropStakes, alchemy);
		}
		for (String key : new String[] { "wildberries", "wildberries_1", "wildberry_juice" }) {
			text(wildberries, key).related(crushing, brewing);
		}
		for (String key : new String[] { "grapes", "grapes_1", "grape_juice" }) {
			text(grapes, key).related(rope, crushing, brewing);
		}
		for (String key : new String[] { "apple_trees", "apple_trees_1", "apple_juice" }) {
			text(appleTrees, key).related(crushing, brewing);
		}
		text(oliveTrees, "olive_trees").related(crushing);
		text(oliveTrees, "olive_oil").related(crushing);
		text(ironwoodTrees, "ironwood_trees").related(crushing, drying, alchemy, brewing);
		text(ironwoodTrees, "ironberry_juice").related(crushing, drying, alchemy, brewing);
		text(herbs, "herbs").related(alchemy);
		text(herbs, "aloe", "blood_orchid", "chamomile", "cloudsbluff", "cohosh").related(alchemy);
		text(herbs, "core_root", "deathstalk", "ginseng", "horsetail", "marsh_mallow", "mooncap").related(alchemy);
		text(herbs, "wind_thistle", "vanta_lily").related(alchemy, crushing, vantaOil);

		text(elixirs, "elixirs").related(alchemy, vantaOil);
		for (String key : new String[] { "alchemy", "alchemy_basic", "alchemy_basic_1", "alchemy_basic_2", "alchemy_advanced", "alchemy_advanced_1",
				"alchemy_advanced_2" }) {
			text(alchemy, key).related(elixirs, herbs, bees, chiliPeppers, ironwoodTrees);
		}
		text(vantaOil, "vanta_oil_weapons").related(herbs, crushing, elixirs);
		text(vantaOil, "vanta_oil_weapons_1").related(herbs, crushing, elixirs);
		BookEntry[] boozeLinks = { brewing, bees, ironwoodTrees, appleTrees, grapes, wildberries };
		text(alcoholicBeverages, "alcoholic_beverages").related(boozeLinks);
		text(alcoholicBeverages, "alcoholic_beverages_1").related(boozeLinks);
		text(alcoholicBeverages, "alcoholic_beverages_2").related(boozeLinks);
		text(alcoholicBeverages, "ale", "ale_wort").related(boozeLinks);
		for (String key : new String[] { "cider", "iron_wine", "mead", "wildberry_wine", "wine", "ambrosia" }) {
			text(alcoholicBeverages, key).related(boozeLinks);
		}
		BookEntry[] brewingLinks = { alcoholicBeverages, bees, ironwoodTrees, appleTrees, grapes, wildberries };
		text(brewing, "brewing").related(brewingLinks);
		text(brewing, "brewing_1").related(brewingLinks);
		text(brewing, "brewing_2", "brewing_3").related(brewingLinks);
		for (String key : new String[] { "brewing_4", "brewing_5", "brewing_6" }) {
			text(brewing, key).related(brewingLinks);
		}
		BookEntry[] crushingLinks = { bees, grapes, appleTrees, wildberries, oliveTrees, ironwoodTrees };
		text(crushing, "crushing").related(crushingLinks);
		text(crushing, "crushing_1").related(crushingLinks);
		text(drying, "drying").related(ironwoodTrees);
		text(drying, "drying_1").related(ironwoodTrees);

		return new Almanac(categories, index);
	}

	private static ResourceLocation icon(String name) {
		return RusticRevived.id("textures/gui/book/" + name + ".png");
	}

	private static BookEntry entry(String name, BookCategory category, Supplier<? extends ItemLike> icon) {
		return new BookEntry(name, category).setIcon(new ItemStack(icon.get()));
	}

	private static TextPage text(BookEntry entry, String... textKeys) {
		TextPage page = new TextPage(entry, textKeys);
		entry.addPage(page);
		return page;
	}

	private static ItemStack filledBottle(ModFluids.FluidEntry fluid) {
		ItemStack bottle = new ItemStack(ModItems.FLUID_BOTTLE.get());
		bottle.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(new FluidStack(fluid.get(), BOTTLE_AMOUNT)));
		return bottle;
	}

	/** Reads a boolean config option, tolerating an unloaded config. */
	private static boolean option(Supplier<Boolean> option, boolean fallback) {
		try {
			return option.get();
		} catch (IllegalStateException e) {
			return fallback;
		}
	}
}
