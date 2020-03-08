package ma.inventory.plugin.csv.import2;

// TODO CSTAT SUBSTAT IMPLEFMENT / NOCH NICHT SO GANZ AUSGEREIFT: SOLLTE DAS JETZT DIREKT TABLE ROW IMPLEMENTIEREN UND DANN DIE HÄLFTE OVN STATIC TABLE ROW KOPIEREN ODER BEKOMMEN DIE BEIDEN EINE GEMEINSAME ÜBERKLASSE?
class ImportTableRow /*extends TableRow*/ extends ma.inventory.m.StaticTableRow {

	public ImportTableRow(RevLookupStr rls, String[] header,
							String[] fields) {
		super(null); // TODO z MUST FAIL BUT SEE ABOVE ABOUT WHAT IS ACTUALLY NEEDED TO BE DONE...
	}

}
