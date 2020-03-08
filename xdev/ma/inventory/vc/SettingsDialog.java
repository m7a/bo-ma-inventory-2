package ma.inventory.vc;

import java.util.List;
import java.util.Map;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.event.ActionEvent;

import ma.inventory.m.Verteiler;
import ma.inventory.vc.generic.DescribedProperties;
import ma.inventory.vc.generic.KVE;

class SettingsDialog {

	private final Verteiler v;
	private final Stage dialog;

	private final TreeView<DescribedProperties> confTree;

	private final TreeItem<DescribedProperties> root;
	private final TreeItem<DescribedProperties> db;
	private final TreeItem<DescribedProperties> plugins;

	private final TableView<KVE> selectedSettings;

	SettingsDialog(Stage owner, Verteiler v) { // TODO z PARAM owner not used (remove superflous parameter)
		super();
		this.v = v;
		dialog = new Stage();
		dialog.setTitle("Settings");
		BorderPane rootNode = new BorderPane();

		// Left
		root = mkpti("General");
		root.getChildren().add(db      = mkpti("Database"));
		root.getChildren().add(plugins = mkpti("Plugins"));
		root.setExpanded(true);
		rootNode.setLeft(confTree =
				new TreeView<DescribedProperties>(root));

		// Center
		BorderPane mainPane = new BorderPane();
		selectedSettings = new TableView<KVE>();
		selectedSettings.setEditable(true);
		AppWndCnt.addColumn(selectedSettings, "Key",   "key").
			setCellFactory(TextFieldTableCell.forTableColumn());
		AppWndCnt.addColumn(selectedSettings, "Value", "value").
			setCellFactory(TextFieldTableCell.forTableColumn());
		mainPane.setCenter(selectedSettings);
		Button add = new Button("+");
		add.setOnAction(this::refAdd);
		Button remove = new Button("-");
		remove.setOnAction(this::refRemove);
		mainPane.setBottom(new FlowPane(add, remove));
		rootNode.setCenter(mainPane);
		
		// Bottom
		Button cancel = new Button("Cancel");
		cancel.setOnAction(this::refCancel);
		Button reset = new Button("Reset");
		reset.setOnAction(this::refReset);
		Button ok = new Button("OK");
		ok.setOnAction(this::refOK);
		rootNode.setBottom(new FlowPane(cancel, reset, ok));
		dialog.setScene(new Scene(rootNode));

		confTree.getSelectionModel().selectedItemProperty().addListener(
			(x, p, n) -> {
				if(p != null)
					store(p.getValue());
				if(n != null)
					load(n.getValue());
			}
		);
	}

	private static TreeItem<DescribedProperties> mkpti(String d) {
		return new TreeItem<DescribedProperties>(
						new DescribedProperties(d));
	}

	private void refAdd(ActionEvent ev) {
		selectedSettings.getItems().add(new KVE("(KEY)", "(VALUE)"));
	}

	private void refRemove(ActionEvent ev) {
		selectedSettings.getItems().removeAll(selectedSettings.
					getSelectionModel().getSelectedItems());
	}

	private void refCancel(ActionEvent ev) {
		dialog.hide();
	}

	private void refReset(ActionEvent ev) {
		reset();
	}

	/** KVE -&gt; DescribedProperties */
	private void store(DescribedProperties dp) {
		dp.kv.clear();
		for(KVE kve: selectedSettings.getItems())
			dp.kv.put(kve.getKey(), kve.getValue());
	}

	/** DescribedProperties -&gt; KVE */
	private void load(DescribedProperties dp) {
		selectedSettings.getItems().clear();
		for(Map.Entry<String,String> e: dp.kv.entrySet())
			selectedSettings.getItems().add(new KVE(e.getKey(),
								e.getValue()));
	}

	private void reset() {
		confTree.getSelectionModel().clearSelection();
		selectedSettings.getItems().clear();

		db.getValue().kv.clear();
		root.getValue().kv.clear();
		plugins.getValue().kv.clear();
		for(Map.Entry<Object,Object> e: v.set.properties.entrySet()) {
			String k = e.getKey().toString();
			String v = e.getValue().toString();
			if(k.startsWith("db.")) {
				db.getValue().kv.put(k.substring(3), v);
			} else if(k.startsWith("plugins.")) {
				plugins.getValue().kv.put(k.substring(8), v);
			} else if(k.indexOf('.') == -1) {
				// `plugins` will automatically be calculated
				// from the keys for the ``Plugins'' category...
				if(!k.equals("plugins"))
					root.getValue().kv.put(k, v);
			} else {
				this.v.log.warning("Unknown property key: \"" +
								k + "\"");
			}
		}
	}

	private void refOK(ActionEvent ev) {
		List<TreeItem<DescribedProperties>> si =
				confTree.getSelectionModel().getSelectedItems();
		if(si.size() == 1)
			store(si.get(0).getValue());

		StringBuilder pluginList = new StringBuilder();
		boolean dbchg = hasDBChanged(); 
		v.set.properties.clear();

		// General
		for(Map.Entry<String,String> e: root.getValue().kv.entrySet())
			v.set.properties.put(e.getKey(), e.getValue());

		// DB
		for(Map.Entry<String,String> e: db.getValue().kv.entrySet())
			v.set.properties.put("db." + e.getKey(), e.getValue());

		// Plugins
		for(Map.Entry<String,String> e:
					plugins.getValue().kv.entrySet()) {
			if(pluginList.length() != 0)
				pluginList.append(',');
			pluginList.append(e.getKey());
			v.set.putPluginConfig(e.getKey(), e.getValue());
		}
		v.set.properties.put("plugins", pluginList.toString());

		// Reload
		if(dbchg)
			v.stop(); // Disconnect upon changing DB settings...
		v.loadPlugins();

		v.set.save();
		dialog.hide();
	}

	private boolean hasDBChanged() {
		int numdb = 0;
		for(Map.Entry<Object,Object> e: v.set.properties.entrySet()) {
			String k = e.getKey().toString();
			if(k.startsWith("db.")) {
				numdb++;
				if(!e.getValue().toString().equals(
						db.getValue().kv.get(k)))
					return true;
			}
		}
		return db.getValue().kv.size() != numdb;
	}

	void editSettings() {
		reset();
		dialog.show();
	}

}
