package nadiendev.rusticrevived.book.pages;

import java.util.List;

import nadiendev.rusticrevived.book.BookCategory;
import nadiendev.rusticrevived.book.BookEntry;
import nadiendev.rusticrevived.book.BookPage;

/**
 * A page of a category: links to (up to twelve of) the category's entries.
 */
public class CategoryPage extends BookPage {
	private final BookCategory category;
	private final List<BookEntry> entries;

	public CategoryPage(BookEntry entry, BookCategory category, List<BookEntry> entries) {
		super(entry);
		this.category = category;
		this.entries = List.copyOf(entries);
	}

	public BookCategory getCategory() {
		return category;
	}

	public List<BookEntry> getEntries() {
		return entries;
	}
}
