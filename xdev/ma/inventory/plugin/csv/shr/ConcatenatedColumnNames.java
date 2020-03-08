package ma.inventory.plugin.csv.shr;

import ma.inventory.m.Constants;

/**
 * Algorithmically initialized constants.
 *
 * TODO z MIGHT MAKE SENSE NOT TO HAVE THIS STATIC?
 */
public class ConcatenatedColumnNames {

	// reverse-lookup
	public static final int COL_ID_IDX = 0;
	public static final int COL_PREV_QTY_IDX = 1;
	public static final int COL_QTY_IDX = 2;
	public static final int COL_TYPE_IDX = 3;

	public static final String[] PRE_EXPORT_COL = {
		Constants.COL_ID, Constants.COL_PREV_QTY, Constants.COL_QTY,
		Constants.COL_TYPE
	};

	public static final String[] ALL_COLUMN_LABELS;

	static {
		int numcol = PRE_EXPORT_COL.length;
		for(int i = 0; i < Constants.FD.length; i++)
			numcol += Constants.FD[i].length;
		String[] actmp = new String[numcol];

		System.arraycopy(PRE_EXPORT_COL, 0, actmp, 0,
							PRE_EXPORT_COL.length);
		int offset = PRE_EXPORT_COL.length;
		for(int i = 0; i < Constants.FD.length; i++) {
			System.arraycopy(Constants.FD[i], 0, actmp, offset,
							Constants.FD[i].length);
			offset += Constants.FD[i].length;
		}

		ALL_COLUMN_LABELS = actmp;
	}

}
