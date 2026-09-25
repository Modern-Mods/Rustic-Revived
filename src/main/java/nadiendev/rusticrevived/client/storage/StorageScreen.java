package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.storage.StorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the vase, barrel and cabinets (legacy GuiVase, GuiBarrel, GuiCabinet, GuiCabinetDouble):
 * the 27 or 54 slot generic chest background.
 */
public class StorageScreen<T extends StorageMenu> extends AbstractContainerScreen<T> {
	private static final ResourceLocation SINGLE = RusticRevived.id("textures/gui/generic_27.png");
	private static final ResourceLocation DOUBLE = RusticRevived.id("textures/gui/generic_54.png");

	private final ResourceLocation background;

	public StorageScreen(T menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		boolean isDouble = menu.getRowCount() > 3;
		background = isDouble ? DOUBLE : SINGLE;
		imageHeight = isDouble ? 222 : 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
