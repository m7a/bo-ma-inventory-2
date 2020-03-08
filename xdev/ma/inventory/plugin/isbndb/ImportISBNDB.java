package ma.inventory.plugin.isbndb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ma.tools2.xml.ErrorAwareXMLParser;

public class ImportISBNDB {

	private static final short ISBN_10_LEN    = 32;
	private static final short AUTHOR_LEN     = 512;
	private static final short AUTHOR_LNK_LEN = 128;
	private static final short YEAR_LEN       = 32;
	private static final short PUBLISHER_LEN  = 512;
	private static final short LOCATION_LEN   = 256;

	private final Path dir;

	private ImportISBNDB(Path dir) {
		super();
		this.dir = dir;
	}

	private void run() throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		SAXParser par = factory.newSAXParser();

		System.out.println("BEGIN TRANSACTION;");
		establishTables();

		try(InputStream data = openGZIP("DNBTitel.rdf.gz")) {
			par.parse(data, new BookEntryHandler(
							this::refInsertBook));
		} catch(Exception ex) {
			fail(ex);
		}

		try(InputStream data = openGZIP("GND.rdf.gz")) {
			par.parse(data, new AuthorEntryHandler(
						this::refInsertAuthor));
		} catch(Exception ex) {
			fail(ex);
		}

		reorganize();

		System.out.println("COMMIT;");
	}

	private static void fail(Exception ex) {
		System.out.println("ABORT;");
		ex.printStackTrace();
		System.exit(1);
	}

	private InputStream openGZIP(String name) throws IOException {
		return new GZIPInputStream(Files.newInputStream(dir.resolve(
								name)));
	}

	private void finishInsert() {
		System.out.println(";");
	}

	private static void establishTables() {
		System.out.println("DROP TABLE IF EXISTS books;");
		System.out.println(
			"CREATE TEMPORARY TABLE books_tmp ( " +
			"	isbn13       BIGINT, " +
			"	isbn10       VARCHAR(" + ISBN_10_LEN + "), " + 
			"	title        TEXT, " +
			"	year         VARCHAR(" + YEAR_LEN + "), " +
			"	publisher    VARCHAR(" + PUBLISHER_LEN + "), " +
			"	location     VARCHAR(" + LOCATION_LEN + "), " +
			"	pages        INTEGER " +
			");" +
			"CREATE TABLE books ( " +
			"	isbn13       BIGINT, " +
			"	isbn10       VARCHAR(" + ISBN_10_LEN + "), " + 
			"	author       TEXT, " +
			"	title        TEXT, " +
			"	year         VARCHAR(" + YEAR_LEN + "), " +
			"	publisher    VARCHAR(" + PUBLISHER_LEN + "), " +
			"	location     VARCHAR(" + LOCATION_LEN + "), " +
			"	pages        INTEGER " +
			");" +
			"CREATE TEMPORARY TABLE book_authors ( " +
			"	isbn13       BIGINT, " +
			"	author       VARCHAR(" + AUTHOR_LNK_LEN + ") " +
			");" +
			"CREATE TEMPORARY TABLE authors ( " +
			"	author       VARCHAR(" + AUTHOR_LNK_LEN +
									"), " +
			"	name         VARCHAR(" + AUTHOR_LEN + ") " +
			");"
		);
	}

	private void refInsertBook(DBEntry entry) {
		try {
			System.out.println(
				"INSERT INTO books_tmp (isbn13, isbn10, " +
				"title, year, publisher, location, pages) " +
				"VALUES (" +
				lval(entry.isbn13) + "," +
				strval(entry.isbn10, ISBN_10_LEN) + "," +
				strval(entry.title, Short.MAX_VALUE) + "," +
				strval(entry.year, YEAR_LEN) + "," +
				strval(entry.publisher, PUBLISHER_LEN) + "," +
				strval(entry.location, LOCATION_LEN) + "," +
				lval(entry.pages) + ");"
			);
			if(entry.isbn13 != -1)
				for(String s: entry.authorLinks)
					System.out.println(
						"INSERT INTO book_authors (" +
						"isbn13, author) VALUES (" +
						lval(entry.isbn13) + "," +
						strval(s, AUTHOR_LNK_LEN) + ");"
					);
		} catch(RuntimeException ex) {
			System.err.println("Invalid DB field detected " +
				"(not printed). DBEntry: " + entry.toString());
			ex.printStackTrace();
		}
	}

	private void refInsertAuthor(AuthorEntry entry) {
		try {
			System.out.println(
				"INSERT INTO authors (author, name) VALUES " +
				"(" + strval(entry.author, AUTHOR_LNK_LEN) +
				"," + strval(entry.name, AUTHOR_LEN) + ");"
			);
		} catch(RuntimeException ex) {
			System.err.println("Invalid DB field detected " +
					"(not printed). AuthorEntry: " +
					entry.toString());
			ex.printStackTrace();
		}
	}

	private static String strval(String str, short maxl) {
		if(str == null)
			return "NULL";
		else if(str.length() >= maxl)
			throw new RuntimeException("String length " + maxl +
				" exceeded (got length " + str.length() +
				") for value \"" + str + "\"");
		else
			return ("'" + str.replace("'", "''") + "'");
	}

	private static String lval(long lval) {
		return lval == -1? "NULL": String.valueOf(lval);
	}

	private static void reorganize() {
		System.out.println(
			"INSERT INTO books " +
			"SELECT books_tmp.isbn13, books_tmp.isbn10, " +
				"STRING_AGG(DISTINCT authors.name, '; ') " +
					"AS real_author, " +
				"books_tmp.title, books_tmp.year, " +
				"books_tmp.publisher, books_tmp.location, " +
				"books_tmp.pages " +
			"FROM books_tmp " +
			"NATURAL LEFT JOIN book_authors " +
			"NATURAL LEFT JOIN authors " +
			"GROUP BY books_tmp.isbn13, books_tmp.isbn10, " +
				"books_tmp.title, books_tmp.year, " +
				"books_tmp.publisher, books_tmp.location, " +
				"books_tmp.pages;"
		);

		System.out.println("DROP TABLE book_authors;");
		System.out.println("DROP TABLE books_tmp;");
		System.out.println("DROP TABLE authors;");

		System.out.println("CREATE INDEX idx13 ON books(isbn13);");
		System.out.println("CREATE INDEX idx10 ON books(isbn10);");
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 1) {
			new ImportISBNDB(Paths.get(args[0])).run();
		} else {
			System.err.println("USAGE $0 DIRECTORY");
			System.err.println("Reads DNBTitel.rdf.gz and " +
						"GND.rdf.gz from DIRECTORY.");
			System.exit(1);
		}
	}

}
