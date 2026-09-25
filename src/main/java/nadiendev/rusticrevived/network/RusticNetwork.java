package nadiendev.rusticrevived.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Rustic's custom packets. Shame particles are sent with vanilla particle packets.
 */
public final class RusticNetwork {
	private RusticNetwork() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(FirePowerAttackPayload.TYPE, FirePowerAttackPayload.STREAM_CODEC, FirePowerAttackPayload::handle);
		registrar.playToServer(VaseDesignPayload.TYPE, VaseDesignPayload.STREAM_CODEC, VaseDesignPayload::handle);
	}
}
