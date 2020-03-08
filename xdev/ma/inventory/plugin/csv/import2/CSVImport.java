package ma.inventory.plugin.csv.import2;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.nio.file.Files;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import ma.inventory.m.TableRow;
import ma.inventory.m.Import;
import ma.inventory.m.Checkpoint;
import ma.inventory.plugin.csv.shr.AbstractCSVLogic;
import ma.inventory.plugin.csv.shr.ConcatenatedColumnNames;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CSVImport extends AbstractCSVLogic<CSVImportDialog, CSVImportConf>
							implements Import {

	private final ObservableList<Checkpoint> checkpoints;

	/**
	 * @param checkpoints from Verteiler, only ever read (+ listeners)
	 */
	public CSVImport(ObservableList<Checkpoint> checkpoints) {
		super();
		this.checkpoints = checkpoints;
	}

	@Override
	protected CSVImportDialog instantiateDialog() {
		return new CSVImportDialog(checkpoints);
	}

	@Override
	protected CSVImportConf configure(CSVImportDialog dialog) {
		return dialog.configureImport();
	}

	@Override
	public Iterable<TableRow> importReturningRows(Stage guiRoot)
							throws Exception {
		CSVImportConf cnfs = getConf(guiRoot);
		if(cnfs == null)
			return null;

		String[] expectedHeader = null;
		RevLookupStr rev = new RevLookupStr();

		try(BufferedReader in = Files.newBufferedReader(getFile(),
								UTF_8)) {
			List<TableRow> resultRows = new ArrayList<TableRow>();
			String line;
			while((line = in.readLine()) != null) {
				String[] fields = splitCSVLine(cnfs, line);
				if(expectedHeader == null) {
					if(cnfs.general.processHeader)
						expectedHeader = fields;
					else
						expectedHeader =
							ConcatenatedColumnNames.
							ALL_COLUMN_LABELS;
				} else {
					resultRows.add(new ImportTableRow(rev,
						expectedHeader, fields));
				}
			}

			return resultRows;
		}
	}

	private static String[] splitCSVLine(CSVImportConf conf, String line) {
		return conf.general.hasQuot()?
			new CSVAutomaton(conf.general.quotationCharacter,
					conf.general.separator).run(line):
			line.split(conf.general.separator);
	}

	@Override
	protected String getInvalidSelectionLabel() {
		return "Could not import the selected file because the " +
			"assigned text field contains an invalid path. " +
			"Solve this by pressing `...` to select a file " +
			"before running the import.";
	}

}
