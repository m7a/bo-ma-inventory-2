package ma.inventory.plugin.csv.export;

import java.io.BufferedWriter;
import java.io.IOException;

import ma.inventory.m.MainTableRow;
import ma.inventory.m.Constants;
import ma.inventory.m.RowType;
import ma.inventory.plugin.csv.shr.ConcatenatedColumnNames;

class CSVExportLogic {

	private final BufferedWriter w;
	private final Iterable<MainTableRow> rows;
	private final CSVExportConf cnf;

	CSVExportLogic(BufferedWriter w, Iterable<MainTableRow> rows,
							CSVExportConf cnf) {
		super();
		this.w = w;
		this.rows = rows;
		this.cnf = cnf;
	}

	void run() throws IOException {
		Object[] ln = new Object[cnf.getNumberOfSelectedColumns()];
		if(cnf.general.processHeader) {
			makeLabelLine(ln);
			writeLine(w, ln);
		}
		for(MainTableRow r: rows) {
			makeContentLine(ln, r);
			if(cnf.general.lineQTYRepeat) {
				for(int i = 0; i < r.getQTY(); i++)
					writeLine(w, ln);
			} else {
				writeLine(w, ln);
			}
		}
	}

	private void makeLabelLine(Object[] data) {
		String[] lbl = cnf.getColumnLabels();
		assert(lbl.length == data.length);
		System.arraycopy(lbl, 0, data, 0, lbl.length);
	}

	private void writeLine(BufferedWriter w, Object[] ln)
							throws IOException {
		for(int i = 0; i < ln.length; i++) {
			if(i != 0)
				w.write(cnf.general.separator);

			if(cnf.general.hasQuot() && ln[i] instanceof String) {
				String qchr = cnf.general.quotationCharacter;
				w.write(qchr);
				// escape output by repeating quote character
				// "" for a " in a "-quoted line.
				w.write(ln[i].toString().replace(qchr,
								qchr + qchr));
				w.write(qchr);
			} else {
				w.write(ln[i].toString());
			}
		}
		w.newLine();
	}

	private void makeContentLine(Object[] data, MainTableRow r) {
		boolean[] colsel = cnf.getSelectedColumns();
		int pos = 0;
		// TODO z THIS WAY, ALL (POTENTIALLY NUMERIC) IDS are treated as text...
		if(colsel[ConcatenatedColumnNames.COL_ID_IDX])
			data[pos++] = r.getUserSuppliedID();
		if(colsel[ConcatenatedColumnNames.COL_PREV_QTY_IDX])
			data[pos++] = r.getPrevQTY();
		if(colsel[ConcatenatedColumnNames.COL_QTY_IDX])
			data[pos++] = r.getQTY();
		if(colsel[ConcatenatedColumnNames.COL_TYPE_IDX])
			// explicit toString as to generate quotation later
			data[pos++] = r.getType().toString();


		int flatidx = ConcatenatedColumnNames.PRE_EXPORT_COL.length;

		for(RowType t: RowType.values()) {
			for(int j = 0; j < Constants.FD[t.ordinal()].length;
									j++) {
				if(colsel[flatidx])
					data[pos++] = r.getField(t, j);
				flatidx++;
			}
		}

		assert(pos == data.length);
	}

}
