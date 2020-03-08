package ma.inventory.plugin.csv.import2;

import java.awt.Point;
import java.util.Map;
import java.util.HashMap;

import ma.inventory.m.Constants;
import ma.inventory.m.RowType;

class RevLookupStr {

	private final Map<String,Point> revLookupTBL;

	RevLookupStr() {
		super();
		this.revLookupTBL = new HashMap<String,Point>();
		for(RowType r: RowType.class.getEnumConstants()) {
			int x = r.ordinal();
			for(int i = 0; i < Constants.FD[x].length; i++) {
				revLookupTBL.put(Constants.FD[x][i],
							new Point(x, i));
			}
		}
	}

	/**
	 * @return
	 * 	Type (x) and sub-index (y) if rowName is for a common field or
	 *	<code>null</code> if unknown/not a field.
	 */
	public Point getIDXForField(String rowName) {
		return revLookupTBL.get(rowName);
	}

}
