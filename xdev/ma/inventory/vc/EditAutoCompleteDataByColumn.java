package ma.inventory.vc;

import javafx.collections.ObservableList;
import ma.inventory.m.MainTableData;
import ma.inventory.m.MainTableRow;
import ma.inventory.m.Constants;
import ma.inventory.m.RowType;
import ma.inventory.m.generic.*;

class EditAutoCompleteDataByColumn {

	private final ObservableList<String>[][] data;

	// Otherwise we get ``generic array creation''
	@SuppressWarnings("unchecked")
	public EditAutoCompleteDataByColumn(MainTableData dt) {
		super();
		ObservableList<String>[][] dTmp = new ObservableList[
							Constants.FD.length][];
		for(final RowType t: RowType.class.getEnumConstants()) {
			int i = t.ordinal();
			dTmp[i] = new ObservableList[Constants.FD[i].length];
			for(int j = 0; j < dTmp[i].length; j++) {
				final int jc = j;
				dTmp[i][j] = new UniqueTransformationList<
						String>(
						new FunctionTransformationList<
						MainTableRow,String>(dt,
						line -> line.getField(t, jc)));
			}
		}
		data = dTmp;
	}

	ObservableList<String> getList(RowType t, int idx) {
		return data[t.ordinal()][idx];
	}

}
