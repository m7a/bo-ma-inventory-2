package ma.inventory.vc.generic;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;

/**
 * Inspired by https://gist.github.com/tarrsalah/5492452
 */
class EditableStringListView extends ListView<String> {

	EditableStringListView() {
		super();
		configure();
	}

	EditableStringListView(ObservableList<String> values) {
		super(values);
		configure();
	}

	private void configure() {
		setEditable(true);
		setCellFactory(TextFieldListCell.forListView());
		setOnEditCommit(t -> getItems().set(t.getIndex(),
							t.getNewValue()));
	}

}
