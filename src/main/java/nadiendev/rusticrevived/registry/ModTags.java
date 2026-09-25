package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Fluid;

/**
 * Tags used by Rustic. Common ({@code c:}) tags follow the NeoForge 1.21 conventions.
 */
public final class ModTags {
	private ModTags() {
	}

	public static final class Blocks {
		/** Olive logs (legacy logWood). */
		public static final TagKey<Block> OLIVE_LOGS = rustic("olive_logs");
		public static final TagKey<Block> IRONWOOD_LOGS = rustic("ironwood_logs");
		public static final TagKey<Block> CHAIRS = rustic("chairs");
		public static final TagKey<Block> TABLES = rustic("tables");
		public static final TagKey<Block> PAINTED_WOOD = rustic("painted_wood");
		public static final TagKey<Block> PILLARS = rustic("pillars");
		public static final TagKey<Block> HERBS = rustic("herbs");
		/** Blocks a chandelier / hanging lantern may hang from besides sturdy faces. */
		public static final TagKey<Block> CHANDELIER_SUPPORTS = rustic("chandelier_supports");
		/** Blocks herbs of the "cave" type can be planted on. */
		public static final TagKey<Block> CAVE_HERB_SOIL = rustic("cave_herb_soil");
		/** Blocks herbs of the "desert" type can be planted on. */
		public static final TagKey<Block> DESERT_HERB_SOIL = rustic("desert_herb_soil");
		/** Blocks herbs of the "nether" type can be planted on. */
		public static final TagKey<Block> NETHER_HERB_SOIL = rustic("nether_herb_soil");
		/** Blocks apiary bees will pollinate and age. */
		public static final TagKey<Block> BEE_GROWABLES = rustic("bee_growables");

		public static final TagKey<Block> SLATE_STONES = common("stones/slate");

		private static TagKey<Block> rustic(String name) {
			return TagKey.create(Registries.BLOCK, RusticRevived.id(name));
		}

		private static TagKey<Block> common(String name) {
			return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", name));
		}
	}

	public static final class Items {
		public static final TagKey<Item> OLIVE_LOGS = rustic("olive_logs");
		public static final TagKey<Item> IRONWOOD_LOGS = rustic("ironwood_logs");
		public static final TagKey<Item> CHAIRS = rustic("chairs");
		public static final TagKey<Item> TABLES = rustic("tables");
		public static final TagKey<Item> PAINTED_WOOD = rustic("painted_wood");
		public static final TagKey<Item> PILLARS = rustic("pillars");
		public static final TagKey<Item> HERBS = rustic("herbs");
		/** Beeswax and tallow (legacy "wax" ore dictionary entry). */
		public static final TagKey<Item> WAX = rustic("wax");
		/** Items a cabinet can be made from (legacy plankWood + treated planks). */
		public static final TagKey<Item> CABINET_MATERIALS = rustic("cabinet_materials");
		/** Items that can be coated with vanta oil (legacy "Vanta Oil Whitelist" defaults). */
		public static final TagKey<Item> VANTA_OILABLE = rustic("vanta_oilable");
		/** Foods that can not be olive oiled. */
		public static final TagKey<Item> OLIVE_OIL_BLACKLIST = rustic("olive_oil_blacklist");
		/** Seeds chickens are tempted by and breed with (legacy EntityChicken.TEMPTATION_ITEMS). */
		public static final TagKey<Item> CHICKEN_FOOD = rustic("chicken_food");

		public static final TagKey<Item> CROPS_OLIVE = common("crops/olive");
		public static final TagKey<Item> CROPS_IRONBERRY = common("crops/ironberry");
		public static final TagKey<Item> CROPS_WILDBERRY = common("crops/wildberry");
		public static final TagKey<Item> CROPS_GRAPE = common("crops/grape");
		public static final TagKey<Item> CROPS_TOMATO = common("crops/tomato");
		public static final TagKey<Item> CROPS_CHILI_PEPPER = common("crops/chili_pepper");
		public static final TagKey<Item> FOODS_FRUIT = common("foods/fruit");
		public static final TagKey<Item> FOODS_BERRY = common("foods/berry");
		public static final TagKey<Item> FOODS_VEGETABLE = common("foods/vegetable");
		public static final TagKey<Item> SEEDS_TOMATO = common("seeds/tomato");
		public static final TagKey<Item> SEEDS_CHILI_PEPPER = common("seeds/chili_pepper");
		public static final TagKey<Item> SEEDS_GRAPE = common("seeds/grape");
		public static final TagKey<Item> SEEDS_APPLE = common("seeds/apple");
		public static final TagKey<Item> DUSTS_GOLD = common("dusts/gold");
		public static final TagKey<Item> TINY_DUSTS_IRON = common("tiny_dusts/iron");
		public static final TagKey<Item> INGOTS_SILVER = common("ingots/silver");
		public static final TagKey<Item> SLATE_STONES = common("stones/slate");
		public static final TagKey<Item> MARBLE_STONES = common("stones/marble");
		public static final TagKey<Item> LIMESTONE_STONES = common("stones/limestone");
		public static final TagKey<Item> WAX_COMMON = common("wax");
		public static final TagKey<Item> HONEYCOMBS = common("honeycombs");

		private static TagKey<Item> rustic(String name) {
			return TagKey.create(Registries.ITEM, RusticRevived.id(name));
		}

		private static TagKey<Item> common(String name) {
			return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", name));
		}
	}

	public static final class Fluids {
		public static final TagKey<Fluid> HONEY = common("honey");
		public static final TagKey<Fluid> OLIVE_OIL = common("olive_oil");
		public static final TagKey<Fluid> SEED_OIL = common("seed_oil");
		public static final TagKey<Fluid> APPLE_JUICE = common("apple_juice");
		public static final TagKey<Fluid> GRAPE_JUICE = common("grape_juice");
		public static final TagKey<Fluid> JUICES = common("juices");
		/** Fluids the brewing barrel turns into booze. */
		public static final TagKey<Fluid> BREWABLE = TagKey.create(Registries.FLUID, RusticRevived.id("brewable"));
		/** Every Rustic booze. */
		public static final TagKey<Fluid> BOOZE = TagKey.create(Registries.FLUID, RusticRevived.id("booze"));

		private static TagKey<Fluid> common(String name) {
			return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", name));
		}
	}

	public static final class Biomes {
		public static final TagKey<Biome> HAS_OLIVE_TREES = rustic("has_feature/olive_trees");
		public static final TagKey<Biome> HAS_IRONWOOD_TREES = rustic("has_feature/ironwood_trees");
		public static final TagKey<Biome> HAS_BEEHIVES = rustic("has_feature/beehives");
		public static final TagKey<Biome> HAS_WILDBERRIES = rustic("has_feature/wildberries");
		public static final TagKey<Biome> HAS_SLATE = rustic("has_feature/slate");
		public static final TagKey<Biome> HAS_NETHER_SLATE = rustic("has_feature/nether_slate");
		public static final TagKey<Biome> HAS_CAVE_HERBS = rustic("has_feature/cave_herbs");
		public static final TagKey<Biome> HAS_NETHER_HERBS = rustic("has_feature/nether_herbs");
		public static final TagKey<Biome> HAS_JUNGLE_HERBS = rustic("has_feature/jungle_herbs");
		public static final TagKey<Biome> HAS_DESERT_HERBS = rustic("has_feature/desert_herbs");
		public static final TagKey<Biome> HAS_MOUNTAIN_HERBS = rustic("has_feature/mountain_herbs");
		public static final TagKey<Biome> HAS_SWAMP_HERBS = rustic("has_feature/swamp_herbs");
		public static final TagKey<Biome> HAS_FOREST_HERBS = rustic("has_feature/forest_herbs");
		public static final TagKey<Biome> HAS_PLAINS_HERBS = rustic("has_feature/plains_herbs");

		private static TagKey<Biome> rustic(String name) {
			return TagKey.create(Registries.BIOME, RusticRevived.id(name));
		}
	}

	public static final class EntityTypes {
		/** Living entities the iron skin layer is never drawn on. */
		public static final TagKey<EntityType<?>> NO_IRON_SKIN_LAYER = TagKey.create(Registries.ENTITY_TYPE, RusticRevived.id("no_iron_skin_layer"));
	}

	public static final class BannerPatterns {
		public static final TagKey<BannerPattern> ERIS = TagKey.create(Registries.BANNER_PATTERN, RusticRevived.id("pattern_item/eris"));
	}
}
