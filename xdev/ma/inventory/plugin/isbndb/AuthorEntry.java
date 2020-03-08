package ma.inventory.plugin.isbndb;

class AuthorEntry {

	String author;
	String name;

	void reset(String id) {
		author = id;
		name = null;
	}

	boolean isUseful() {
		return name != null;
	}

}
