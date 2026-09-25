package nadiendev.rusticrevived;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.network.RusticNetwork;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModCreativeTabs;
import nadiendev.rusticrevived.registry.ModCriteria;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModEntities;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.ModWorldGen;
import nadiendev.rusticrevived.setup.CommonSetup;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * Rustic Revived: a port of Rustic (1.12.2) to NeoForge.
 * <p>
 * The mod id is {@value #MODID}; every registry entry, asset and data file lives in the
 * {@value #NAMESPACE} namespace (declared as a second mod in the same jar, see
 * {@link RusticRevivedNamespace}).
 */
@Mod(RusticRevived.MODID)
public class RusticRevived {
	/** Mod id (mods.toml, events, configs). */
	public static final String MODID = "rustic";
	/** Namespace of every registry entry, asset and data file. */
	public static final String NAMESPACE = "rusticrevived";
	public static final Logger LOGGER = LogUtils.getLogger();

	public RusticRevived(IEventBus modBus, ModContainer container) {
		// fluids first: they add their liquid blocks and buckets to the block / item registers
		ModFluids.FLUID_TYPES.register(modBus);
		ModFluids.FLUIDS.register(modBus);
		ModBlocks.BLOCKS.register(modBus);
		ModItems.ITEMS.register(modBus);
		ModBlockEntities.BLOCK_ENTITIES.register(modBus);
		ModMenus.MENUS.register(modBus);
		ModEntities.ENTITIES.register(modBus);
		ModEffects.EFFECTS.register(modBus);
		ModDataComponents.COMPONENTS.register(modBus);
		ModRecipes.TYPES.register(modBus);
		ModRecipes.SERIALIZERS.register(modBus);
		ModRecipes.BOOK_CATEGORIES.register(modBus);
		ModRecipes.CONDITIONS.register(modBus);
		ModCreativeTabs.TABS.register(modBus);
		ModWorldGen.register(modBus);
		ModCriteria.TRIGGERS.register(modBus);

		container.registerConfig(ModConfig.Type.COMMON, RusticConfig.COMMON_SPEC);
		container.registerConfig(ModConfig.Type.CLIENT, RusticConfig.CLIENT_SPEC);

		modBus.addListener(RusticNetwork::register);
		CommonSetup.init(modBus);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}
}
