package ma.inventory.plugin.csv.export;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.BooleanProperty;

import ma.inventory.m.Constants;
import ma.inventory.plugin.csv.generic.FileTextField;
import ma.inventory.plugin.csv.shr.AbstractCSVDialog;
import ma.inventory.plugin.csv.shr.ConcatenatedColumnNames;
import ma.inventory.plugin.csv.shr.CSVConf;

class CSVExportDialog extends AbstractCSVDialog {

	private BooleanProperty[] columnSelection;

	CSVExportDialog() {
		super();
	}

	@Override
	protected String getTitleLabel() {
		return "Export";
	}

	@Override
	protected String getFileLabel() {
		return "Target File";
	}

	@Override
	protected String getColumnHeaderLabel() {
		return "Export Column Headers";
	}

	@Override
	protected String getRepeatQTYLabel() {
		return "Repeat lines quantity times.";
	}

	@Override
	protected FileTextField makeFileTextField() {
		return new FileTextField(null, false, false);
	}

	@Override
	protected void addAdditionalChildren(VBox main) {
		VBox colConf = new VBox();
		GridPane colSel = new GridPane();
		BooleanProperty[] colSelProp = new BooleanProperty[
			ConcatenatedColumnNames.ALL_COLUMN_LABELS.length];
		for(int i = 0; i < ConcatenatedColumnNames.
						ALL_COLUMN_LABELS.length; i++) {
			String s = ConcatenatedColumnNames.ALL_COLUMN_LABELS[i];
			CheckBox cbx = new CheckBox(s + "  ");
			GridPane.setColumnIndex(cbx, i % 3);
			GridPane.setRowIndex(   cbx, i / 3);
			cbx.setSelected(!(i < ConcatenatedColumnNames.
						PRE_EXPORT_COL.length &&
					s.equals(Constants.COL_PREV_QTY)));
			colSel.getChildren().add(cbx);
			colSelProp[i] = cbx.selectedProperty();
		}
		columnSelection = colSelProp;
		FlowPane colMod = new FlowPane();
		String[] lbl = new String[] { "Select all", "Deselect all" };
		for(int i = 0; i < lbl.length; i++) {
			final int i2 = i;
			Button selectAll = new Button(lbl[i]);
			selectAll.setOnAction(x -> {
				for(BooleanProperty p: columnSelection)
					p.setValue(i2 == 0);
			});
			colMod.getChildren().add(selectAll);
		}
		colConf.getChildren().addAll(colSel, colMod);
		main.getChildren().add(new TitledPane("Column Selection",
								colConf));
	}

	/** @return null if cancelled */
	CSVExportConf configureExport() {
		CSVConf general = configure();
		if(general == null) {
			return null;
		} else {
			CSVExportConf ret = new CSVExportConf(general);
			for(int mode = 0; mode <= 1; mode++)
				procselMode(mode, ret);
			if(general.processHeader)
				ret.calculateLabels();
			return ret;
		}
	}

	private void procselMode(int mode, CSVExportConf ret) {
		int numsel = 0;
		for(int i = 0; i < columnSelection.length; i++) {
			if(columnSelection[i].getValue()) {
				switch(mode) {
				case 0: numsel++; break;
				case 1: ret.declareSelected(i); break;
				}
			}
		}
		if(mode == 0)
			ret.declareNumberOfColumnsSelected(numsel);
	}

}
