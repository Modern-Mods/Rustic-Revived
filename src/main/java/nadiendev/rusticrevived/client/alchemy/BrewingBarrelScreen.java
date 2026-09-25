package nadiendev.rusticrevived.client.alchemy;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity;
import nadiendev.rusticrevived.menu.alchemy.BrewingBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Brewing barrel GUI (legacy GuiBrewingBarrel): culture, input and output tanks, ghost icons in
 * the empty container slots, bubbles and brewing progress.
 */
public class BrewingBarrelScreen extends AbstractContainerScreen<BrewingBarrelMenu> {
	private static final ResourceLocation TEXTURE = RusticRevived.id("textures/gui/brewing_barrel.png");
	private static final int BUBBLES_HEIGHT = 28;
	/** Bubble cycles over a whole brew. */
	private static final int BUBBLE_CYCLES = 5600;

	public BrewingBarrelScreen(BrewingBarrelMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
		BrewingBarrelBlockEntity barrel = menu.getBarrel();
		tankTooltip(graphics, barrel.getInput(), 62, 27, 32, mouseX, mouseY);
		tankTooltip(graphics, barrel.getOutput(), 116, 27, 32, mouseX, mouseY);
		tankTooltip(graphics, barrel.getCulture(), 26, 35, 16, mouseX, mouseY);
	}

	private void tankTooltip(GuiGraphics graphics, FluidTank tank, int x, int y, int height, int mouseX, int mouseY) {
		if (isHovering(x, y, 16, height, mouseX, mouseY)) {
			graphics.renderComponentTooltip(font, FluidRendering.tankTooltip(tank.getFluid(), tank.getCapacity()), mouseX, mouseY);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// the legacy barrel GUI has no labels
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		BrewingBarrelBlockEntity barrel = menu.getBarrel();
		drawTank(graphics, barrel.getInput(), 62, 27, 32);
		drawTank(graphics, barrel.getOutput(), 116, 27, 32);
		drawTank(graphics, barrel.getCulture(), 26, 35, 16);

		if (menu.getSlot(BrewingBarrelBlockEntity.INPUT_IN_SLOT).getItem().isEmpty()) {
			graphics.blit(TEXTURE, leftPos + 62, topPos + 7, 176, 54, 16, 16);
		}
		if (menu.getSlot(BrewingBarrelBlockEntity.OUTPUT_IN_SLOT).getItem().isEmpty()) {
			graphics.blit(TEXTURE, leftPos + 116, topPos + 7, 176, 70, 16, 16);
		}
		if (menu.getSlot(BrewingBarrelBlockEntity.CULTURE_IN_SLOT).getItem().isEmpty()) {
			graphics.blit(TEXTURE, leftPos + 26, topPos + 15, 176, 70, 16, 16);
		}

		int brewTime = menu.getBrewTime();
		int maxBrewTime = menu.getMaxBrewTime();
		if (brewTime > 0 && maxBrewTime > 0) {
			int bubbles = (int) ((brewTime / (float) maxBrewTime) * BUBBLE_CYCLES) % BUBBLES_HEIGHT;
			graphics.blit(TEXTURE, leftPos + 139, topPos + 29 + BUBBLES_HEIGHT - bubbles, 176, BUBBLES_HEIGHT - bubbles, 11, bubbles);
			graphics.blit(TEXTURE, leftPos + 85, topPos + 35, 176, 28, brewTime * 24 / maxBrewTime + 1, 16);
			graphics.blit(TEXTURE, leftPos + 45, topPos + 38, 176, 44, brewTime * 10 / maxBrewTime + 1, 10);
		}
	}

	private void drawTank(GuiGraphics graphics, FluidTank tank, int x, int y, int height) {
		FluidRendering.drawTank(graphics, tank.getFluid(), tank.getCapacity(), leftPos + x, topPos + y, 16, height);
	}
}
