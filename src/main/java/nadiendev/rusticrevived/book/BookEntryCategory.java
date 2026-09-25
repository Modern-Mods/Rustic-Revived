package nadiendev.rusticrevived.book;

import java.util.ArrayList;
import java.util.List;

import nadiendev.rusticrevived.book.pages.CategoryPage;

/**
 * The entry of a {@link BookCategory}: its pages list the category's entries, twelve per page.
 */
public class BookEntryCategory extends BookEntry {
	private static final int ENTRIES_PER_PAGE = 12;

	private final BookCategory entryCategory;

	BookEntryCategory(BookCategory category) {
		super("", null);
		this.entryCategory = category;
	}

	@Override
	public String getName() {
		return entryCategory.getName();
	}

	public BookCategory getEntryCategory() {
		return entryCategory;
	}

	@Override
	public List<BookPage> getPages() {
		List<BookEntry> entries = entryCategory.getEntries();
		List<BookPage> pages = new ArrayList<>();
		for (int start = 0; start < entries.size(); start += ENTRIES_PER_PAGE) {
			pages.add(new CategoryPage(this, entryCategory, entries.subList(start, Math.min(start + ENTRIES_PER_PAGE, entries.size()))));
		}
		return pages;
	}
}
