package ma.inventory.plugin.isbndb;

import java.util.LinkedHashSet;
import java.util.Arrays;

class DBEntry {

	long isbn13;
	String isbn10;
	String author;
	String title;
	String year;
	String publisher;
	String location;
	int pages;

	final LinkedHashSet<String> authorLinks = new LinkedHashSet<String>();

	void reset() {
		isbn13 = -1;
		isbn10 = null;
		author = null;
		title = null;
		year = null;
		publisher = null;
		location = null;
		pages = -1;
		authorLinks.clear();
	}

	void refSetISBN13(String isbn13) {
		if(this.isbn13 == -1)
			this.isbn13 = Long.parseLong(isbn13);
	}

	void refSetISBN10(String isbn10) {
		if(this.isbn10 == null)
			this.isbn10 = isbn10;
	}

	void refSetTitle(String title) {
		if(this.title == null)
			this.title = title.replace("@", "");
	}

	void refSetYear(String year) {
		if(this.year == null)
			this.year = year;
	}

	void refSetPublisher(String publisher) {
		if(this.publisher == null)
			this.publisher = publisher;
	}

	void refSetLocation(String location) {
		if(this.location == null)
			this.location = location;
	}

	void refSetPages(String pages) {
		if(this.pages == -1) {
			if(pages.matches("^[0-9]+$"))
				this.pages = Integer.parseInt(pages);
			else if(pages.matches("^[0-9]+ S.$"))
				this.pages = Integer.parseInt(pages.substring(0,
							pages.length() - 3));
		}
	}

	void addAuthorLink(String raw) {
		if(!authorLinks.contains(raw))
			authorLinks.add(raw);
	}

	@Override
	public String toString() {
		return "isbn13=" + isbn13 + ",isbn10=" + isbn10 + ",author=" +
				author + ",title=" + title + ",year=" + year +
				",publisher=" + publisher + ",location=" +
				location + ",pages=" + pages + ",authors=" +
				Arrays.toString(authorLinks.toArray());
	}

	boolean isUseful() {
		return title != null || isbn13 != -1;
	}

}
