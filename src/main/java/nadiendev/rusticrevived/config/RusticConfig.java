package nadiendev.rusticrevived.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Rustic configuration. Everything that used to live in rustic.cfg.
 * <p>
 * Block toggles ("Enable Slate", ...) no longer remove the blocks from the registry (1.21 registries
 * are frozen before configs load); instead they hide the blocks from the creative tabs and disable
 * their recipes through the {@code rustic:config} recipe condition.
 */
public final class RusticConfig {

	public static final ModConfigSpec COMMON_SPEC;
	public static final Common COMMON;
	public static final ModConfigSpec CLIENT_SPEC;
	public static final Client CLIENT;

	static {
		ModConfigSpec.Builder common = new ModConfigSpec.Builder();
		COMMON = new Common(common);
		COMMON_SPEC = common.build();
		ModConfigSpec.Builder client = new ModConfigSpec.Builder();
		CLIENT = new Client(client);
		CLIENT_SPEC = client.build();
	}

	private RusticConfig() {
	}

	public static final class Common {
		// general
		public final ModConfigSpec.BooleanValue fleshSmelting;
		public final ModConfigSpec.BooleanValue enableOliveOiling;
		public final ModConfigSpec.ConfigValue<List<? extends String>> oliveOilBlacklist;
		public final ModConfigSpec.BooleanValue oliveOilUseWhitelist;
		public final ModConfigSpec.BooleanValue enableSeedDrops;
		public final ModConfigSpec.IntValue seedDropRate;
		public final ModConfigSpec.BooleanValue grapeDropNeedsTool;
		public final ModConfigSpec.ConfigValue<List<? extends String>> grapeToolWhitelist;
		public final ModConfigSpec.BooleanValue enableBottleEmptying;
		public final ModConfigSpec.IntValue minBrewQualityChange;
		public final ModConfigSpec.IntValue maxBrewQualityChange;
		public final ModConfigSpec.IntValue maxBrewTime;
		public final ModConfigSpec.ConfigValue<List<? extends String>> vantaOilWhitelist;
		public final ModConfigSpec.IntValue maxWildberryWineAmplifier;
		public final ModConfigSpec.ConfigValue<List<? extends String>> wildberryAmplifierOverrides;
		public final ModConfigSpec.BooleanValue enableSlate;
		public final ModConfigSpec.BooleanValue enablePillars;
		public final ModConfigSpec.BooleanValue enableClayWalls;
		public final ModConfigSpec.BooleanValue enablePaintedWood;
		public final ModConfigSpec.BooleanValue enableTables;
		public final ModConfigSpec.BooleanValue enableChairs;
		public final ModConfigSpec.BooleanValue enableLattice;

		// bees
		public final ModConfigSpec.DoubleValue beehiveGenChance;
		public final ModConfigSpec.IntValue maxBeehiveAttempts;
		public final ModConfigSpec.DoubleValue beeGrowthMultiplier;
		public final ModConfigSpec.DoubleValue beeReproductionMultiplier;
		public final ModConfigSpec.DoubleValue beeHoneycombMultiplier;

		// world
		public final ModConfigSpec.BooleanValue netherSlate;
		public final ModConfigSpec.IntValue slateVeinsPerChunk;
		public final ModConfigSpec.IntValue slateVeinSize;
		public final ModConfigSpec.DoubleValue oliveGenChance;
		public final ModConfigSpec.IntValue maxOliveGenAttempts;
		public final ModConfigSpec.DoubleValue ironwoodGenChance;
		public final ModConfigSpec.IntValue maxIronwoodGenAttempts;
		public final ModConfigSpec.DoubleValue herbGenChance;
		public final ModConfigSpec.IntValue maxHerbAttempts;
		public final ModConfigSpec.DoubleValue wildberryGenChance;
		public final ModConfigSpec.IntValue maxWildberryAttempts;

		// compat
		public final ModConfigSpec.BooleanValue enableSilverDecor;
		public final ModConfigSpec.BooleanValue enableDynamicTreesCompat;

		Common(ModConfigSpec.Builder b) {
			b.comment("General Options").push("general");
			fleshSmelting = b.comment("enable smelting rotten flesh into tallow")
					.define("fleshSmelting", true);
			enableOliveOiling = b.comment("enable/disable the ability to add olive oil to food")
					.define("enableOliveOiling", true);
			oliveOilBlacklist = b.comment("add an item's registry name to this list to prevent it from being craftable with olive oil")
					.defineListAllowEmpty("oliveOilFoodBlacklist", List.of(), () -> "", RusticConfig::isString);
			oliveOilUseWhitelist = b.comment("treat \"oliveOilFoodBlacklist\" as a whitelist instead")
					.define("oliveOilUseWhitelist", false);
			enableSeedDrops = b.comment("set this to false to prevent any of Rustic's seeds from dropping from grass or vines")
					.define("enableSeedDrops", true);
			seedDropRate = b.comment("decrease this number to make seeds more difficult to find (10 is wheat seed rarity)")
					.defineInRange("seedDropRate", 7, 1, 100);
			grapeDropNeedsTool = b.comment("with this value set to true, vines will only drop grape seeds when broken with tools from the whitelist")
					.define("grapeDropNeedsTool", false);
			grapeToolWhitelist = b.comment("items (or #tags) allowed to make vines drop grape seeds when grapeDropNeedsTool is enabled")
					.defineListAllowEmpty("grapeToolWhitelist", List.of("minecraft:iron_hoe", "minecraft:diamond_hoe", "minecraft:netherite_hoe"), () -> "", RusticConfig::isString);
			enableBottleEmptying = b.comment("set this to false if you experience any issues with Rustic's glass bottle emptying recipe")
					.define("enableBottleEmptying", true);
			minBrewQualityChange = b.comment("the minimum amount of increase that booze culture will provide to the new brew, in percent")
					.defineInRange("minBrewQualityChange", -1, -50, 50);
			maxBrewQualityChange = b.comment("the maximum amount of increase that booze culture will provide to the new brew, in percent")
					.defineInRange("maxBrewQualityChange", 4, -50, 50);
			maxBrewTime = b.comment("how long it should take for a brewing barrel to finish a brew, in ticks")
					.defineInRange("brewingTime", 12000, 1200, 120000);
			vantaOilWhitelist = b.comment("items (or #tags) that can be coated with vanta oil and elixirs")
					.defineListAllowEmpty("vantaOilWhitelist", List.of("#minecraft:swords", "#minecraft:axes", "minecraft:stick", "minecraft:bone"), () -> "", RusticConfig::isString);
			maxWildberryWineAmplifier = b.comment("the maximum amplifier (0 = level I) wildberry wine will raise beneficial effects to")
					.defineInRange("maxWildberryWineAmplifier", 2, 0, 100);
			wildberryAmplifierOverrides = b.comment("per-effect overrides of maxWildberryWineAmplifier, written as <effect id>=<max amplifier>, e.g. \"minecraft:speed=4\"")
					.defineListAllowEmpty("wildberryAmplifierOverrides", List.of(), () -> "", RusticConfig::isAmplifierOverride);
			enableSlate = b.comment("enable/disable all slate blocks and world gen").define("enableSlate", true);
			enablePillars = b.comment("enable/disable all stone pillar blocks").define("enablePillars", true);
			enableClayWalls = b.comment("enable/disable all clay wall blocks").define("enableClayWalls", true);
			enablePaintedWood = b.comment("enable/disable all painted wood blocks").define("enablePaintedWood", true);
			enableTables = b.comment("enable/disable all table blocks").define("enableTables", true);
			enableChairs = b.comment("enable/disable all chair blocks").define("enableChairs", true);
			enableLattice = b.comment("enable/disable lattice blocks").define("enableLattice", true);
			b.pop();

			b.comment("Bee Related Options").push("bees");
			beehiveGenChance = b.comment("chance for beehives to try to generate in a chunk")
					.defineInRange("beehiveGenChance", 0.03D, 0D, 1D);
			maxBeehiveAttempts = b.comment("maximum number of times the generator will attempt to place a beehive in a chunk")
					.defineInRange("maxBeehiveAttempts", 3, 0, 128);
			beeGrowthMultiplier = b.comment("higher values increase the frequency with which apiaries forcibly age a crop")
					.defineInRange("beeCropBoostMultiplier", 1D, 0D, 10D);
			beeReproductionMultiplier = b.comment("the time it takes for an apiary to produce a new bee is multiplied by this value", "LARGER numbers make bees reproduce LESS often")
					.defineInRange("beeReproductionMultiplier", 1D, 0D, 10D);
			beeHoneycombMultiplier = b.comment("the time it takes for an apiary to produce a honeycomb is multiplied by this value", "LARGER numbers make bees produce honeycomb LESS often")
					.defineInRange("beeHoneycombMultiplier", 1D, 0D, 10D);
			b.pop();

			b.comment("World Generation Options").push("world");
			netherSlate = b.comment("if set to true, slate will generate in the nether instead of the overworld")
					.define("netherSlate", false);
			slateVeinsPerChunk = b.comment("number of times the generator will try to place a slate vein per chunk")
					.defineInRange("slateVeinsPerChunk", 5, 0, 30);
			slateVeinSize = b.comment("number of blocks per slate vein (applied through the placed feature count; vein shape is fixed by the configured feature)")
					.defineInRange("slateVeinSize", 20, 0, 25);
			oliveGenChance = b.comment("chance for olive trees to try to generate in a chunk")
					.defineInRange("oliveGenChance", 0.03D, 0D, 1D);
			maxOliveGenAttempts = b.comment("maximum number of times the generator will attempt to place an olive tree in a chunk")
					.defineInRange("maxOliveGenAttempts", 5, 0, 128);
			ironwoodGenChance = b.comment("chance for ironwood trees to try to generate in a chunk")
					.defineInRange("ironwoodGenChance", 0.015D, 0D, 1D);
			maxIronwoodGenAttempts = b.comment("maximum number of times the generator will attempt to place an ironwood tree in a chunk")
					.defineInRange("maxIronwoodGenAttempts", 4, 0, 128);
			herbGenChance = b.comment("chance for an herb to try to generate in a chunk")
					.defineInRange("herbGenChance", 0.125D, 0D, 1D);
			maxHerbAttempts = b.comment("maximum number of times the generator will attempt to place an herb in a chunk")
					.defineInRange("maxHerbAttempts", 8, 0, 128);
			wildberryGenChance = b.comment("chance for wildberry bushes to try to generate in a chunk")
					.defineInRange("wildberryGenChance", 0.05D, 0D, 1D);
			maxWildberryAttempts = b.comment("maximum number of times the generator will attempt to place a wildberry bush in a chunk")
					.defineInRange("maxWildberryAttempts", 4, 0, 128);
			b.pop();

			b.comment("Mod Compatibility Related Options").push("compat");
			enableSilverDecor = b.comment("set this to false to disable silver chain, chandelier, candle, and lantern blocks")
					.define("enableSilverDecor", true);
			enableDynamicTreesCompat = b.comment("when Dynamic Trees is installed, generate dynamic olive and ironwood trees instead of the static ones")
					.define("enableDynamicTreesCompat", true);
			b.pop();
		}

		private Map<Identifier, Integer> amplifierCache;
		private List<? extends String> amplifierCacheSource;

		/**
		 * Maximum amplifier wildberry wine may raise the given effect to (PR #265 + #267).
		 */
		public int getWildberryMaxAmplifier(Identifier effectId) {
			List<? extends String> raw = wildberryAmplifierOverrides.get();
			if (amplifierCache == null || amplifierCacheSource != raw) {
				Map<Identifier, Integer> map = new HashMap<>();
				for (String entry : raw) {
					String[] split = entry.split("=");
					if (split.length != 2) continue;
					Identifier id = Identifier.tryParse(split[0].trim());
					if (id == null) continue;
					try {
						map.put(id, Integer.parseInt(split[1].trim()));
					} catch (NumberFormatException ignored) {
					}
				}
				amplifierCache = map;
				amplifierCacheSource = raw;
			}
			return amplifierCache.getOrDefault(effectId, maxWildberryWineAmplifier.get());
		}
	}

	public static final class Client {
		public final ModConfigSpec.BooleanValue toughnessHud;
		public final ModConfigSpec.BooleanValue extraArmorHud;
		public final ModConfigSpec.ConfigValue<List<? extends String>> ironSkinRenderBlacklist;
		public final ModConfigSpec.BooleanValue offsetWildberryBushes;

		Client(ModConfigSpec.Builder b) {
			b.push("client");
			extraArmorHud = b.comment("if enabled, allows the armor meter to go beyond one row",
					"only one extra row will ever be rendered, because the armor stat is naturally capped at 30")
					.define("extraArmorHud", true);
			toughnessHud = b.comment("if enabled, adds a hud element over the hunger meter to show armor toughness, if applicable")
					.define("armorToughnessHud", true);
			ironSkinRenderBlacklist = b.comment("entity ids for which the iron skin visual effect is disabled")
					.defineListAllowEmpty("ironSkinRenderBlacklist", List.of("minecraft:slime", "minecraft:shulker", "minecraft:magma_cube"), () -> "", RusticConfig::isString);
			offsetWildberryBushes = b.comment("enable/disable the random offset added to wildberry bush models (requires resource reload)")
					.define("offsetWildberryBushes", true);
			b.pop();
		}
	}

	private static boolean isString(Object o) {
		return o instanceof String;
	}

	private static boolean isAmplifierOverride(Object o) {
		if (!(o instanceof String s)) return false;
		String[] split = s.split("=");
		if (split.length != 2 || Identifier.tryParse(split[0].trim()) == null) return false;
		try {
			Integer.parseInt(split[1].trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
