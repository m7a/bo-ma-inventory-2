package ma.inventory.vc;

import javafx.scene.control.TableView;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import ma.inventory.m.MainTableModel;
import ma.inventory.m.MainTableRow;

class MainTableChangeListener implements ChangeListener<MainTableModel> {

	private final TableView<MainTableRow> v;

	MainTableChangeListener(TableView<MainTableRow> v) {
		super();
		this.v = v;
	}

	@Override
	public void changed(ObservableValue<? extends MainTableModel> ov,
				MainTableModel old, MainTableModel newValue) {
		v.getColumns().setAll(newValue.getTableColumns());
		v.setItems(newValue.getContents());
	}

}
