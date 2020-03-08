package ma.inventory.plugin.isbndb;

import java.util.function.Consumer;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

class AuthorEntryHandler extends CommonHandler {

	private final Consumer<AuthorEntry> ins;
	private final AuthorEntry entry = new AuthorEntry();

	private boolean interesting = false;
	private int dsclvl = 0;

	AuthorEntryHandler(Consumer<AuthorEntry> ins) {
		this.ins = ins;
	}

	@Override
	public void startElement(String u, String ln, final String qn,
				final Attributes att) throws SAXException {
		switch(qn) {
		case "rdf:Description":
			String ra = att.getValue("rdf:about");
			if(dsclvl++ == 0 && ra != null)
				entry.reset(ra);
			break;
		case "gndo:preferredNameForTheCorporateBody":
		case "gndo:preferredNameForThePerson":
		case "gndo:personalName":
			interesting = true;
			break;
		}
	}

	@Override
	public void endElement(String u, String ln, final String qn)
							throws SAXException {
		switch(qn) {
		case "rdf:Description":
			if(dsclvl != 0 && --dsclvl == 0 && entry.isUseful())
				ins.accept(entry);
			break;
		case "gndo:preferredNameForTheCorporateBody":
		case "gndo:preferredNameForThePerson":
		case "gndo:personalName":
			if(entry.name == null)
				entry.name = buf.toString().replace("@", "");
			buf.setLength(0);
			interesting = false;
			break;
		}
	}

	@Override
	protected boolean isInteresting() {
		return interesting;
	}

}
