package ma.inventory.vc.generic;

import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import java.util.function.Consumer;

public class MutableRadioMenu<T> extends MutableMenu<T> {

	private final ToggleGroup tg;

	public MutableRadioMenu(String name, Consumer<T> callback) {
		super(name, callback);
		tg = new ToggleGroup();
	}

	@Override
	protected MenuItem instantiateMenuItem(String label) {
		RadioMenuItem rv = new RadioMenuItem(label);
		rv.setToggleGroup(tg);
		return rv;
	}

}
