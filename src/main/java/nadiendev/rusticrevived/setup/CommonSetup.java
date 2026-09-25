package nadiendev.rusticrevived.setup;

import nadiendev.rusticrevived.client.ClientSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Wires every subsystem's setup class.
 */
public final class CommonSetup {
	private CommonSetup() {
	}

	public static void init(IEventBus modBus) {
		DecorSetup.init(modBus);
		FarmSetup.init(modBus);
		StorageSetup.init(modBus);
		AlchemySetup.init(modBus);
		CompatSetup.init(modBus);

		modBus.addListener(CommonSetup::onCommonSetup);
		modBus.addListener(CommonSetup::onRegisterCapabilities);

		if (FMLEnvironment.dist.isClient()) {
			ClientSetup.init(modBus);
		}
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			DecorSetup.commonSetup();
			FarmSetup.commonSetup();
			StorageSetup.commonSetup();
			AlchemySetup.commonSetup();
			CompatSetup.commonSetup();
		});
	}

	private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		DecorSetup.registerCapabilities(event);
		FarmSetup.registerCapabilities(event);
		StorageSetup.registerCapabilities(event);
		AlchemySetup.registerCapabilities(event);
	}
}
