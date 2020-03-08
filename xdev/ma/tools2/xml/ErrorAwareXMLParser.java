package ma.tools2.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @version 1.1.0.1
 */
public abstract class ErrorAwareXMLParser extends DefaultHandler {
	
	private final boolean hints;
	
	protected ErrorAwareXMLParser(boolean printHints) {
		super();
		hints = printHints;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		super.warning(ex);
		if(hints)
			saxMessage("SAX generated a warning", ex);
	}
	
	@Override
	public void error(SAXParseException ex) throws SAXException {
		super.error(ex);
		saxMessage("Error", ex);
	}
	
	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		super.fatalError(ex);
		saxMessage("Fatal error", ex);
	}

	private void saxMessage(String prefix, SAXParseException ex) {
		edprintf(ex, "%s while processing the input XML file.\n",
								prefix);
	}
	
	public abstract void edprintf(Exception ex, String fmt, Object...args);
	
	public static SAXParser createParser()
			throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setXIncludeAware(true);
		factory.setNamespaceAware(true);
		return factory.newSAXParser();
	}

}
