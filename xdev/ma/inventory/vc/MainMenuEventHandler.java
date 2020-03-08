package ma.inventory.vc;

import java.util.List;
import java.io.IOException;
import java.io.File;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import ma.tools2.util.ErrorInfo;
import ma.inventory.m.*;

class MainMenuEventHandler implements EventHandler<ActionEvent> {

	private final Verteiler v;
	private final TableView<MainTableRow> table;
	private final Stage wnd;
	private final EditDialog ed;

	MainMenuEventHandler(Verteiler v, TableView<MainTableRow> table,
								Stage stage) {
		super();
		this.v = v;
		this.table = table;
		wnd = stage;
		ed = new EditDialog(stage, v);

		// http://stackoverflow.com/questions/26563390/
		// detect-doubleclick-on-row-of-tableview-javafx
		// It is not the best solution, but the shortest one.
		table.setOnMousePressed(ev -> {
			if(ev.isPrimaryButtonDown() && ev.getClickCount() == 2) 
				editDelete(true);
		});
	}

	@Override
	public void handle(ActionEvent event) {
		String cmd = ((MenuItem)event.getSource()).getText();
		boolean isOpen = false;
		switch(cmd) {
		case "Open...":
			isOpen = true;
		case "Initialize Database...":
			FileChooser fc = new FileChooser();
			File file = isOpen? fc.showOpenDialog(wnd):
							fc.showSaveDialog(wnd);
			if(file != null) {
				// attempt to delete to initialize
				if(!isOpen && file.exists())
					file.delete();
				VMGMT.setDB(file.toString(), v);
			}
			return;
		case "Settings...":
			new SettingsDialog(wnd, v).editSettings();
			return;
		case "Exit":
			wnd.close();
			return;
		case "Add...":
			ed.edit(null, r2 -> handleTryChange(r2, -1));
			return;
		case "New Checkpoint":
			v.log.warning("Menu entry not implemented: " + cmd); // TODO z
			return;
		case "About...":
			Alert alert = new Alert(Alert.AlertType.INFORMATION,
							Constants.COPYRIGHT);
			alert.setHeaderText(null);
			alert.initOwner(wnd);
			alert.showAndWait();
			return;
		case "Edit...":
		case "Delete":
			editDelete(cmd.equals("Edit..."));
			return;
		case "Generate IDs...":
			TextInputDialog tin = new TextInputDialog("1");
			tin.setContentText("Enter the number of IDs to " +
								"generate.");
			tin.initOwner(wnd);
			String sv;
			if((sv = tin.showAndWait().get()) != null) {
				try {
					v.db.generateIDs(Integer.parseInt(sv));
				} catch(RuntimeException ex) {
					v.log.error("Failed to generate IDs " +
							"for input \"" + sv +
							"\".", ex);
				}
			}
			return;
		default:
			throw new RuntimeException("No code to handle menu " +
						"item: \"" + cmd + "\"");
		}
	}

	private void editDelete(boolean isEdit) {
		final int selected = table.getSelectionModel().
							getSelectedIndex();
		if(selected != -1) {
			if(isEdit)
				ed.edit(v.tableData.get(selected), r2 ->
						handleTryChange(r2, selected));
			else
				v.tableData.remove(selected);
		}
	}

	private String handleTryChange(MainTableRow row, int selected) {
		try {
			if(selected == -1) {
				int hasel = v.tableData.lookupID(row.getDBID());
				if(hasel == -1) {
					v.tableData.add(row);
					hasel = v.tableData.size() - 1;
				} else {
					v.tableData.set(hasel, row);
				}
				table.scrollTo(hasel);
				return "";
			} else {
				v.tableData.set(selected, row);
				return null;
			}
		} catch(Exception ex) {
			return ErrorInfo.getStackTrace(ex).toString();
		}
	}

}
