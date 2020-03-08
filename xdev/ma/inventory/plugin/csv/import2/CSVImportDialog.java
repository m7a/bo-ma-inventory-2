package ma.inventory.plugin.csv.import2;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import tornadofx.control.DateTimePicker;

import ma.inventory.m.Checkpoint;
import ma.inventory.plugin.csv.generic.FileTextField;
import ma.inventory.plugin.csv.shr.CSVConf;
import ma.inventory.plugin.csv.shr.AbstractCSVDialog;

class CSVImportDialog extends AbstractCSVDialog {

	private final ObservableList<Checkpoint> checkpoints;

	private ReadOnlyObjectProperty<Toggle> inventoryDateMode;

	/**
	 * Compare inventory date mode against this to find out if the
	 * toggle button for an existing Checkpoint is selected.
	 */
	private Object cmpSelExisting;

	/** Value for an existing Checkpoint. */
	private ObjectProperty<Checkpoint> checkpointSel;

	/** Value for a new Checkpoint */
	private ObjectProperty<LocalDateTime> calSel;

	/**
	 * @param checkpoints only ever accessed in a reading fashion
	 *		(yet probably registering listeners at it)
	 */
	CSVImportDialog(ObservableList<Checkpoint> checkpoints) {
		super();
		this.checkpoints = checkpoints;
	}

	@Override
	protected String getTitleLabel() {
		return "Import";
	}

	@Override
	protected String getFileLabel() {
		return "Source File";
	}

	@Override
	protected String getColumnHeaderLabel() {
		return "Interpret first row as column headers";
	}

	@Override
	protected String getRepeatQTYLabel() {
		return "Repeated lines increment QTY " +
					"(otherwise causes duplicate entries).";
	}

	@Override
	protected FileTextField makeFileTextField() {
		return new FileTextField(null, false, true);
	}

	@Override
	protected void addAdditionalChildren(VBox main) {
		// -- Pre --
		GridPane dateConf = new GridPane();
		ToggleGroup group = new ToggleGroup();
		inventoryDateMode = group.selectedToggleProperty();

		// -- For existing inventory dates --
		final RadioButton existing = new RadioButton(
						"Existing inventory date ");
		cmpSelExisting = existing;
		existing.setToggleGroup(group);
		existing.setSelected(true);

		final ComboBox<Checkpoint> existingCheckpoints =
					new ComboBox<Checkpoint>(checkpoints);
		checkpointSel = existingCheckpoints.valueProperty();
		dateConf.addRow(1, existing, existingCheckpoints);

		// -- For new inventory dates --
		final RadioButton newDate =
					new RadioButton("New inventory date ");
		newDate.setToggleGroup(group);

		final DateTimePicker dp = new DateTimePicker();
		dp.setDisable(true);
		calSel = dp.dateTimeValueProperty();
		dateConf.addRow(2, newDate, dp);

		// -- Post --
		inventoryDateMode.addListener((x, o, n) -> {
			// == is OK here, we want the instance!
			existingCheckpoints.setDisable(o == existing);
			dp.setDisable(o == newDate);
		});
		main.getChildren().add(new TitledPane("Inventory Date",
								dateConf));
	}

	CSVImportConf configureImport() {
		CSVConf general = configure();
		return general == null? null: new CSVImportConf(general,
			(inventoryDateMode.getValue() == cmpSelExisting)?
				checkpointSel.getValue():
				new Checkpoint(zLocalDateTimeToJUDate(
							calSel.getValue())));
	}

	/**
	 * Copied from http://stackoverflow.com/questions/19431234/
	 * converting-between-java-time-localdatetime-and-java-util-date
	 */
	private static Date zLocalDateTimeToJUDate(LocalDateTime ldt) {
		return Date.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()).
								toInstant());
	}

}
