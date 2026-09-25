package nadiendev.rusticrevived.client;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.client.alchemy.AlchemyClientSetup;
import nadiendev.rusticrevived.client.decor.DecorClientSetup;
import nadiendev.rusticrevived.client.farm.FarmClientSetup;
import nadiendev.rusticrevived.client.storage.StorageClientSetup;
import nadiendev.rusticrevived.registry.ModEntities;
import nadiendev.rusticrevived.registry.ModFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Physical client entry point. Only loaded when {@code FMLEnvironment.dist.isClient()}.
 */
public final class ClientSetup {
	private ClientSetup() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ClientSetup::registerClientExtensions);
		modBus.addListener(ClientSetup::registerRenderers);

		DecorClientSetup.init(modBus);
		FarmClientSetup.init(modBus);
		StorageClientSetup.init(modBus);
		AlchemyClientSetup.init(modBus);
		RusticClient.init(modBus);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		for (ModFluids.FluidEntry fluid : ModFluids.ALL) {
			ResourceLocation still = fluid.stillTexture();
			ResourceLocation flowing = fluid.flowingTexture();
			// screen overlay while the camera is inside a placed juice (legacy RenderBlockOverlayEvent)
			ResourceLocation overlay = fluid.block != null ? RusticRevived.id("textures/block/fluids/" + fluid.name + "_overlay.png") : null;
			event.registerFluidType(new IClientFluidTypeExtensions() {
				@Override
				public ResourceLocation getStillTexture() {
					return still;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return flowing;
				}

				@Override
				public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
					return overlay;
				}
			}, fluid.type.get());
		}
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.TOMATO.get(), ThrownItemRenderer::new);
	}
}
