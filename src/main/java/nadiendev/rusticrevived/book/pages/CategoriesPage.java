package nadiendev.rusticrevived.book.pages;

import java.util.List;

import nadiendev.rusticrevived.book.BookCategory;
import nadiendev.rusticrevived.book.BookEntry;
import nadiendev.rusticrevived.book.BookPage;

/**
 * The index page: one animated button per category.
 */
public class CategoriesPage extends BookPage {
	private final List<BookCategory> categories;

	public CategoriesPage(BookEntry entry, List<BookCategory> categories) {
		super(entry);
		this.categories = categories;
	}

	public List<BookCategory> getCategories() {
		return categories;
	}
}
