package ma.inventory.plugin.csv;

import ma.inventory.m.Plugin;
import ma.inventory.m.Verteiler;
import ma.inventory.plugin.csv.export.CSVExport;
import ma.inventory.plugin.csv.import2.CSVImport;

public class CSVPlugin implements Plugin {

	private Verteiler ve = null;
	private CSVExport ce = null;
	private CSVImport ci = null;

	@Override
	public void init(String conf, Verteiler ve) {
		this.ve = ve;
		ve.exports.add(ce = new CSVExport());
		ve.imports.add(ci = new CSVImport(ve.checkpoints));
	}

	@Override
	public String getDefaultConfiguration() {
		return "";
	}

	@Override
	public void close() {
		if(ve != null) {
			if(ce != null)
				ve.exports.remove(ce);
			if(ci != null)
				ve.imports.remove(ci);
		}
	}


}
