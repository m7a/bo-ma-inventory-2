package ma.inventory.m.generic;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.TransformationList;

/**
 * Provides a view on the elements in the source list which is defined by the
 * given transformer function. The FunctionTransformationList contains for
 * each element from source the result of invoking transformer on the element.
 *
 * T: from
 * R: to
 */
public class FunctionTransformationList<T,R> extends TransformationList<R,T> {

	private final Function<T,R> transformer;

	private class TransformedChange extends ListChangeListener.Change<R> {

		private final ListChangeListener.Change<? extends T> chg;

		private TransformedChange(ListChangeListener.Change<? extends T>
									chg) {
			super(FunctionTransformationList.this);
			this.chg = chg;
		}

		@Override public int getFrom() { return chg.getFrom(); }

		@Override
		protected int[] getPermutation() {
			return new int[0]; // no permutation / TODO z HACK. IT SHOULD REALLY BE SIMILAR TO chg.getPermutation()
			//return chg.getPermutation();
		}

		@Override
		public List<R> getRemoved() {
			if(chg.getRemoved() == null)
				return null;

			List<R> transformationResultIntermediate =
							new ArrayList<R>();
			for(T el: chg.getRemoved())
				transformationResultIntermediate.add(
							transformer.apply(el));

			return transformationResultIntermediate;
		}

		@Override public int getTo() { return chg.getTo(); }
		@Override public boolean next() { return chg.next(); }
		@Override public void reset() { chg.reset(); }

		/*
		@Override public int getAddedSize() { chg.getAddedSize(); }

		@Override
		public List<T> getAddedSubList() {
			return new FunctionTransformationList(
					chg.getAddedSubList(), transformer);
		}

		@Override
		public ObservableList<R> getSourceList() {
			return FunctionTransformationList.this;
		}

		@Override
		*/

	}

	public FunctionTransformationList(ObservableList<? extends T> source,
						Function<T,R> transformer) {
		super(source);
		this.transformer = transformer;
	}

	@Override
	public int getSourceIndex(int index) {
		return index;
	}

	@Override
	protected void sourceChanged(ListChangeListener.Change<? extends T> c) {
		fireChange(new TransformedChange(c));
	}

	@Override
	public R get(int index) {
		return transformer.apply(getSource().get(index));
	}

	@Override
	public int size() {
		return getSource().size();
	}

}
