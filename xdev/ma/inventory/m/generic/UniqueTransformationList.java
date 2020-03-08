package ma.inventory.m.generic;

import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.TransformationList;
import java.util.*;

/** T must be elegible as a hash map key */
public class UniqueTransformationList<T> extends TransformationList<T,T> {

	/** Element, Idx */
	private final Map<T,Integer> selectedValues;

	/** Indices only but fast random access */
	private final List<Integer>  selectedIndices;

	public UniqueTransformationList(ObservableList<? extends T> source) {
		super(source);
		selectedIndices = new ArrayList<Integer>();
		selectedValues  = new HashMap<T,Integer>();

		addList(source, 0);
	}

	private void addList(List<? extends T> source, int idxOffset) {
		beginChange();
		int idx = idxOffset;
		for(T el: source) {
			if(!selectedValues.containsKey(el)) {
				selectedValues.put(el, idx);
				selectedIndices.add(idx);
			}
			idx++;
		}
		endChange();
	}

	@Override
	public int getSourceIndex(int index) {
		return selectedIndices.get(index);
	}

	@Override
	protected void sourceChanged(ListChangeListener.Change<? extends T> c) {
		beginChange();
		// TODO z this is very slow, but correct
		selectedValues.clear();
		selectedIndices.clear();
		addList(getSource(), 0);
		endChange();
		/*
		// TODO z CAUSES ARRAY INDEX OUT OF BOUNDS EXCEPTIONS DUE TO MISSING DELETION&PERMUTATION LOGIC.
		System.err.println("SOURCE CHANGED " + c);
		beginChange();
		// TODO z not overly effective (ideally, we could get the indices as in the original list)
		while(c.next()) {
			// TODO z Aufrücklogik fehlt -- Löschen aus der Mitte?
			for(T e: c.getRemoved()) {
				if(selectedValues.containsKey(e)) {
					int idx = selectedValues.get(e);
					System.err.println(" -> " + idx);
					selectedValues.remove((Object)e);
					selectedIndices.remove(idx);
				}
			}

			if(c.wasAdded())
				addList(c.getAddedSubList(), c.getFrom());
	***
 if (c.wasPermutated()) {
     for (int i = c.getFrom(); i < c.getTo(); ++i) {
	  //permutate
     }
 } else if (c.wasUpdated()) {
	//update item
 } else {
     for (Item remitem : c.getRemoved()) {
	 remitem.remove(Outer.this);
     }
     for (Item additem : c.getAddedSubList()) {
	 additem.add(Outer.this);
     }
 }
}
		}
		endChanged();
*/
	}

	@Override
	public T get(int index) {
		return getSource().get(getSourceIndex(index));
	}

	@Override
	public int size() {
		return selectedIndices.size();
	}

}
