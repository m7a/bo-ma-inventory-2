package ma.inventory.plugin.csv.shr;

import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public abstract class AbstractCSVLogic<T extends AbstractCSVDialog, V> {

	private final T dialog;

	protected AbstractCSVLogic() {
		super();
		dialog = instantiateDialog();
	}

	protected abstract T instantiateDialog();

	protected abstract V configure(T dialog);

	protected abstract String getInvalidSelectionLabel();

	protected Path getFile() {
		return dialog.file.getSelection();
	}

	/** @return null if not succeeded */
	protected V getConf(Stage guiRoot) {
		V v = configure(dialog);
		if(v != null && !dialog.file.hasValidSelection()) {
			Alert alert = new Alert(Alert.AlertType.WARNING,
						getInvalidSelectionLabel());
			alert.setHeaderText(null);
			alert.initOwner(guiRoot);
			alert.showAndWait();
			return null;
		} else {
			return v;
		}
	}

	/** For making this an Import or Export menu entry. */
	@Override
	public String toString() {
		return "CSV...";
	}

}
