package nadiendev.rusticrevived.client.storage;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.storage.ApiaryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Apiary screen (legacy GuiApiary). Blacklisted from Inventory Tweaks sorting by class name
 * (see {@link nadiendev.rusticrevived.compat.invtweaks.InvTweaksCompat}): keep the name in sync.
 */
public class ApiaryScreen extends AbstractContainerScreen<ApiaryMenu> {
	private static final Identifier BACKGROUND = RusticRevived.id("textures/gui/apiary.png");

	public ApiaryScreen(ApiaryMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
	}
}
