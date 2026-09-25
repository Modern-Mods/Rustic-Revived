package nadiendev.rusticrevived.client;

import nadiendev.rusticrevived.client.misc.ArmorHud;
import nadiendev.rusticrevived.client.misc.FullmetalClient;
import nadiendev.rusticrevived.client.misc.MetalSkinClient;
import nadiendev.rusticrevived.network.FirePowerAttackPayload;
import nadiendev.rusticrevived.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * General client features (legacy EventHandlerClient / ClientProxy): iron skin layer, fullmetal
 * overlay, FOV and input lock, extra armor and toughness HUD, and the Fire Power attack input.
 * The Almanac screen is opened by {@link nadiendev.rusticrevived.item.AlmanacItem}.
 */
public final class RusticClient {
	private RusticClient() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(MetalSkinClient::addLayers);
		modBus.addListener(MetalSkinClient::registerReloadListeners);
		modBus.addListener(RusticClient::registerGuiLayers);

		NeoForge.EVENT_BUS.addListener(MetalSkinClient::onRenderArm);
		NeoForge.EVENT_BUS.addListener(FullmetalClient::onComputeFov);
		NeoForge.EVENT_BUS.addListener(FullmetalClient::onMovementInput);
		NeoForge.EVENT_BUS.addListener(ArmorHud::onRenderLayer);
		NeoForge.EVENT_BUS.addListener(RusticClient::onInteractionKey);
	}

	private static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelowAll(FullmetalClient.OVERLAY_LAYER, FullmetalClient::renderOverlay);
		event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, ArmorHud.TOUGHNESS_LAYER, ArmorHud::renderToughness);
	}

	/** Under Fire Power, starting an attack (not sneaking, not mid swing) shoots a fireball. */
	private static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
		if (!event.isAttack()) return;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.hasEffect(ModEffects.FIRE_POWER) && !player.swinging && !player.isShiftKeyDown()) {
			PacketDistributor.sendToServer(new FirePowerAttackPayload());
		}
	}
}
