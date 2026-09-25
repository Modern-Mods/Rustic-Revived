package nadiendev.rusticrevived.registry;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.world.feature.BeehiveFeature;
import nadiendev.rusticrevived.world.feature.SlateVeinFeature;
import nadiendev.rusticrevived.world.loot.GrapeSeedsModifier;
import nadiendev.rusticrevived.world.loot.GrassSeedsModifier;
import nadiendev.rusticrevived.world.placement.ConfigChancePlacement;
import nadiendev.rusticrevived.world.placement.ConfigCountPlacement;
import nadiendev.rusticrevived.world.placement.ConfigFilterPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World generation and loot modifier registries (features, placement modifiers, tree parts,
 * global loot modifier codecs). Owned by the farm subsystem.
 * <p>
 * The legacy olive, ironwood and apple trees use the vanilla straight trunk / blob foliage shape,
 * so no custom trunk or foliage placers are needed.
 */
public final class ModWorldGen {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RusticRevived.NAMESPACE);

	// ================================================================ features

	public static final DeferredHolder<Feature<?>, BeehiveFeature> BEEHIVE = FEATURES.register("beehive",
			() -> new BeehiveFeature(NoneFeatureConfiguration.CODEC));
	public static final DeferredHolder<Feature<?>, SlateVeinFeature> SLATE_VEIN = FEATURES.register("slate_vein",
			() -> new SlateVeinFeature(OreConfiguration.CODEC));

	// ================================================================ placement modifiers

	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigChancePlacement>> CONFIG_CHANCE =
			PLACEMENT_MODIFIERS.register("config_chance", () -> () -> ConfigChancePlacement.CODEC);
	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigCountPlacement>> CONFIG_COUNT =
			PLACEMENT_MODIFIERS.register("config_count", () -> () -> ConfigCountPlacement.CODEC);
	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigFilterPlacement>> CONFIG_FILTER =
			PLACEMENT_MODIFIERS.register("config_filter", () -> () -> ConfigFilterPlacement.CODEC);

	// ================================================================ global loot modifiers

	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GrassSeedsModifier>> GRASS_SEEDS =
			LOOT_MODIFIERS.register("grass_seeds", () -> GrassSeedsModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GrapeSeedsModifier>> GRAPE_SEEDS =
			LOOT_MODIFIERS.register("grape_seeds", () -> GrapeSeedsModifier.CODEC);

	private ModWorldGen() {
	}

	public static void register(IEventBus modBus) {
		FEATURES.register(modBus);
		PLACEMENT_MODIFIERS.register(modBus);
		TRUNK_PLACERS.register(modBus);
		FOLIAGE_PLACERS.register(modBus);
		TREE_DECORATORS.register(modBus);
		LOOT_MODIFIERS.register(modBus);
	}
}
