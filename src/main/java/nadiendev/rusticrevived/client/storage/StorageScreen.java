package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.storage.StorageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the vase, barrel and cabinets (legacy GuiVase, GuiBarrel, GuiCabinet, GuiCabinetDouble):
 * the 27 or 54 slot generic chest background.
 */
public class StorageScreen<T extends StorageMenu> extends AbstractContainerScreen<T> {
	private static final Identifier SINGLE = RusticRevived.id("textures/gui/generic_27.png");
	private static final Identifier DOUBLE = RusticRevived.id("textures/gui/generic_54.png");

	private final Identifier background;

	public StorageScreen(T menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, isDouble(menu) ? 222 : 166);
		background = isDouble(menu) ? DOUBLE : SINGLE;
	}

	private static boolean isDouble(StorageMenu menu) {
		return menu.getRowCount() > 3;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
	}
}
