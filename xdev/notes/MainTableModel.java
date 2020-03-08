package ma.inventory.m;

import java.util.Collection;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;

public class MainTableModel {

	// TODO ....

	//private final 
	//private final ObservableList<MainTableEntry> contents;

	public ObservableList<MainTableRow> getContents() {
		// ...
		return new javafx.beans.property.SimpleListProperty<MainTableRow>();
	}

	public Collection<TableColumn<MainTableRow,String>> getTableColumns() {
		// ...
		return new java.util.ArrayList<TableColumn<MainTableRow,String>>();
	}

}
