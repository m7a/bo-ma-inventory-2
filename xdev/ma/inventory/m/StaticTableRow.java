package ma.inventory.m;

import javafx.beans.property.*;

public class StaticTableRow implements TableRow {

	private final long dbID;
	private final String userSuppliedID;
	private final int prevQTY;
	private final int qty;
	private final RowType type;
	private final String[][] fields;

	/** Create snapshot */
	public StaticTableRow(MainTableRow base) {
		this(base.dbID, base.userSuppliedID, base.prevQTY,
					base.qty, base.type, base.fields);
	}

	/** Create snapshot from properties */
	public StaticTableRow(long dbID, StringProperty userSuppliedID,
						IntegerProperty prevQTY,
						IntegerProperty qty,
						ObjectProperty<RowType> type,
						StringProperty[][] fields) {
		super();
		this.dbID = dbID;
		this.userSuppliedID = userSuppliedID.getValue();
		this.prevQTY = prevQTY.getValue();
		this.qty = qty.getValue();
		this.type = type.getValue();
		this.fields = new String[fields.length][];
		for(int i = 0; i < fields.length; i++) {
			this.fields[i] = new String[fields[i].length];
			for(int j = 0; j < fields[i].length; j++)
				this.fields[i][j] = fields[i][j].getValue();
		}
	}

	/** Create instance from values */
	public StaticTableRow(long dbID, String userSuppliedID, int qty,
			int prevQTY, RowType type, String[][] fields) {
		super();
		this.dbID = dbID;
		this.userSuppliedID = userSuppliedID;
		this.prevQTY = prevQTY;
		this.qty = qty;
		this.type = type;
		this.fields = fields;
	}

	@Override
	public long getDBID() { return dbID; }
	@Override
	public String getUserSuppliedID() { return userSuppliedID; }
	@Override
	public int getPrevQTY() { return prevQTY; }
	@Override
	public int getQTY() { return qty; }
	@Override
	public RowType getType() { return type; }
	@Override
	public String getField(RowType t, int idx) {
		return fields[t.ordinal()][idx];
	}

	@Override
	public void setDBID(long dbID) {
		throw new UnsupportedOperationException(
			"Cannot set the ID of a static table row: dbID " +
			this.dbID + " -> " + dbID + " could not be set."
		);
	}

}
