package nadiendev.rusticrevived.client.alchemy;

import nadiendev.rusticrevived.menu.alchemy.AbstractCondenserMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Shared condenser GUI (legacy GuiCondenser / GuiCondenserAdvanced): water tank, fuel flame and
 * brewing progress arrow.
 */
public abstract class AbstractCondenserScreen<M extends AbstractCondenserMenu> extends AbstractContainerScreen<M> {
	public static final int TANK_X = 133;
	public static final int TANK_Y = 27;
	public static final int TANK_WIDTH = 16;
	public static final int TANK_HEIGHT = 32;
	public static final int PROGRESS_X = 44;
	public static final int PROGRESS_WIDTH = 50;
	private static final int FLAME_HEIGHT = 13;
	private static final int DEFAULT_BURN_TIME = 200;
	private static final int TEXTURE_SIZE = 256;

	private final Identifier texture;
	private final int progressY;
	private final int progressHeight;

	protected AbstractCondenserScreen(M menu, Inventory playerInventory, Component title, Identifier texture, int progressY, int progressHeight) {
		super(menu, playerInventory, title);
		this.texture = texture;
		this.progressY = progressY;
		this.progressHeight = progressHeight;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		if (isHovering(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
			graphics.setComponentTooltipForNextFrame(font, FluidRendering.tankTooltip(menu.getTank().getFluid(), menu.getTank().getCapacity()), mouseX,
					mouseY);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		FluidRendering.drawTank(graphics, menu.getTank().getFluid(), menu.getTank().getCapacity(), leftPos + TANK_X, topPos + TANK_Y, TANK_WIDTH,
				TANK_HEIGHT);
		if (menu.getBurnTime() > 0) {
			int itemBurnTime = menu.getItemBurnTime() == 0 ? DEFAULT_BURN_TIME : menu.getItemBurnTime();
			int flame = menu.getBurnTime() * FLAME_HEIGHT / itemBurnTime;
			blit(graphics, leftPos + 67, topPos + 46 + 12 - flame, 176, 12 - flame, 14, flame + 1);
		}
		int brewTime = menu.getBrewTime();
		int total = menu.getTotalBrewTime();
		if (brewTime > 0 && total > 0) {
			blit(graphics, leftPos + PROGRESS_X, topPos + progressY, 176, 14, brewTime * PROGRESS_WIDTH / total + 1, progressHeight);
		}
	}

	private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
	}
}
