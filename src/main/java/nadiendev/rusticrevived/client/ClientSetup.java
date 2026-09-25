package nadiendev.rusticrevived.client;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.client.alchemy.AlchemyClientSetup;
import nadiendev.rusticrevived.client.decor.DecorClientSetup;
import nadiendev.rusticrevived.client.farm.FarmClientSetup;
import nadiendev.rusticrevived.client.storage.StorageClientSetup;
import nadiendev.rusticrevived.registry.ModEntities;
import nadiendev.rusticrevived.registry.ModFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

/**
 * Physical client entry point. Only loaded when {@code FMLEnvironment.getDist().isClient()}.
 */
public final class ClientSetup {
	private ClientSetup() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ClientSetup::registerFluidModels);
		modBus.addListener(ClientSetup::registerClientExtensions);
		modBus.addListener(ClientSetup::registerRenderers);

		DecorClientSetup.init(modBus);
		FarmClientSetup.init(modBus);
		StorageClientSetup.init(modBus);
		AlchemyClientSetup.init(modBus);
		RusticClient.init(modBus);
	}

	/** Still / flowing sprites of every Rustic fluid (also used by tanks, bottles and JEI). */
	private static void registerFluidModels(RegisterFluidModelsEvent event) {
		for (ModFluids.FluidEntry fluid : ModFluids.ALL) {
			event.register(new FluidModel.Unbaked(new Material(fluid.stillTexture()), new Material(fluid.flowingTexture()), null, (FluidTintSource) null),
					fluid.source, fluid.flowing);
		}
	}

	/** Screen overlay while the camera is inside a placed juice (legacy RenderBlockOverlayEvent). */
	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		for (ModFluids.FluidEntry fluid : ModFluids.ALL) {
			if (fluid.block == null) continue;
			Identifier overlay = RusticRevived.id("textures/block/fluids/" + fluid.name + "_overlay.png");
			event.registerFluidType(new IClientFluidTypeExtensions() {
				@Override
				public Identifier getRenderOverlayTexture(Minecraft mc) {
					return overlay;
				}
			}, fluid.type.get());
		}
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.TOMATO.get(), ThrownItemRenderer::new);
	}
}
