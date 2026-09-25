package nadiendev.rusticrevived.client.book;

import nadiendev.rusticrevived.book.BookEntry;
import nadiendev.rusticrevived.book.BookPage;
import nadiendev.rusticrevived.book.pages.CategoriesPage;
import nadiendev.rusticrevived.book.pages.CategoryPage;
import nadiendev.rusticrevived.book.pages.TextPage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Client side view of a {@link BookPage}: adds the page's widgets and draws its contents.
 */
public interface PageView {
	/** Adds the widgets of the page (the screen's widgets were just cleared). */
	void init(AlmanacScreen screen);

	/** Draws the page contents over the book background and the widgets. */
	void render(AlmanacScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

	static PageView of(BookPage page) {
		if (page instanceof CategoriesPage categories) return new Categories(categories);
		if (page instanceof CategoryPage category) return new Category(category);
		if (page instanceof TextPage text) return new Text(text);
		throw new IllegalArgumentException("Unknown book page " + page.getClass().getName());
	}

	/** The index: one animated button per category. */
	record Categories(CategoriesPage page) implements PageView {
		@Override
		public void init(AlmanacScreen screen) {
			int x = screen.getGuiLeft() + AlmanacScreen.WIDTH / 2 - 16;
			int y = screen.getGuiTop() + 28;
			for (var category : page.getCategories()) {
				screen.addPageWidget(new BookButtons.CategoryButton(x, y, screen.getFont(), category, () -> screen.goToEntry(category.getCategoryEntry())));
				y += 48;
			}
		}

		@Override
		public void render(AlmanacScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			screen.drawTitle(graphics, page.getEntry().getName(), screen.getGuiLeft() + AlmanacScreen.WIDTH / 2, screen.getGuiTop() + 12);
		}
	}

	/** A category: a list of entry links. */
	record Category(CategoryPage page) implements PageView {
		@Override
		public void init(AlmanacScreen screen) {
			screen.addNavButtons();
			Font font = screen.getFont();
			int x = screen.getGuiLeft() + 14;
			int y = screen.getGuiTop() + 24;
			for (BookEntry entry : page.getEntries()) {
				screen.addPageWidget(new BookButtons.EntryLinkButton(x, y, AlmanacScreen.WIDTH - 16 - 14, font, entry, () -> screen.goToEntry(entry)));
				y += font.lineHeight + 2;
			}
		}

		@Override
		public void render(AlmanacScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			screen.drawTitle(graphics, page.getCategory().getName(), screen.getGuiLeft() + AlmanacScreen.WIDTH / 2, screen.getGuiTop() + 12);
		}
	}

	/** Text paragraphs (drawn at 3/4 scale) and ribbons linking to related entries. */
	record Text(TextPage page) implements PageView {
		private static final float TEXT_SCALE = 0.75F;
		private static final int MAX_RELATED_ENTRIES = 9;

		@Override
		public void init(AlmanacScreen screen) {
			screen.addNavButtons();
			int x = screen.getGuiLeft() + AlmanacScreen.WIDTH;
			int y = screen.getGuiTop() + 10 + 16 + 2;
			int count = 0;
			for (BookEntry related : page.getRelatedEntries()) {
				if (count++ >= MAX_RELATED_ENTRIES) break;
				screen.addPageWidget(new BookButtons.RelatedEntryButton(x, y, screen.getFont(), related, () -> screen.goToEntry(related)));
				y += 16;
			}
		}

		@Override
		public void render(AlmanacScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			Font font = screen.getFont();
			int left = screen.getGuiLeft();
			int top = screen.getGuiTop();
			int y = top + 12;
			int maxY = top + AlmanacScreen.HEIGHT - 19;
			screen.drawTitle(graphics, page.getEntry().getName(), left + AlmanacScreen.WIDTH / 2, y);
			y += font.lineHeight + 4;
			int x = left + 16;

			graphics.pose().pushMatrix();
			graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
			int increment = (int) ((font.lineHeight + 2) * TEXT_SCALE);
			for (String key : page.getTextKeys()) {
				for (FormattedCharSequence line : font.split(Component.translatable(key), (int) ((AlmanacScreen.WIDTH - 32) / TEXT_SCALE))) {
					if (y + increment > maxY) break;
					graphics.text(font, line, (int) (x / TEXT_SCALE), (int) (y / TEXT_SCALE), AlmanacScreen.TEXT_COLOR, false);
					y += increment;
				}
				y += (int) (4 * TEXT_SCALE);
			}
			graphics.pose().popMatrix();

			if (!page.getRelatedEntries().isEmpty()) {
				Component label = Component.translatable("book.rusticrevived.label.related_entries").withStyle(ChatFormatting.UNDERLINE);
				int labelWidth = font.width(label);
				graphics.blit(RenderPipelines.GUI_TEXTURED, AlmanacScreen.BOOK_TEXTURE, left + AlmanacScreen.WIDTH, top + 10, (146 - 10) - (labelWidth + 2), 210,
						labelWidth + 2 + 10, 16, AlmanacScreen.TEXTURE_SIZE, AlmanacScreen.TEXTURE_SIZE);
				graphics.text(font, label, left + AlmanacScreen.WIDTH + 2, top + 10 + 4, AlmanacScreen.TEXT_COLOR, false);
			}
		}
	}
}
