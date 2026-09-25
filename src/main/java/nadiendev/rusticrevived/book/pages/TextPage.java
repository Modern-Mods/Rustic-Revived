package nadiendev.rusticrevived.book.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.book.BookEntry;
import nadiendev.rusticrevived.book.BookPage;

/**
 * A page of text paragraphs ({@code book.rusticrevived.text.<key>}) with ribbons linking to related entries.
 */
public class TextPage extends BookPage {
	private final List<String> textKeys = new ArrayList<>();
	private final Set<BookEntry> relatedEntries = new LinkedHashSet<>();

	public TextPage(BookEntry entry, String... textKeys) {
		super(entry);
		for (String textKey : textKeys) {
			this.textKeys.add("book." + RusticRevived.NAMESPACE + ".text." + textKey);
		}
	}

	/** Translation keys of the paragraphs. */
	public List<String> getTextKeys() {
		return textKeys;
	}

	public Set<BookEntry> getRelatedEntries() {
		return relatedEntries;
	}

	/** Adds related entries; null entries (disabled in the config) are skipped. */
	public TextPage related(@Nullable BookEntry... entries) {
		for (BookEntry related : entries) {
			if (related != null) {
				relatedEntries.add(related);
			}
		}
		return this;
	}
}
