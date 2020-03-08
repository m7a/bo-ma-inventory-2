package ma.inventory.vc;

import java.util.function.Consumer;
import javafx.stage.Stage;
import ma.inventory.m.Verteiler;

abstract class AbstractManager<T> implements Consumer<T> {

	protected final Verteiler v;
	protected final Stage wnd;

	AbstractManager(Verteiler v, Stage wnd) {
		super();
		this.v = v;
		this.wnd = wnd;
	}

	@Override
	public void accept(T e) {
		try {
			acceptFailable(e);
		} catch(Exception ex) {
			v.log.error(toString() + " failed.", ex);
		}
	}

	protected abstract void acceptFailable(T e) throws Exception;

}
