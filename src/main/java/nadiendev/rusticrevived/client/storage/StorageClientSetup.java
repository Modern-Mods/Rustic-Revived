package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.storage.CabinetMenu;
import nadiendev.rusticrevived.menu.storage.DoubleCabinetMenu;
import nadiendev.rusticrevived.menu.storage.RusticBarrelMenu;
import nadiendev.rusticrevived.menu.storage.VaseMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModMenus;
import net.minecraft.ChatFormatting;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client setup of the storage subsystem: screens, the cabinet renderers (block entity and item
 * special model), the material colour cache and tooltip, and the vase scroll input. The vase item
 * selects its design model on the vanilla {@code block_state} item model property (see the
 * generated client item). Called from {@link nadiendev.rusticrevived.client.ClientSetup} on the
 * physical client only.
 */
public final class StorageClientSetup {
	private StorageClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(StorageClientSetup::registerScreens);
		modBus.addListener(StorageClientSetup::registerLayers);
		modBus.addListener(StorageClientSetup::registerRenderers);
		modBus.addListener(StorageClientSetup::registerSpecialRenderers);
		modBus.addListener(StorageClientSetup::registerReloadListeners);
		NeoForge.EVENT_BUS.addListener(VaseScrollHandler::onMouseScroll);
		NeoForge.EVENT_BUS.addListener(StorageClientSetup::addMaterialTooltip);
	}

	private static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.VASE.get(), StorageScreen<VaseMenu>::new);
		event.register(ModMenus.BARREL.get(), StorageScreen<RusticBarrelMenu>::new);
		event.register(ModMenus.CABINET.get(), StorageScreen<CabinetMenu>::new);
		event.register(ModMenus.CABINET_DOUBLE.get(), StorageScreen<DoubleCabinetMenu>::new);
		event.register(ModMenus.APIARY.get(), ApiaryScreen::new);
	}

	private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CabinetModel.SINGLE, () -> CabinetModel.createSingle(false));
		event.registerLayerDefinition(CabinetModel.SINGLE_MIRROR, () -> CabinetModel.createSingle(true));
		event.registerLayerDefinition(CabinetModel.DOUBLE, () -> CabinetModel.createDouble(false));
		event.registerLayerDefinition(CabinetModel.DOUBLE_MIRROR, () -> CabinetModel.createDouble(true));
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.CABINET.get(), CabinetRenderer::new);
	}

	private static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(CabinetSpecialRenderer.ID, CabinetSpecialRenderer.Unbaked.MAP_CODEC);
	}

	private static void registerReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(RusticRevived.id("cabinet_material_colors"), (ResourceManagerReloadListener) resourceManager -> MaterialColors.clear());
	}

	/** Cabinets show the planks they were made from under their name (legacy BlockCabinet#addInformation). */
	private static void addMaterialTooltip(ItemTooltipEvent event) {
		Item material = event.getItemStack().get(ModDataComponents.CABINET_MATERIAL.get());
		if (material != null && !event.getToolTip().isEmpty()) {
			event.getToolTip().add(1, material.getName(material.getDefaultInstance()).copy().withStyle(ChatFormatting.GRAY));
		}
	}
}
