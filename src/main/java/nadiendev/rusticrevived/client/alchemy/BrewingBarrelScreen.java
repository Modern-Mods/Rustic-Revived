package nadiendev.rusticrevived.client.alchemy;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyTank;
import nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity;
import nadiendev.rusticrevived.menu.alchemy.BrewingBarrelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Brewing barrel GUI (legacy GuiBrewingBarrel): culture, input and output tanks, ghost icons in
 * the empty container slots, bubbles and brewing progress.
 */
public class BrewingBarrelScreen extends AbstractContainerScreen<BrewingBarrelMenu> {
	private static final Identifier TEXTURE = RusticRevived.id("textures/gui/brewing_barrel.png");
	private static final int TEXTURE_SIZE = 256;
	private static final int BUBBLES_HEIGHT = 28;
	/** Bubble cycles over a whole brew. */
	private static final int BUBBLE_CYCLES = 5600;

	public BrewingBarrelScreen(BrewingBarrelMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		BrewingBarrelBlockEntity barrel = menu.getBarrel();
		tankTooltip(graphics, barrel.getInput(), 62, 27, 32, mouseX, mouseY);
		tankTooltip(graphics, barrel.getOutput(), 116, 27, 32, mouseX, mouseY);
		tankTooltip(graphics, barrel.getCulture(), 26, 35, 16, mouseX, mouseY);
	}

	private void tankTooltip(GuiGraphicsExtractor graphics, AlchemyTank tank, int x, int y, int height, int mouseX, int mouseY) {
		if (isHovering(x, y, 16, height, mouseX, mouseY)) {
			graphics.setComponentTooltipForNextFrame(font, FluidRendering.tankTooltip(tank.getFluid(), tank.getCapacity()), mouseX, mouseY);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		// the legacy barrel GUI has no labels
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		BrewingBarrelBlockEntity barrel = menu.getBarrel();
		drawTank(graphics, barrel.getInput(), 62, 27, 32);
		drawTank(graphics, barrel.getOutput(), 116, 27, 32);
		drawTank(graphics, barrel.getCulture(), 26, 35, 16);

		if (menu.getSlot(BrewingBarrelBlockEntity.INPUT_IN_SLOT).getItem().isEmpty()) {
			blit(graphics, leftPos + 62, topPos + 7, 176, 54, 16, 16);
		}
		if (menu.getSlot(BrewingBarrelBlockEntity.OUTPUT_IN_SLOT).getItem().isEmpty()) {
			blit(graphics, leftPos + 116, topPos + 7, 176, 70, 16, 16);
		}
		if (menu.getSlot(BrewingBarrelBlockEntity.CULTURE_IN_SLOT).getItem().isEmpty()) {
			blit(graphics, leftPos + 26, topPos + 15, 176, 70, 16, 16);
		}

		int brewTime = menu.getBrewTime();
		int maxBrewTime = menu.getMaxBrewTime();
		if (brewTime > 0 && maxBrewTime > 0) {
			int bubbles = (int) ((brewTime / (float) maxBrewTime) * BUBBLE_CYCLES) % BUBBLES_HEIGHT;
			blit(graphics, leftPos + 139, topPos + 29 + BUBBLES_HEIGHT - bubbles, 176, BUBBLES_HEIGHT - bubbles, 11, bubbles);
			blit(graphics, leftPos + 85, topPos + 35, 176, 28, brewTime * 24 / maxBrewTime + 1, 16);
			blit(graphics, leftPos + 45, topPos + 38, 176, 44, brewTime * 10 / maxBrewTime + 1, 10);
		}
	}

	private void drawTank(GuiGraphicsExtractor graphics, AlchemyTank tank, int x, int y, int height) {
		FluidRendering.drawTank(graphics, tank.getFluid(), tank.getCapacity(), leftPos + x, topPos + y, 16, height);
	}

	private static void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
	}
}
