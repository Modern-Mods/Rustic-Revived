package nadiendev.rusticrevived.client.book;

import java.util.ArrayList;
import java.util.List;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.book.Almanac;
import nadiendev.rusticrevived.book.BookEntry;
import nadiendev.rusticrevived.book.BookEntryCategory;
import nadiendev.rusticrevived.book.BookPage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The Almanac screen (legacy GuiBook): a single page book with index, category and text pages,
 * page turning, a back button and a history of visited entries.
 */
public class AlmanacScreen extends Screen {
	public static final int WIDTH = 146;
	public static final int HEIGHT = 180;
	public static final ResourceLocation BOOK_TEXTURE = RusticRevived.id("textures/gui/book/book.png");

	private final Almanac almanac;
	private final List<BookLocation> history = new ArrayList<>();
	private int guiLeft;
	private int guiTop;
	private BookEntry currentEntry;
	private BookEntry prevEntry;
	private int currentPageNum;
	private int prevPageNum;
	private BookPage currentPage;
	private PageView pageView;
	private BookLocation backLocation;

	public AlmanacScreen(Almanac almanac) {
		super(Component.translatable(almanac.getIndex().getName()));
		this.almanac = almanac;
	}

	/** Opens a freshly built Almanac. Only called on the physical client. */
	public static void open() {
		Minecraft.getInstance().setScreen(new AlmanacScreen(Almanac.create()));
	}

	@Override
	protected void init() {
		guiTop = (height - HEIGHT) / 2;
		guiLeft = (width - WIDTH) / 2;
		if (currentEntry == null || currentPage == null) {
			currentEntry = almanac.getIndex();
			currentPageNum = 0;
			currentPage = currentEntry.getPages().get(0);
		}
		openPage();
	}

	private void openPage() {
		clearWidgets();
		pageView = PageView.of(currentPage);
		pageView.init(this);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		pageView.render(this, graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(graphics);
		graphics.blit(BOOK_TEXTURE, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);
	}

	@Override
	public void tick() {
		prevEntry = currentEntry;
		prevPageNum = currentPageNum;
		if (minecraft.player == null || !minecraft.player.isAlive()) {
			onClose();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			if (currentEntry == almanac.getIndex()) {
				onClose();
			} else {
				goBack();
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ---------------------------------------------------------------- navigation

	public void goToPage(int pageNum) {
		List<BookPage> pages = currentEntry.getPages();
		if (pageNum >= 0 && pageNum < pages.size() && prevPageNum == currentPageNum) {
			currentPageNum = pageNum;
			currentPage = pages.get(pageNum);
			openPage();
		}
	}

	public void goToEntry(BookEntry entry) {
		if (entry == null || entry.getPages().isEmpty() || prevEntry != currentEntry) return;
		boolean targetIsIndex = entry instanceof BookEntryCategory || entry == almanac.getIndex();
		boolean currentIsIndex = currentEntry instanceof BookEntryCategory || currentEntry == almanac.getIndex();
		if (currentEntry instanceof BookEntryCategory && !targetIsIndex) {
			backLocation = new BookLocation(currentEntry, currentPageNum);
		}
		if (targetIsIndex) {
			backLocation = null;
			history.clear();
		} else if (!currentIsIndex) {
			history.add(new BookLocation(currentEntry, currentPageNum));
		}
		showPage(entry, 0);
	}

	private void goToHistoryLocation(int index) {
		if (index < 0 || index >= history.size() || prevEntry != currentEntry || prevPageNum != currentPageNum) return;
		BookLocation location = history.get(index);
		history.subList(index, history.size()).clear();
		showPage(location.entry(), location.page());
	}

	private void showPage(BookEntry entry, int pageNum) {
		List<BookPage> pages = entry.getPages();
		currentEntry = entry;
		currentPageNum = Math.min(pageNum, pages.size() - 1);
		currentPage = pages.get(currentPageNum);
		openPage();
	}

	/** Back button / inventory key: category entries go to the index, entries to where they were opened from. */
	private void goBack() {
		if (currentEntry.getCategory() == null) {
			goToEntry(almanac.getIndex());
		} else if (backLocation != null) {
			BookLocation location = backLocation;
			goToEntry(location.entry());
			if (currentEntry == location.entry()) {
				showPage(location.entry(), location.page());
			}
		} else {
			goToEntry(currentEntry.getCategory().getCategoryEntry());
		}
	}

	/** Adds the page turn, back and history buttons of the current page. */
	public void addNavButtons() {
		int numPages = currentEntry.getPages().size();
		if (currentPageNum > 0) {
			addRenderableWidget(new BookButtons.PageTurnButton(guiLeft, guiTop + HEIGHT - 10, false, () -> goToPage(currentPageNum - 1)));
		}
		if (currentPageNum < numPages - 1) {
			addRenderableWidget(new BookButtons.PageTurnButton(guiLeft + WIDTH - 18, guiTop + HEIGHT - 10, true, () -> goToPage(currentPageNum + 1)));
		}
		boolean backButton = currentEntry != almanac.getIndex();
		boolean historyButton = !history.isEmpty() && backButton;
		if (backButton) {
			int x = historyButton ? guiLeft + WIDTH / 2 - 30 / 2 : guiLeft + WIDTH / 2 - 8;
			addRenderableWidget(new BookButtons.BackButton(x, guiTop + HEIGHT, this::goBack));
		}
		if (historyButton) {
			addRenderableWidget(new BookButtons.LastHistoryButton(guiLeft + WIDTH / 2 + 30 / 2 - 10, guiTop + HEIGHT - 3,
					() -> goToHistoryLocation(history.size() - 1)));
		}
	}

	public <T extends AbstractWidget> T addPageWidget(T widget) {
		return addRenderableWidget(widget);
	}

	// ---------------------------------------------------------------- drawing helpers

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	public Font getFont() {
		return font;
	}

	/** Draws an underlined bold title centered on {@code x}. */
	public void drawTitle(GuiGraphics graphics, String translationKey, int x, int y) {
		Component title = Component.translatable(translationKey).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
		graphics.drawString(font, title, x - font.width(title) / 2, y, 0x000000, false);
	}

	private record BookLocation(BookEntry entry, int page) {
	}
}
