package ma.inventory.vc;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import ma.inventory.m.Constants;
import ma.inventory.m.Verteiler;
import ma.inventory.m.MainTableRow;
import ma.inventory.m.RowType;

public class AppWndCnt extends BorderPane {

	private final MainMenuBar men;
	private final TableView<MainTableRow> table;
	private final Label status;

	public AppWndCnt(Verteiler v, Stage stage) {
		super();
		setBottom(status = new Label());
		setCenter(table = createTable(v));
		setTop(men = new MainMenuBar(
			new MainMenuEventHandler(v, table, stage),
			new ExportManager(v, stage, table),
			new ImportManager(v, stage),
			null // TODO HANDLER TBD. IT CAN NOT BE NULL! CSTAT IMPLEMENT MUTLIPLE EXPORT TIME POINTS ETC.
		));
		v.exports.addListener(men.exportCL);
		v.imports.addListener(men.importCL);
		v.checkpoints.addListener(men.checkpointCL);
	}

	private static TableView<MainTableRow> createTable(Verteiler v) {
		TableView<MainTableRow> table = new TableView<MainTableRow>(
								v.tableData);
		table.getSelectionModel().setSelectionMode(
							SelectionMode.MULTIPLE);

		addColumn(table, Constants.COL_ID,       "userSuppliedID");
		addColumn(table, Constants.COL_PREV_QTY, "prevQTY");
		addColumn(table, Constants.COL_QTY,      "qty");
		addColumn(table, Constants.COL_TYPE,     "type");

		for(int i = 0; i < Constants.FD.length; i++)
			for(int j = 0; j < Constants.FD[i].length; j++)
				addDynamicFieldColumn(table, i, j);

		return table;
	}

	static <V> TableColumn<V,String> addColumn(TableView<V> table,
						String visual, String fn) {
		TableColumn<V,String> col = new TableColumn<V,String>(visual);
		col.setCellValueFactory(new PropertyValueFactory<V,String>(fn));
		table.getColumns().add(col);
		return col;
	}

	private static void addDynamicFieldColumn(TableView<MainTableRow> table,
						final int i, final int j) {
		TableColumn<MainTableRow,String> col = new TableColumn<
				MainTableRow,String>(Constants.FD[i][j]);
		col.setCellValueFactory(p -> p.getValue().fields[i][j]);
		table.getColumns().add(col);
	}

	void setStatus(String s) {
		status.setText(s);
	}

}
