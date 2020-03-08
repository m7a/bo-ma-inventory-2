package ma.inventory.plugin.csv.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

import javafx.stage.Stage;

import ma.inventory.m.Export;
import ma.inventory.m.MainTableRow;
import ma.inventory.plugin.csv.shr.AbstractCSVLogic;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CSVExport extends AbstractCSVLogic<CSVExportDialog, CSVExportConf>
							implements Export {

	@Override
	protected CSVExportDialog instantiateDialog() {
		return new CSVExportDialog();
	}

	@Override
	protected CSVExportConf configure(CSVExportDialog dialog) {
		return dialog.configureExport();
	}

	@Override
	public void export(Stage guiRoot, Iterable<MainTableRow> rows)
							throws Exception {
		CSVExportConf cnfs = getConf(guiRoot);
		if(cnfs != null) {
			try(BufferedWriter w = Files.newBufferedWriter(
							getFile(), UTF_8)) {
				new CSVExportLogic(w, rows, cnfs).run();
			}
		}
	}

	@Override
	protected String getInvalidSelectionLabel() {
		return "Could not export to selected file because the " +
			"assigned text field contains an invalid path. " +
			"Solve this by pressing `...` to select a file " +
			"before starting the export.";
	}

}
