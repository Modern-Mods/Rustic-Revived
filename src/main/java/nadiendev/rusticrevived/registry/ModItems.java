package nadiendev.rusticrevived.registry;

import java.util.Random;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.item.AlmanacItem;
import nadiendev.rusticrevived.item.ElixirItem;
import nadiendev.rusticrevived.item.FluidBottleItem;
import nadiendev.rusticrevived.item.LiquidBarrelItem;
import nadiendev.rusticrevived.item.RusticFoodItem;
import nadiendev.rusticrevived.item.StakeCropSeedItem;
import nadiendev.rusticrevived.item.TomatoItem;
import nadiendev.rusticrevived.item.VaseItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every Rustic item. Fluid buckets are registered from {@link ModFluids}.
 */
public final class ModItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RusticRevived.NAMESPACE);

	private static final Random RAND = new Random();

	// ================================================================ materials

	public static final DeferredItem<AlmanacItem> ALMANAC = ITEMS.registerItem("almanac", AlmanacItem::new, () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> BEE = ITEMS.registerSimpleItem("bee");
	public static final DeferredItem<Item> HONEYCOMB = ITEMS.registerSimpleItem("honeycomb");
	public static final DeferredItem<Item> BEESWAX = ITEMS.registerSimpleItem("beeswax");
	public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");
	public static final DeferredItem<Item> TINY_IRON_DUST = ITEMS.registerSimpleItem("tiny_iron_dust");
	public static final DeferredItem<Item> GOLD_DUST = ITEMS.registerSimpleItem("gold_dust");
	/** Applies the Eris symbol in a loom (the pattern comes from the {@code rusticrevived:pattern_item/eris} tag). */
	public static final DeferredItem<Item> ERIS_BANNER_PATTERN = ITEMS.registerItem("eris_banner_pattern", Item::new,
			() -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
					.delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(ModTags.BannerPatterns.ERIS)));

	// ================================================================ alchemy & brewing

	public static final DeferredItem<FluidBottleItem> FLUID_BOTTLE = ITEMS.registerItem("fluid_bottle", FluidBottleItem::new, () -> new Item.Properties().stacksTo(16));
	public static final DeferredItem<ElixirItem> ELIXIR = ITEMS.registerItem("elixir", ElixirItem::new, () -> new Item.Properties().stacksTo(16));

	// ================================================================ food

	public static final DeferredItem<RusticFoodItem> OLIVES = ITEMS.registerItem("olives", p -> new RusticFoodItem(p, null, null),
			() -> food(1, 0.4F, false, 1.2F, new MobEffectInstance(MobEffects.NAUSEA, 200, 1, false, false), 0.95F));
	public static final DeferredItem<RusticFoodItem> IRONBERRIES = ITEMS.registerItem("ironberries", p -> new RusticFoodItem(p, (stack, level, player) -> {
		int duration = 15 * 20;
		MobEffectInstance effect = player.getEffect(ModEffects.FULLMETAL);
		player.addEffect(new MobEffectInstance(ModEffects.FULLMETAL, effect == null ? duration : effect.getDuration() + duration, 0, false, true));
	}, null), () -> food(2, 0.4F, true, 0.8F, null, 0F));
	public static final DeferredItem<TomatoItem> TOMATO = ITEMS.registerItem("tomato", TomatoItem::new, () -> food(4, 0.4F, false, 1.6F, null, 0F));
	public static final DeferredItem<RusticFoodItem> CHILI_PEPPER = ITEMS.registerItem("chili_pepper", p -> new RusticFoodItem(p, (stack, level, player) -> {
		player.addEffect(new MobEffectInstance(MobEffects.SPEED, 400));
		if (RAND.nextInt(24) == 0) {
			player.hurt(level.damageSources().onFire(), 1.0F);
		}
	}, null), () -> food(3, 0.4F, true, 1.6F, null, 0F));
	public static final DeferredItem<RusticFoodItem> GHOST_PEPPER = ITEMS.registerItem("ghost_pepper", p -> new RusticFoodItem(p, (stack, level, player) -> {
		player.hurt(level.damageSources().onFire(), 2.0F);
		player.addEffect(new MobEffectInstance(ModEffects.FIRE_POWER, 1200, 0));
	}, "tooltip.rusticrevived.ghost_pepper"), () -> food(4, 0.7F, true, 1.6F, null, 0F));
	public static final DeferredItem<RusticFoodItem> WILDBERRIES = ITEMS.registerItem("wildberries", p -> new RusticFoodItem(p, null, null),
			() -> food(2, 0.5F, false, 0.8F, null, 0F));
	public static final DeferredItem<RusticFoodItem> GRAPES = ITEMS.registerItem("grapes", p -> new RusticFoodItem(p, null, null),
			() -> food(3, 0.3F, false, 0.8F, null, 0F));

	// ================================================================ seeds

	public static final DeferredItem<StakeCropSeedItem> TOMATO_SEEDS = ITEMS.registerItem("tomato_seeds",
			p -> new StakeCropSeedItem(ModBlocks.TOMATO_CROP.get(), p), Item.Properties::new);
	public static final DeferredItem<StakeCropSeedItem> CHILI_PEPPER_SEEDS = ITEMS.registerItem("chili_pepper_seeds",
			p -> new StakeCropSeedItem(ModBlocks.CHILI_CROP.get(), p), Item.Properties::new);

	// ================================================================ herbs (roots that are planted and eaten)

	public static final DeferredItem<BlockItem> CLOUDSBLUFF = ITEMS.registerItem("cloudsbluff", p -> new BlockItem(ModBlocks.CLOUDSBLUFF.get(), p),
			() -> food(2, 0.2F, true, 1.6F, new MobEffectInstance(MobEffects.LEVITATION, 400), 1.0F).useItemDescriptionPrefix());
	public static final DeferredItem<BlockItem> CORE_ROOT = ITEMS.registerItem("core_root", p -> new BlockItem(ModBlocks.CORE_ROOT.get(), p),
			() -> food(2, 0.3F, false, 1.6F, null, 0F).useItemDescriptionPrefix());
	public static final DeferredItem<BlockItem> GINSENG = ITEMS.registerItem("ginseng", p -> new BlockItem(ModBlocks.GINSENG.get(), p),
			() -> food(2, 0.3F, false, 1.6F, null, 0F).useItemDescriptionPrefix());
	public static final DeferredItem<BlockItem> MARSH_MALLOW = ITEMS.registerItem("marsh_mallow", p -> new BlockItem(ModBlocks.MARSH_MALLOW.get(), p),
			() -> food(3, 0.3F, false, 1.6F, null, 0F).useItemDescriptionPrefix());

	// ================================================================ block items

	static {
		// decoration
		blockItems(ModBlocks.STONE_PILLAR, ModBlocks.ANDESITE_PILLAR, ModBlocks.DIORITE_PILLAR, ModBlocks.GRANITE_PILLAR, ModBlocks.SLATE_PILLAR,
				ModBlocks.BASALT_PILLAR, ModBlocks.LIMESTONE_PILLAR, ModBlocks.MARBLE_PILLAR,
				ModBlocks.SLATE, ModBlocks.SLATE_ROOF, ModBlocks.SLATE_TILE, ModBlocks.SLATE_BRICK, ModBlocks.SLATE_CHISELED, ModBlocks.SLATE_PAVEMENT,
				ModBlocks.SLATE_ROOF_STAIRS, ModBlocks.SLATE_ROOF_SLAB, ModBlocks.SLATE_BRICK_STAIRS, ModBlocks.SLATE_BRICK_SLAB,
				ModBlocks.CLAY_WALL, ModBlocks.CLAY_WALL_CROSS, ModBlocks.CLAY_WALL_DIAG, ModBlocks.GARGOYLE,
				ModBlocks.CHAIN, ModBlocks.CHAIN_GOLD, ModBlocks.CHAIN_SILVER,
				ModBlocks.CANDLE, ModBlocks.CANDLE_GOLD, ModBlocks.CANDLE_SILVER,
				ModBlocks.CANDLE_DOUBLE, ModBlocks.CANDLE_DOUBLE_GOLD, ModBlocks.CANDLE_DOUBLE_SILVER,
				ModBlocks.CANDLE_LEVER, ModBlocks.CANDLE_LEVER_GOLD, ModBlocks.CANDLE_LEVER_SILVER,
				ModBlocks.CHANDELIER, ModBlocks.CHANDELIER_GOLD, ModBlocks.CHANDELIER_SILVER,
				ModBlocks.IRON_TORCH, ModBlocks.IRON_LANTERN, ModBlocks.GOLDEN_LANTERN, ModBlocks.SILVER_LANTERN, ModBlocks.LANTERN_WOOD,
				ModBlocks.IRON_LATTICE, ModBlocks.ROPE);
		ModBlocks.PAINTED_WOOD.values().forEach(ModItems::blockItem);
		ModBlocks.CHAIRS.values().forEach(ModItems::blockItem);
		ModBlocks.TABLES.values().forEach(ModItems::blockItem);
		// woods
		blockItems(ModBlocks.OLIVE_PLANKS, ModBlocks.IRONWOOD_PLANKS, ModBlocks.OLIVE_LOG, ModBlocks.IRONWOOD_LOG,
				ModBlocks.OLIVE_LEAVES, ModBlocks.IRONWOOD_LEAVES, ModBlocks.OLIVE_SAPLING, ModBlocks.IRONWOOD_SAPLING,
				ModBlocks.OLIVE_FENCE, ModBlocks.IRONWOOD_FENCE, ModBlocks.OLIVE_FENCE_GATE, ModBlocks.IRONWOOD_FENCE_GATE,
				ModBlocks.OLIVE_SLAB, ModBlocks.IRONWOOD_SLAB, ModBlocks.OLIVE_STAIRS, ModBlocks.IRONWOOD_STAIRS,
				ModBlocks.CARVED_OLIVE_WOOD, ModBlocks.CARVED_IRONWOOD,
				ModBlocks.APPLE_SEEDS, ModBlocks.APPLE_SAPLING, ModBlocks.APPLE_LEAVES);
		ITEMS.registerItem("olive_door", p -> new DoubleHighBlockItem(ModBlocks.OLIVE_DOOR.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
		ITEMS.registerItem("ironwood_door", p -> new DoubleHighBlockItem(ModBlocks.IRONWOOD_DOOR.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
		// agriculture
		blockItems(ModBlocks.FERTILE_SOIL, ModBlocks.CROP_STAKE, ModBlocks.WILDBERRY_BUSH, ModBlocks.GRAPE_STEM,
				ModBlocks.ALOE_VERA, ModBlocks.BLOOD_ORCHID, ModBlocks.CHAMOMILE, ModBlocks.COHOSH, ModBlocks.DEATHSTALK_MUSHROOM,
				ModBlocks.HORSETAIL, ModBlocks.MOONCAP_MUSHROOM, ModBlocks.WIND_THISTLE, ModBlocks.VANTA_LILY);
		// storage & bees
		ITEMS.registerItem("vase", p -> new VaseItem(ModBlocks.VASE.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
		blockItems(ModBlocks.BARREL, ModBlocks.CABINET, ModBlocks.APIARY, ModBlocks.BEEHIVE);
		// alchemy & brewing
		blockItems(ModBlocks.CONDENSER, ModBlocks.RETORT, ModBlocks.CONDENSER_ADVANCED, ModBlocks.RETORT_ADVANCED,
				ModBlocks.BREWING_BARREL, ModBlocks.CRUSHING_TUB, ModBlocks.EVAPORATING_BASIN);
		ITEMS.registerItem("liquid_barrel", p -> new LiquidBarrelItem(ModBlocks.LIQUID_BARREL.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
	}

	private ModItems() {
	}

	/**
	 * Food properties with a custom eating time and an optional chance based status effect.
	 */
	private static Item.Properties food(int nutrition, float saturation, boolean alwaysEdible, float eatSeconds, MobEffectInstance effect, float chance) {
		FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
		if (alwaysEdible) food.alwaysEdible();
		Consumable.Builder consumable = Consumables.defaultFood().consumeSeconds(eatSeconds);
		if (effect != null) consumable.onConsume(new ApplyStatusEffectsConsumeEffect(effect, chance));
		return new Item.Properties().food(food.build(), consumable.build());
	}

	@SafeVarargs
	private static void blockItems(DeferredBlock<? extends Block>... blocks) {
		for (DeferredBlock<? extends Block> block : blocks) {
			blockItem(block);
		}
	}

	private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> block) {
		return ITEMS.registerSimpleBlockItem(block);
	}
}
