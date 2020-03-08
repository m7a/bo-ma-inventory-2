package ma.inventory.m;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.scene.Node;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class Verteiler {

	public final Log log;
	public final Settings set; 
	public final MainTableData tableData;
	public final ObservableList<Checkpoint> checkpoints;
	public final DataTransmitter db;

	/** plugins may register here */
	public final List<Suggester> suggesters;
	public final ObservableList<Export> exports;
	public final ObservableList<Import> imports;

	/**
	 * Supplies icons for control buttons or null for no icon.
	 * One plugin may fill this property. If possible, the function should
	 * use a Cache because it is probably being called often.
	 */
	public final ObjectProperty<Function<Character,Node>> buttonIcons;

	Connection dbConn; // only access from VMGMT
	private final List<Plugin> plugins;

	public Verteiler() {
		super();
		log = new Log();
		set = new Settings(log);
		tableData = new MainTableData();
		checkpoints = FXCollections.observableArrayList();
		db = new DataTransmitter(log, tableData, checkpoints);
		suggesters = new ArrayList<Suggester>();
		plugins = new ArrayList<Plugin>();
		buttonIcons = new SimpleObjectProperty<Function<Character,
							Node>>(c -> null);
		exports = FXCollections.observableArrayList();
		imports = FXCollections.observableArrayList();
	}

	/** May also be used for reload */
	public void loadPlugins() {
		VMGMT.loadPlugins(plugins, this);
	}

	public void stop() {
		VMGMT.unloadPlugins(plugins, this);
		plugins.clear();
		if(dbConn != null) {
			try {
				dbConn.close();
			} catch(SQLException ex) {
				log.error(
					"Failed to close database connection " +
					"(inventory database). This might " +
					"have caused data loss. Please " +
					"verify all stored data is correct " +
					"and complete.", ex
				);
			}
		}
	}

}
