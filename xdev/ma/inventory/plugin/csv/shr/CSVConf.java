package ma.inventory.plugin.csv.shr;

public class CSVConf {

	public final String quotationCharacter;
	public final String separator;
	public final boolean processHeader;
	public final boolean lineQTYRepeat;

	CSVConf(String quotationCharacter, String separator,
				boolean processHeader, boolean lineQTYRepeat) {
		this.quotationCharacter = quotationCharacter;
		this.separator          = separator;
		this.processHeader      = processHeader;
		this.lineQTYRepeat      = lineQTYRepeat;
	}

	public boolean hasQuot() {
		return quotationCharacter.length() != 0;
	}

}
