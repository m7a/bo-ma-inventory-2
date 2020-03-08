package ma.inventory.vc;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.collections.ListChangeListener;

import java.util.function.Consumer;

import ma.inventory.vc.generic.MutableMenu;
import ma.inventory.vc.generic.MutableRadioMenu;
import ma.inventory.m.Export;
import ma.inventory.m.Import;
import ma.inventory.m.Checkpoint;

public class MainMenuBar extends MenuBar {

	final ListChangeListener<Export>     exportCL;
	final ListChangeListener<Import>     importCL;
	final ListChangeListener<Checkpoint> checkpointCL;

	MainMenuBar(MainMenuEventHandler h, Consumer<Export> exportCB,
					Consumer<Import> importCB,
					Consumer<Checkpoint> checkpointCB) {
		super();
		MutableMenu<Checkpoint> cm = mkmen(
				new MutableRadioMenu<Checkpoint>("Checkpoints",
				checkpointCB));
		MutableMenu<Export> em = mkmen(new MutableMenu<Export>("Export",
								exportCB));
		MutableMenu<Import> im = mkmen(new MutableMenu<Import>("Import",
								importCB));
		getMenus().addAll(
			mkmen("File",
				i(h, "Initialize Database..."),
				i(h, "Open...", 'o'),
				i(h, "Settings..."),
				i(h, "Exit", 'q')),
			mkmen("Inventory",
				i(h, "Add...", 'i'),
				i(h, "Edit...", 'e'),
				i(h, "Delete"),
				i(h, "Generate IDs..."),
				i(h, "New Checkpoint")),
			cm,
			em,
			im,
			mkmen("?", i(h, "About..."))

		);
		checkpointCL = cm;
		exportCL     = em;
		importCL     = im;
	}

	private static Menu mkmen(String name, MenuItem... cnt) {
		return mkmen(new Menu(name), cnt);
	}

	private static <T extends Menu> T mkmen(T men, MenuItem... cnt) {
		men.getItems().addAll(cnt);
		return men;
	}

	private static MenuItem i(MainMenuEventHandler h, String s) {
		return i(h, s, '0');
	}

	private static MenuItem i(MainMenuEventHandler h, String s, char x) {
		MenuItem ret = new MenuItem(s);
		if(x != '0')
			ret.setAccelerator(KeyCombination.keyCombination(
				"shortcut+'" + Character.toUpperCase(x) + "'"));
		ret.setOnAction(h);
		return ret;
	}

}
