package nadiendev.rusticrevived.book;

import java.util.ArrayList;
import java.util.List;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.resources.ResourceLocation;

/**
 * A top level section of the Almanac (decoration, agriculture, production). Its entries are listed
 * on the pages of its {@link BookEntryCategory}.
 */
public class BookCategory {
	private final String name;
	private final ResourceLocation icon;
	private final List<BookEntry> entries = new ArrayList<>();
	private final BookEntryCategory categoryEntry;

	/**
	 * @param name suffix of the translation key {@code book.rusticrevived.category.<name>}
	 * @param icon animated icon texture (32 x 256, eight 32 x 32 frames stacked vertically)
	 */
	public BookCategory(String name, ResourceLocation icon) {
		this.name = "book." + RusticRevived.NAMESPACE + ".category." + name;
		this.icon = icon;
		this.categoryEntry = new BookEntryCategory(this);
	}

	/** Translation key of the category title. */
	public String getName() {
		return name;
	}

	public ResourceLocation getIcon() {
		return icon;
	}

	public List<BookEntry> getEntries() {
		return entries;
	}

	void addEntry(BookEntry entry) {
		entries.add(entry);
	}

	/** The entry listing every entry of this category. */
	public BookEntryCategory getCategoryEntry() {
		return categoryEntry;
	}
}
