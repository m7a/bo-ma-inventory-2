package ma.inventory.vc;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import ma.inventory.m.MainTableRow;
import ma.inventory.m.Export;
import ma.inventory.m.Verteiler;

class ExportManager extends AbstractManager<Export> {

	private final TableView<MainTableRow> table;

	ExportManager(Verteiler v, Stage wnd, TableView<MainTableRow> table) {
		super(v, wnd);
		this.table = table;
	}

	private Iterable<MainTableRow> getExportRows() {
		ObservableList<MainTableRow> rowsS = table.getSelectionModel().
							getSelectedItems();
		return (rowsS.size() == 0)? v.tableData: rowsS;
	}

	@Override
	public void acceptFailable(Export e) throws Exception {
		e.export(wnd, getExportRows());
	}

	@Override
	public String toString() {
		return "Export";
	}

}
