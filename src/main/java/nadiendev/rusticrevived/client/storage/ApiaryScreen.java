package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.storage.ApiaryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Apiary screen (legacy GuiApiary). Blacklisted from Inventory Tweaks sorting by class name
 * (see {@link nadiendev.rusticrevived.compat.invtweaks.InvTweaksCompat}): keep the name in sync.
 */
public class ApiaryScreen extends AbstractContainerScreen<ApiaryMenu> {
	private static final ResourceLocation BACKGROUND = RusticRevived.id("textures/gui/apiary.png");

	public ApiaryScreen(ApiaryMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
