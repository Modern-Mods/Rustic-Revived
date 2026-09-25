package nadiendev.rusticrevived.book;

/**
 * One page of an Almanac entry. Pages only hold data; they are drawn by the client book screen.
 */
public abstract class BookPage {
	protected final BookEntry entry;

	protected BookPage(BookEntry entry) {
		this.entry = entry;
	}

	public BookEntry getEntry() {
		return entry;
	}
}
