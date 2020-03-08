package ma.inventory.plugin.csv.shr;

import javafx.event.ActionEvent;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

import ma.inventory.plugin.csv.generic.FileTextField;

public abstract class AbstractCSVDialog {

	private final Stage dialog;

	final FileTextField file;

	private final StringProperty quotationCharacter;
	private final BooleanProperty processHeader;
	private final ReadOnlyObjectProperty<Toggle> separatorMode;
	private final StringProperty customSeparator;
	private final BooleanProperty qtyLineRepeat;

	private boolean ok;

	protected AbstractCSVDialog() {
		super();
		dialog = new Stage();
		dialog.setTitle("CSV " + getTitleLabel());
		VBox main = new VBox();
		// TODO z SOME MARGIN WANTED BUT IT SHOULD BE EQUAL TO THE MARGIN OF THE OTHER COMPONENTS... ALSO THE LAYOUT WITH SEPARATOR AND QUOTATION IS SLIGHTLY CONFUSING (LITTLE STRUCTURE)
		main.getChildren().add(new TitledPane(getFileLabel(),
						file = makeFileTextField()));

		VBox confT = new VBox();
		CheckBox header = new CheckBox(getColumnHeaderLabel());
		processHeader = header.selectedProperty();
		processHeader.setValue(true);
		confT.getChildren().add(header);
		TextField quotationCharacterC = new TextField();
		quotationCharacter = quotationCharacterC.textProperty();
		// -- separator character --
		ToggleGroup group = new ToggleGroup();
		separatorMode = group.selectedToggleProperty();
		RadioButton custom = new RadioButton("Custom ");
		TextField customText = new TextField(";");
		customSeparator = customText.textProperty();
		custom.setToggleGroup(group);
		custom.setSelected(true);
		RadioButton tab = new RadioButton("Tab");
		tab.setToggleGroup(group);
		separatorMode.addListener((x, o, n) ->
					// == is OK here, we want the instance!
					customText.setDisable(n != custom));
		confT.getChildren().add(new FlowPane(new Label("Separator "),
				custom, customText, new Label(" "), tab));
		// -- end --
		confT.getChildren().add(new FlowPane(
					new Label("Quotation Character "),
					quotationCharacterC));
		CheckBox repeatLinesPerQTYC = new CheckBox(getRepeatQTYLabel());
		qtyLineRepeat = repeatLinesPerQTYC.selectedProperty();
		confT.getChildren().add(repeatLinesPerQTYC);
		main.getChildren().add(new TitledPane("Technical Configuration",
									confT));

		addAdditionalChildren(main);

		Button ok = new Button("OK");
		ok.setOnAction(this::refOK);
		Button cancel = new Button("Cancel");
		cancel.setOnAction(this::refCancel);
		main.getChildren().add(new FlowPane(cancel, ok));

		dialog.setScene(new Scene(main));
	}

	protected abstract String getTitleLabel();
	protected abstract String getFileLabel();
	protected abstract String getColumnHeaderLabel();
	protected abstract String getRepeatQTYLabel();
	protected abstract FileTextField makeFileTextField();

	/** Subclasses may add children here */
	protected abstract void addAdditionalChildren(VBox main);

	private void refOK(ActionEvent ev) {
		ok = true;
		dialog.hide();
	}

	private void refCancel(ActionEvent ev) {
		ok = false;
		dialog.hide();
	}

	/** @return null if not available */
	protected CSVConf configure() {
		ok = false; // Default for close is to assume cancel!
		dialog.showAndWait();
		if(ok)
			return new CSVConf(quotationCharacter.getValue(),
						getSeparator(),
						processHeader.getValue(),
						qtyLineRepeat.getValue());
		else
			return null;
	}

	private String getSeparator() {
		return (((RadioButton)separatorMode.getValue()).getText().
						equals("Tab"))? "\t":
						customSeparator.getValue();
	}

}
