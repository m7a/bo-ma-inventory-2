package ma.inventory.plugin.isbndb;

import java.util.function.Consumer;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

class BookEntryHandler extends CommonHandler {

	private final Consumer<DBEntry> ins;
	private final DBEntry entry = new DBEntry();

	private Consumer<String> assignToField = null;
	private int dsclvl = 0;
	private boolean author = false;

	BookEntryHandler(Consumer<DBEntry> ins) {
		super();
		this.ins = ins;
	}

	@Override
	public void startElement(String u, String ln, final String qn,
				final Attributes att) throws SAXException {
		switch(qn) {
		case "rdf:Description":
			if(dsclvl++ == 0)
				entry.reset();
			break;
		case "bibo:isbn13": assignToField = entry::refSetISBN13; break;
		case "bibo:isbn10": assignToField = entry::refSetISBN10; break;
		case "dc:title":    assignToField = entry::refSetTitle; break;
		case "dcterms:issued": assignToField = entry::refSetYear; break;
		case "dc:publisher":
			assignToField = entry::refSetPublisher; break;
		case "rdau:P60163":
			assignToField = entry::refSetLocation; break;
		case "isbd:P1053": assignToField = entry::refSetPages; break;
		case "bibo:authorList": author = true; break;
		default:
			if(author && qn.startsWith("rdf:_"))
				entry.addAuthorLink(
						att.getValue("rdf:resource"));
		}
	}

	@Override
	public void endElement(String u, String ln, final String qn)
							throws SAXException {
		switch(qn) {
		case "rdf:Description":
			if(--dsclvl == 0 && entry.isUseful())
				ins.accept(entry);
			break;
		case "bibo:isbn13":
		case "bibo:isbn10":
		case "dc:title":
		case "dcterms:issued":
		case "dc:publisher":
		case "rdau:P60163":
		case "isbd:P1053":
			if(assignToField != null) {
				String val = buf.toString().trim();
				try {
					assignToField.accept(val);
				} catch(RuntimeException ex) {
					System.err.println("Failed to assign " +
						"to field (value=\"" + val +
						"\", element=" + qn + ")");
					ex.printStackTrace();
				}
				buf.setLength(0);
				assignToField = null;
			}
			break;
		case "bibo:authorList":
			author = false;
			break;
		}
	}

	@Override
	protected boolean isInteresting() {
		return assignToField != null;
	}

}
