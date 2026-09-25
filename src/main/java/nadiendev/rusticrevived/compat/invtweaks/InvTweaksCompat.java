package nadiendev.rusticrevived.compat.invtweaks;

import net.neoforged.fml.InterModComms;

/**
 * Inventory Tweaks Refoxed integration. It sorts every screen with non player slots, so the vase,
 * barrel and cabinet menus (plain chest-like slots) sort out of the box; the apiary's filtered bee
 * and honeycomb slots must not be sorted, so its screen is blacklisted through IMC.
 */
public final class InvTweaksCompat {
	public static final String MODID = "invtweaks";
	private static final String BLACKLIST_SCREEN = "blacklist-screen";
	/** Class name of {@code client.storage.ApiaryScreen}, as a string so the dedicated server never loads it. */
	private static final String APIARY_SCREEN = "nadiendev.rusticrevived.client.storage.ApiaryScreen";

	private InvTweaksCompat() {
	}

	public static void sendImc() {
		InterModComms.sendTo(MODID, BLACKLIST_SCREEN, () -> APIARY_SCREEN);
	}
}
