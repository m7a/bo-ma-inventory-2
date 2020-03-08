package ma.inventory.plugin.csv.export;

import ma.inventory.plugin.csv.shr.CSVConf;
import ma.inventory.plugin.csv.shr.ConcatenatedColumnNames;

class CSVExportConf {

	final CSVConf general;

	private String[] columnLabels;
	private boolean[] selectedColumns;

	CSVExportConf(CSVConf general) {
		super();
		this.general = general;
		selectedColumns = new boolean[ConcatenatedColumnNames.
						ALL_COLUMN_LABELS.length];
		for(int i = 0; i < selectedColumns.length; i++)
			selectedColumns[i] = false;
	}

	void declareNumberOfColumnsSelected(int num) {
		columnLabels = new String[num];
	}

	void declareSelected(int col) {
		selectedColumns[col] = true;
	}

	void calculateLabels() {
		int ci = 0;
		for(int i = 0; i < selectedColumns.length; i++)
			if(selectedColumns[i])
				columnLabels[ci++] = ConcatenatedColumnNames.
							ALL_COLUMN_LABELS[i];
	}

	int getNumberOfSelectedColumns() {
		return columnLabels.length;
	}

	String[] getColumnLabels() {
		return columnLabels;
	}

	boolean[] getSelectedColumns() {
		return selectedColumns;
	}

}
