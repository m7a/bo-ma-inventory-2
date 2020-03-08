package ma.inventory.plugin.isbndb;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.xml.ErrorAwareXMLParser;

abstract class CommonHandler extends ErrorAwareXMLParser {

	protected final StringBuilder buf = new StringBuilder();

	protected abstract boolean isInteresting();

	public CommonHandler() {
		super(true);
	}

	@Override
	public void characters(char[] c, int s0, int len) throws SAXException {
		if(isInteresting())
			buf.append(c, s0, len);
	}

	@Override
	public void edprintf(Exception ex, String fmt, Object... args) {
		System.err.println(String.format(fmt, args));
		ex.printStackTrace();
	}

}
