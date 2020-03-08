package ma.inventory.m;

public interface TableRow {

	public long getDBID();
	public String getUserSuppliedID();
	public int getPrevQTY();
	public int getQTY();
	public RowType getType();
	public String getField(RowType t, int idx);

	public void setDBID(long dbID);

}
