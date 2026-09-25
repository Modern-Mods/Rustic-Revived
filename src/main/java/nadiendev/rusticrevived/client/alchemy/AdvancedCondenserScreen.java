package nadiendev.rusticrevived.client.alchemy;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.menu.alchemy.AdvancedCondenserMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Advanced condenser GUI (legacy GuiCondenserAdvanced): taller progress arrow, no labels.
 */
public class AdvancedCondenserScreen extends AbstractCondenserScreen<AdvancedCondenserMenu> {
	private static final Identifier TEXTURE = RusticRevived.id("textures/gui/condenser_advanced.png");
	public static final int PROGRESS_Y = 17;
	public static final int PROGRESS_HEIGHT = 53;

	public AdvancedCondenserScreen(AdvancedCondenserMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, TEXTURE, PROGRESS_Y, PROGRESS_HEIGHT);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		// the advanced GUI texture has no room for labels
	}
}
