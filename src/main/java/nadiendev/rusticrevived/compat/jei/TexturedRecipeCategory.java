package nadiendev.rusticrevived.compat.jei;

import java.util.Locale;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * Base of Rustic's JEI categories: draws the top-left corner of a {@code textures/gui/jei_*.png}
 * sheet (256x256) as background and provides small text helpers.
 */
public abstract class TexturedRecipeCategory<T> extends AbstractRecipeCategory<T> {
	private static final int TEXT_COLOR = 0xFF404040;

	protected final IGuiHelper guiHelper;
	protected final ResourceLocation texture;
	private final IDrawableStatic background;

	protected TexturedRecipeCategory(IGuiHelper guiHelper, RecipeType<T> type, String titleKey, ItemLike icon, String textureName,
			int width, int height) {
		super(type, Component.translatable(titleKey), guiHelper.createDrawableItemLike(icon), width, height);
		this.guiHelper = guiHelper;
		this.texture = RusticRevived.id("textures/gui/" + textureName + ".png");
		this.background = guiHelper.createDrawable(texture, 0, 0, width, height);
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics);
	}

	/** Region of the texture sheet, e.g. an animation frame. */
	protected IDrawableStatic sprite(int u, int v, int width, int height) {
		return guiHelper.createDrawable(texture, u, v, width, height);
	}

	protected static void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y) {
		Font font = Minecraft.getInstance().font;
		guiGraphics.drawString(font, text, centerX - font.width(text) / 2, y, TEXT_COLOR, false);
	}

	protected static void drawRightAligned(GuiGraphics guiGraphics, Component text, int right, int y) {
		Font font = Minecraft.getInstance().font;
		guiGraphics.drawString(font, text, right - font.width(text), y, TEXT_COLOR, false);
	}

	/** "12.5 s" style duration of a recipe. */
	protected static Component duration(int ticks) {
		float seconds = ticks / 20F;
		String text = seconds == (int) seconds ? Integer.toString((int) seconds) : String.format(Locale.ROOT, "%.1f", seconds);
		return Component.translatable("jei.rusticrevived.recipe.time", text);
	}

	/** Capacity of a fluid gauge: the amount itself above one bucket, so small amounts stay visible. */
	protected static long gaugeCapacity(long amount) {
		return Math.max(amount, 1000L);
	}
}
