package ma.inventory.m;

import javafx.stage.Stage;

/**
 * <code>toString()</code> needs to return the name.
 */
@FunctionalInterface
public interface Import {

	/**
	 * @return null if user cancelled
	 */
	public Iterable<TableRow> importReturningRows(Stage guiRoot)
							throws Exception;

}
