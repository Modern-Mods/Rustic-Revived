package nadiendev.rusticrevived.book;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.world.item.ItemStack;

/**
 * An Almanac entry: a titled list of pages, optionally belonging to a {@link BookCategory}.
 */
public class BookEntry {
	private final String name;
	@Nullable
	private final BookCategory category;
	private final List<BookPage> pages = new ArrayList<>();
	private ItemStack icon = ItemStack.EMPTY;

	/**
	 * @param name     suffix of the translation key {@code book.rusticrevived.entry.<name>}
	 * @param category the category listing this entry, or null for stand-alone entries (the index)
	 */
	public BookEntry(String name, @Nullable BookCategory category) {
		this.name = "book." + RusticRevived.NAMESPACE + ".entry." + name;
		this.category = category;
		if (category != null) {
			category.addEntry(this);
		}
	}

	/** Translation key of the entry title. */
	public String getName() {
		return name;
	}

	@Nullable
	public BookCategory getCategory() {
		return category;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public BookEntry setIcon(ItemStack icon) {
		this.icon = icon;
		return this;
	}

	public List<BookPage> getPages() {
		return pages;
	}

	public BookEntry addPage(BookPage page) {
		pages.add(page);
		return this;
	}
}
