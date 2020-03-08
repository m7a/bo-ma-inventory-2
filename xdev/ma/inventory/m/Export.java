package ma.inventory.m;

import javafx.stage.Stage;

/**
 * <code>toString()</code> needs to return the name.
 */
@FunctionalInterface
public interface Export {

	public void export(Stage guiRoot, Iterable<MainTableRow> rows)
							throws Exception;

}
