package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.item.VaseItem;
import nadiendev.rusticrevived.network.VaseDesignPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Sneaking and scrolling with a vase in the main hand cycles its design instead of the hotbar slot
 * (legacy EventHandlerClient#onVaseMouseWheel).
 */
public final class VaseScrollHandler {
	private VaseScrollHandler() {
	}

	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.screen != null || !player.isShiftKeyDown() || event.getScrollDeltaY() == 0) return;
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof VaseItem vase)) return;

		event.setCanceled(true);
		int design = VaseItem.cycleDesign(VaseItem.getDesign(stack), event.getScrollDeltaY() > 0 ? 1 : -1);
		vase.setDesign(stack, design);
		PacketDistributor.sendToServer(new VaseDesignPayload(design));
	}
}
