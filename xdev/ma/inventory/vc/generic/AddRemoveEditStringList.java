package ma.inventory.vc.generic;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.event.ActionEvent;

public class AddRemoveEditStringList extends BorderPane {

	private static final String NEWVAL_MARKER = "(Double Click to Edit)";

	private final EditableStringListView lv;

	public AddRemoveEditStringList() {
		this(null);
	}

	public AddRemoveEditStringList(ObservableList<String> values) {
		super();
		setCenter(lv = (values == null?
					new EditableStringListView():
					new EditableStringListView(values)));
		Button add = new Button("+");
		add.setOnAction(this::refAdd);
		Button remove = new Button("-");
		remove.setOnAction(this::refRemove);
		GridPane buttons = new GridPane();
		buttons.addColumn(1, add, remove);
		setRight(buttons);
	}

	private void refAdd(ActionEvent ev) {
		lv.getItems().add(NEWVAL_MARKER);
	}

	private void refRemove(ActionEvent ev) {
		lv.getItems().removeAll(lv.getSelectionModel().
							getSelectedItems());
	}

	public ObservableList<String> getValues() {
		return lv.getItems();
	}

}
