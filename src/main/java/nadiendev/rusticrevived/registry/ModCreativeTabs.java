package nadiendev.rusticrevived.registry;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.config.RusticConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The three legacy creative tabs. Blocks disabled in the config are hidden.
 */
public final class ModCreativeTabs {
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RusticRevived.NAMESPACE);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DECOR = TABS.register("decor", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.rusticrevived.decor"))
			.icon(() -> new ItemStack(ModBlocks.VASE.get()))
			.displayItems((params, out) -> {
				Output o = new Output(out);
				o.add(ModConfigFlags::pillars, ModBlocks.STONE_PILLAR, ModBlocks.ANDESITE_PILLAR, ModBlocks.DIORITE_PILLAR, ModBlocks.GRANITE_PILLAR,
						ModBlocks.BASALT_PILLAR, ModBlocks.LIMESTONE_PILLAR, ModBlocks.MARBLE_PILLAR);
				o.add(() -> ModConfigFlags.slate() && ModConfigFlags.pillars(), ModBlocks.SLATE_PILLAR);
				o.add(ModConfigFlags::slate, ModBlocks.SLATE, ModBlocks.SLATE_ROOF, ModBlocks.SLATE_ROOF_STAIRS, ModBlocks.SLATE_ROOF_SLAB,
						ModBlocks.SLATE_TILE, ModBlocks.SLATE_BRICK, ModBlocks.SLATE_BRICK_STAIRS, ModBlocks.SLATE_BRICK_SLAB,
						ModBlocks.SLATE_CHISELED, ModBlocks.SLATE_PAVEMENT);
				o.add(ModConfigFlags::clayWalls, ModBlocks.CLAY_WALL, ModBlocks.CLAY_WALL_CROSS, ModBlocks.CLAY_WALL_DIAG);
				o.add(ModConfigFlags::paintedWood, ModBlocks.PAINTED_WOOD.values().toArray(Supplier[]::new));
				o.add(ModConfigFlags::chairs, ModBlocks.CHAIRS.values().toArray(Supplier[]::new));
				o.add(ModConfigFlags::tables, ModBlocks.TABLES.values().toArray(Supplier[]::new));
				o.add(() -> true, ModBlocks.VASE, ModBlocks.BARREL, ModBlocks.CABINET, ModBlocks.GARGOYLE, ModBlocks.ROPE,
						ModBlocks.CHAIN, ModBlocks.CANDLE, ModBlocks.CANDLE_DOUBLE, ModBlocks.CANDLE_LEVER, ModBlocks.CHANDELIER, ModBlocks.IRON_LANTERN,
						ModBlocks.CHAIN_GOLD, ModBlocks.CANDLE_GOLD, ModBlocks.CANDLE_DOUBLE_GOLD, ModBlocks.CANDLE_LEVER_GOLD, ModBlocks.CHANDELIER_GOLD,
						ModBlocks.GOLDEN_LANTERN);
				o.add(ModConfigFlags::silver, ModBlocks.CHAIN_SILVER, ModBlocks.CANDLE_SILVER, ModBlocks.CANDLE_DOUBLE_SILVER, ModBlocks.CANDLE_LEVER_SILVER,
						ModBlocks.CHANDELIER_SILVER, ModBlocks.SILVER_LANTERN);
				o.add(() -> true, ModBlocks.LANTERN_WOOD, ModBlocks.IRON_TORCH);
				o.add(ModConfigFlags::lattice, ModBlocks.IRON_LATTICE);
				o.add(() -> true, ModItems.ALMANAC, ModItems.ERIS_BANNER_PATTERN);
			}).build());

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FARMING = TABS.register("farming", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.rusticrevived.farming"))
			.icon(() -> new ItemStack(ModItems.OLIVES.get()))
			.withTabsBefore(DECOR.getId())
			.displayItems((params, out) -> {
				Output o = new Output(out);
				o.add(() -> true, ModBlocks.OLIVE_LOG, ModBlocks.OLIVE_PLANKS, ModBlocks.OLIVE_STAIRS, ModBlocks.OLIVE_SLAB, ModBlocks.OLIVE_FENCE,
						ModBlocks.OLIVE_FENCE_GATE, ModBlocks.OLIVE_DOOR, ModBlocks.CARVED_OLIVE_WOOD, ModBlocks.OLIVE_LEAVES, ModBlocks.OLIVE_SAPLING,
						ModBlocks.IRONWOOD_LOG, ModBlocks.IRONWOOD_PLANKS, ModBlocks.IRONWOOD_STAIRS, ModBlocks.IRONWOOD_SLAB, ModBlocks.IRONWOOD_FENCE,
						ModBlocks.IRONWOOD_FENCE_GATE, ModBlocks.IRONWOOD_DOOR, ModBlocks.CARVED_IRONWOOD, ModBlocks.IRONWOOD_LEAVES, ModBlocks.IRONWOOD_SAPLING,
						ModBlocks.APPLE_SEEDS, ModBlocks.APPLE_SAPLING, ModBlocks.APPLE_LEAVES,
						ModBlocks.FERTILE_SOIL, ModBlocks.CROP_STAKE, ModBlocks.GRAPE_STEM, ModBlocks.WILDBERRY_BUSH,
						ModItems.TOMATO_SEEDS, ModItems.CHILI_PEPPER_SEEDS,
						ModItems.OLIVES, ModItems.IRONBERRIES, ModItems.TOMATO, ModItems.CHILI_PEPPER, ModItems.GHOST_PEPPER, ModItems.WILDBERRIES,
						ModItems.GRAPES,
						ModBlocks.BEEHIVE, ModBlocks.APIARY, ModItems.BEE, ModItems.HONEYCOMB, ModItems.BEESWAX, ModItems.TALLOW,
						ModItems.TINY_IRON_DUST, ModItems.GOLD_DUST);
				for (ModFluids.FluidEntry fluid : ModFluids.JUICES) {
					if (fluid.bucket != null) out.accept(fluid.bucket.get());
				}
			}).build());

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALCHEMY = TABS.register("alchemy", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.rusticrevived.alchemy"))
			.icon(() -> new ItemStack(ModItems.ELIXIR.get()))
			.withTabsBefore(FARMING.getId())
			.displayItems((params, out) -> {
				Output o = new Output(out);
				o.add(() -> true, ModBlocks.ALOE_VERA, ModBlocks.BLOOD_ORCHID, ModBlocks.CHAMOMILE, ModItems.CLOUDSBLUFF, ModBlocks.COHOSH,
						ModItems.CORE_ROOT, ModBlocks.DEATHSTALK_MUSHROOM, ModItems.GINSENG, ModBlocks.HORSETAIL, ModItems.MARSH_MALLOW,
						ModBlocks.MOONCAP_MUSHROOM, ModBlocks.WIND_THISTLE, ModBlocks.VANTA_LILY,
						ModBlocks.CONDENSER, ModBlocks.RETORT, ModBlocks.CONDENSER_ADVANCED, ModBlocks.RETORT_ADVANCED,
						ModBlocks.CRUSHING_TUB, ModBlocks.EVAPORATING_BASIN, ModBlocks.BREWING_BARREL, ModBlocks.LIQUID_BARREL);
				// Filled fluid bottles, liquid barrels and elixirs are added by the items themselves
				// (registered in RusticCreativeContents by the alchemy subsystem).
				RusticCreativeContents.ALCHEMY_EXTRAS.forEach(extra -> extra.accept(params, out));
			}).build());

	private ModCreativeTabs() {
	}

	private record Output(CreativeModeTab.Output out) {
		@SafeVarargs
		final void add(BooleanSupplier enabled, Supplier<? extends ItemLike>... items) {
			if (!enabled.getAsBoolean()) return;
			for (Supplier<? extends ItemLike> item : items) {
				out.accept(item.get());
			}
		}
	}

	/** Config checks that tolerate an unloaded config (e.g. during datagen). */
	private static final class ModConfigFlags {
		static boolean check(Supplier<Boolean> value) {
			try {
				return value.get();
			} catch (IllegalStateException e) {
				return true;
			}
		}

		static boolean pillars() {
			return check(RusticConfig.COMMON.enablePillars);
		}

		static boolean slate() {
			return check(RusticConfig.COMMON.enableSlate);
		}

		static boolean clayWalls() {
			return check(RusticConfig.COMMON.enableClayWalls);
		}

		static boolean paintedWood() {
			return check(RusticConfig.COMMON.enablePaintedWood);
		}

		static boolean chairs() {
			return check(RusticConfig.COMMON.enableChairs);
		}

		static boolean tables() {
			return check(RusticConfig.COMMON.enableTables);
		}

		static boolean lattice() {
			return check(RusticConfig.COMMON.enableLattice);
		}

		static boolean silver() {
			return check(RusticConfig.COMMON.enableSilverDecor);
		}
	}
}
