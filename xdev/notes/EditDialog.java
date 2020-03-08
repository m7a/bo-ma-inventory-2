package ma.inventory.vc;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

import java.util.List;
import java.util.function.Function;

import ma.inventory.m.*;

class EditDialog {

	// TODO ON FIELD VALUE CHANGE (TAB OUT OF IT OR PRESS ENTER) INVOKE SUGGESTERS

	private static final int MAX_QTY = 0x10000;

	private final Stage dialog;
	private final VBox main;
	private final GridPane[] specificFieldPanels;
	private final StringProperty[][] specificFields;
	private final BooleanProperty[][] keepSpecific;

	private final ObjectProperty<RowType> type;
	private final BooleanProperty keepType;
	private final StringProperty idIn;
	private final ObjectProperty<Integer> qtyIn;

	private MainTableRow trU;
	private Function<MainTableRow,Boolean> callback;

	EditDialog(Stage owner, List<Suggester> suggesters) {
		super();
		dialog = new Stage();
		dialog.initOwner(owner);

		BorderPane rootNode = new BorderPane();

		main = new VBox();

		GridPane general = new GridPane();
		TextField id = new TextField();
		idIn = id.textProperty();
		general.addRow(0, new Label("ID or ISBN"), id);
		Spinner<Integer> qty = new Spinner<Integer>(0, MAX_QTY, 1);
		qtyIn = qty.getValueFactory().valueProperty();
		general.addRow(1, new Label("Quantity"), qty);
		ComboBox<RowType> cb = new ComboBox<RowType>();
		cb.getItems().addAll(RowType.class.getEnumConstants());
		cb.getSelectionModel().select(0);
		type = cb.valueProperty();
		CheckBox keepTypeB = new CheckBox();
		keepType = keepTypeB.selectedProperty();
		general.addRow(2, new Label("Type"), cb, keepTypeB);
		main.getChildren().add(general);

		specificFieldPanels = new GridPane[Constants.FD.length];
		specificFields = new StringProperty[Constants.FD.length][];
		keepSpecific = new BooleanProperty[Constants.FD.length][];
		for(int i = 0; i < Constants.FD.length; i++) {
			specificFieldPanels[i] = new GridPane();
			specificFields[i] = new StringProperty[
							Constants.FD[i].length];
			keepSpecific[i] = new BooleanProperty[
							Constants.FD[i].length];
			for(int j = 0; j < Constants.FD[i].length; j++) {
				TextField tf = new TextField();
				CheckBox ks = new CheckBox();
				specificFields[i][j] = tf.textProperty();
				keepSpecific[i][j] = ks.selectedProperty();
				specificFieldPanels[i].addRow(j, new Label(
						Constants.FD[i][j]), tf, ks);
			}
		}
		// default to books
		main.getChildren().add(specificFieldPanels[0]);

		Button ok = new Button("OK");
		ok.setOnAction(this::applyResult);
		Button cancel = new Button("Cancel");
		cancel.setOnAction(ev -> dialog.close());
		BorderPane okCancel = new BorderPane();
		okCancel.setLeft(ok);
		okCancel.setRight(cancel);
		rootNode.setBottom(okCancel);

		rootNode.setCenter(main);
		dialog.setScene(new Scene(rootNode));

		type.addListener((x, o, n) -> {
			main.getChildren().remove(specificFieldPanels[
								o.ordinal()]);
			main.getChildren().add(specificFieldPanels[
								n.ordinal()]);
		});
	}

	/**
	 * Does not change parameter
	 * @return new MainTableRow with changes applied.
	 */
	void edit(MainTableRow tr, Function<MainTableRow,Boolean> cb) {
		set(tr);
		callback = cb;
		dialog.show();
	}

	private void set(MainTableRow tr) {
		dialog.setTitle(tr == null? "Add": "Edit");

		if(tr == null) {
			if(!keepType.getValue())
				type.set(RowType.BOOK);
		} else {
			type.set(tr.type.getValue());
		}

		trU = new MainTableRow(tr);
		for(int i = 0; i < specificFields.length; i++)
			for(int j = 0; j < specificFields[i].length; j++)
				setSpecificField(tr, i, j);

		idIn.setValue(trU.userSuppliedID.getValue());
		qtyIn.setValue(trU.qty.getValue());
	}

	private void setSpecificField(MainTableRow tr, int i, int j) {
		if(tr != null && tr.type.getValue().ordinal() == i)
			specificFields[i][j].setValue(tr.fields[i][j].
								getValue());
		else if(!keepSpecific[i][j].getValue())
			specificFields[i][j].setValue("");
	}

	private void applyResult(ActionEvent ev) {
		trU.userSuppliedID.setValue(idIn.getValue());
		trU.qty.setValue(qtyIn.getValue());
		trU.type.setValue(type.getValue());

		int ctI = trU.type.getValue().ordinal();
		for(int i = 0; i < Constants.FD[ctI].length; i++)
			trU.fields[ctI][i].setValue(specificFields[ctI][i].
								getValue());

		// TODO VALIDATE (W/ ERROR FEADBACK ETC)
		// TODO (SOMEWHERE ELSE) AUTO COMPLETION

		if(callback.apply(trU))
			set(null);
		else
			dialog.close();
	}

}
