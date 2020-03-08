package ma.inventory.m;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import javafx.collections.ModifiableObservableListBase;

public class MainTableData extends ModifiableObservableListBase<MainTableRow> {

	private final Map<Long,MainTableRow> idMap;
	private final ArrayList<MainTableRow> backend;

	BiConsumer<MainTableRow,MainTableRow> preListener = null;

	MainTableData() {
		super();
		backend = new ArrayList<MainTableRow>();
		idMap = new HashMap<Long,MainTableRow>();
	}

	@Override
	public MainTableRow get(int index) {
		return backend.get(index);
	}

	@Override
	public int size() {
		return backend.size();
	}

	@Override
	protected void doAdd(int index, MainTableRow element) {
		if(preListener != null)
			preListener.accept(null, element);
		elinfo(element);
		backend.add(index, element);
	}

	private void elinfo(MainTableRow element) {
		if(element.getDBID() != -1)
			idMap.put(element.getDBID(), element);
	}

	@Override
	protected MainTableRow doSet(int index, MainTableRow element) {
		MainTableRow or = null;
		if(index < size()) {
			or = backend.get(index);
			element.dbID = or.dbID;
		}
		if(preListener != null)
			preListener.accept(or, element);
		elinfo(element);
		return backend.set(index, element);
	}

	@Override
	protected MainTableRow doRemove(int index) {
		MainTableRow rmv = backend.get(index);
		if(preListener != null)
			preListener.accept(rmv, null);
		if(rmv.getDBID() != -1)
			idMap.remove(rmv.getDBID());
		return backend.remove(index);
	}

	/** @return index for ID or -1 if not found */
	public int lookupID(long id) {
		// TODO z indexOf call is inefficient, should be something more advanced
		return (id == -1)? -1: backend.indexOf(idMap.get(id));
	}

}
