package ma.inventory.m;

import javafx.beans.property.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents an inventory item
 */
public class MainTableRow implements TableRow {

	long dbID;

	public final StringProperty userSuppliedID;
	public final IntegerProperty prevQTY;
	public final IntegerProperty qty;
	public final ObjectProperty<RowType> type;
	public final StringProperty[][] fields;

	public MainTableRow(long dbID) {
		this(null);
		this.dbID = dbID;
	}

	public MainTableRow(TableRow base) {
		super();
		if(base == null) {
			dbID = -1;
			userSuppliedID = new SimpleStringProperty("");
			prevQTY = new SimpleIntegerProperty(0);
			qty = new SimpleIntegerProperty(1);
			type = new SimpleObjectProperty<RowType>(RowType.BOOK);
			fields = initFields();
		} else {
			dbID = base.getDBID();
			userSuppliedID = new SimpleStringProperty(base.
							getUserSuppliedID());
			prevQTY = new SimpleIntegerProperty(base.getPrevQTY());
			qty = new SimpleIntegerProperty(base.getQTY());
			type = new SimpleObjectProperty<RowType>(base.
								getType());
			fields = initFields();
			for(RowType t: RowType.class.getEnumConstants())
				for(int j = 0; j < fields[t.ordinal()].length;
									j++)
					fields[t.ordinal()][j].setValue(base.
								getField(t, j));
		}
	}

	private static StringProperty[][] initFields() {
		StringProperty[][] r = new StringProperty[
							Constants.FD.length][];
		for(int i = 0; i < r.length; i++) {
			r[i] = new StringProperty[Constants.FD[i].length];
			for(int j = 0; j < r[i].length; j++)
				r[i][j] = new SimpleStringProperty("");
		}
		return r;
	}

	@Override
	public long getDBID() { return dbID; }
	@Override
	public String getUserSuppliedID() { return userSuppliedID.getValue(); }
	@Override
	public int getPrevQTY() { return prevQTY.getValue(); }
	@Override
	public int getQTY() { return qty.getValue(); }
	@Override
	public RowType getType() { return type.getValue(); }
	@Override
	public String getField(RowType t, int idx) {
		return fields[t.ordinal()][idx].getValue();
	}

	public StringProperty userSuppliedIDProperty() {
		return userSuppliedID;
	}
	public IntegerProperty qtyProperty() { return qty; }
	public IntegerProperty prevQTYProperty() { return prevQTY; }
	public ObjectProperty<RowType> typeProperty() { return type; }

	@Override
	public void setDBID(long dbID) {
		this.dbID = dbID;
	}

}
