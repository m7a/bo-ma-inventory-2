package ma.inventory.vc;

import javafx.stage.Stage;
import ma.inventory.m.Verteiler;
import ma.inventory.m.Import;
import ma.inventory.m.TableRow;

class ImportManager extends AbstractManager<Import> {

	public ImportManager(Verteiler v, Stage wnd) {
		super(v, wnd);
	}

	@Override
	public void acceptFailable(Import e) throws Exception {
		Iterable<TableRow> rv = e.importReturningRows(wnd);
		// TODO ...
	}

}
