package nadiendev.rusticrevived.client.book;

import com.mojang.blaze3d.systems.RenderSystem;

import nadiendev.rusticrevived.book.BookCategory;
import nadiendev.rusticrevived.book.BookEntry;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * The Almanac's widgets: page turning, back and history buttons, category icons, entry links and
 * related entry ribbons.
 */
public final class BookButtons {
	private BookButtons() {
	}

	/**
	 * A book button with a click callback. Hover animations advance in steps of 1/60 s (the legacy
	 * book advanced them once per rendered frame).
	 */
	abstract static class BookButton extends AbstractButton {
		private static final long STEP_MILLIS = 1000 / 60;

		private final Runnable onPress;
		private long lastStepMillis = Util.getMillis();

		BookButton(int x, int y, int width, int height, Component message, Runnable onPress) {
			super(x, y, width, height, message);
			this.onPress = onPress;
		}

		@Override
		public void onPress() {
			onPress.run();
		}

		/** Number of animation steps elapsed since the last call. */
		protected int animationSteps() {
			long now = Util.getMillis();
			int steps = (int) ((now - lastStepMillis) / STEP_MILLIS);
			lastStepMillis += steps * STEP_MILLIS;
			return steps;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}

	/** Arrow in the bottom corners of the page. */
	public static class PageTurnButton extends BookButton {
		private final boolean forward;

		public PageTurnButton(int x, int y, boolean forward, Runnable onPress) {
			super(x, y, 18, 10, Component.translatable("book.rusticrevived.button." + (forward ? "next" : "prev")), onPress);
			this.forward = forward;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.blit(AlmanacScreen.BOOK_TEXTURE, getX(), getY(), isHovered() ? 18 : 0, forward ? 180 : 190, 18, 10);
		}
	}

	/** Returns to the category (or the index). */
	public static class BackButton extends BookButton {
		public BackButton(int x, int y, Runnable onPress) {
			super(x, y, 16, 10, Component.translatable("book.rusticrevived.button.back"), onPress);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.blit(AlmanacScreen.BOOK_TEXTURE, getX(), getY(), isHovered() ? 16 : 0, 200, 16, 10);
		}
	}

	/** Returns to the previously visited entry. */
	public static class LastHistoryButton extends BookButton {
		public LastHistoryButton(int x, int y, Runnable onPress) {
			super(x, y, 10, 16, Component.translatable("book.rusticrevived.button.last_history"), onPress);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.blit(AlmanacScreen.BOOK_TEXTURE, getX(), getY(), isHovered() ? 46 : 36, 193, 10, 16);
		}
	}

	/** Animated category icon on the index page; hovering plays the animation and shows the name. */
	public static class CategoryButton extends BookButton {
		private static final int SIZE = 32;
		private static final int FRAMES = 8;

		private final Font font;
		private final BookCategory category;
		private int ticksHovered;
		private int frame;

		public CategoryButton(int x, int y, Font font, BookCategory category, Runnable onPress) {
			super(x, y, SIZE, SIZE, Component.translatable(category.getName()), onPress);
			this.font = font;
			this.category = category;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int steps = animationSteps();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			if (isHovered()) {
				for (int i = 0; i < steps; i++) {
					ticksHovered++;
					if (ticksHovered % 2 == 0 && frame < FRAMES - 1) {
						frame++;
					}
				}
				graphics.drawString(font, getMessage(), getX() + width / 2 - font.width(getMessage()) / 2, getY() + height, 0x000000, false);
				graphics.setColor(44 / 256F, 114 / 256F, 44 / 256F, 1F);
			} else {
				ticksHovered = 0;
				frame = Math.max(frame - steps, 0);
				graphics.setColor(0F, 0F, 0F, 1F);
			}
			graphics.blit(category.getIcon(), getX(), getY(), SIZE, SIZE, 0, frame * SIZE, SIZE, SIZE, SIZE, SIZE * FRAMES);
			graphics.setColor(1F, 1F, 1F, 1F);
			RenderSystem.disableBlend();
		}
	}

	/** Entry name with a small item icon on a category page; hovering sweeps a shade under it. */
	public static class EntryLinkButton extends BookButton {
		private final Font font;
		private final BookEntry entry;
		private int ticksHovered;

		public EntryLinkButton(int x, int y, int width, Font font, BookEntry entry, Runnable onPress) {
			super(x, y, width, font.lineHeight + 2, Component.translatable(entry.getName()), onPress);
			this.font = font;
			this.entry = entry;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int steps = animationSteps();
			if (isHovered()) {
				ticksHovered = Math.min(ticksHovered + steps, width);
				graphics.fill(getX(), getY(), getX() + Math.min(ticksHovered * 10, width), getY() + height, 0x40000000);
			} else {
				ticksHovered = 0;
			}
			graphics.drawString(font, getMessage(), getX() + font.lineHeight + 2 + 1, getY() + 2, 0x000000, false);
			if (!entry.getIcon().isEmpty()) {
				float scale = (font.lineHeight + 2) / 16F;
				graphics.pose().pushPose();
				graphics.pose().scale(scale, scale, scale);
				graphics.renderItem(entry.getIcon(), (int) (getX() / scale), (int) (getY() / scale));
				graphics.pose().popPose();
			}
		}
	}

	/** Ribbon on the right of a text page linking to a related entry; unrolls to show its name when hovered. */
	public static class RelatedEntryButton extends BookButton {
		private static final int BASE_WIDTH = 24;
		private static final int BASE_HEIGHT = 16;
		private static final int MAX_WIDTH = 144;

		private final Font font;
		private final BookEntry entry;
		private int ticksHovered;

		public RelatedEntryButton(int x, int y, Font font, BookEntry entry, Runnable onPress) {
			super(x, y, BASE_WIDTH, BASE_HEIGHT, Component.translatable(entry.getName()), onPress);
			this.font = font;
			this.entry = entry;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			String text = getMessage().getString();
			int textWidth = font.width(text);
			int steps = animationSteps();
			if (isHovered()) {
				if (width < 140 && width < textWidth + BASE_WIDTH + 2) {
					ticksHovered += steps;
					setWidth(Math.min(BASE_WIDTH + ticksHovered * 8, Math.min(textWidth + BASE_WIDTH + 2, MAX_WIDTH)));
				}
			} else {
				ticksHovered = 0;
				setWidth(Math.max(width - 12 * steps, BASE_WIDTH));
			}
			graphics.blit(AlmanacScreen.BOOK_TEXTURE, getX(), getY(), 146 - width, 226, width, height);
			text = font.plainSubstrByWidth(text, width - BASE_WIDTH, true);
			graphics.drawString(font, text, getX() + (width - BASE_WIDTH) - font.width(text), getY() + 4, 0x000000, false);
			if (!entry.getIcon().isEmpty()) {
				graphics.renderItem(entry.getIcon(), getX() + width - 22, getY());
			}
		}
	}
}
