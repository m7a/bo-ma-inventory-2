package ma.inventory.vc.generic;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import java.util.function.Consumer;

public class MutableMenu<T> extends Menu implements ListChangeListener<T> {

	private final Consumer<T> callback;

	public MutableMenu(String name, Consumer<T> callback) {
		super(name);
		this.callback = callback;
	}

	private void addT(final T e) {
		MenuItem it = instantiateMenuItem(e.toString());
		it.setUserData(e);
		it.setOnAction(this::refCallT);
		getItems().add(it);
	}

	protected MenuItem instantiateMenuItem(String label) {
		return new MenuItem(label);
	}

	@SuppressWarnings("unchecked")
	private void refCallT(ActionEvent ev) {
		callback.accept((T)(((MenuItem)ev.getSource()).getUserData()));
	}

	private void removeT(T e) {
		MenuItem it = null;
		for(MenuItem mi: getItems()) {
			if(mi.getUserData() == e) { // instance comparison
				it = mi;
				break;
			}
		}
		if(it != null)
			getItems().remove(it);
	}

	@Override
	public void onChanged(ListChangeListener.Change<? extends T> ch) {
		while(ch.next()) {
			for(T c: ch.getRemoved())
				removeT(c);
			for(T c: ch.getAddedSubList())
				addT(c);
		}
	}

}
