package ma.inventory.m;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

	public static final String ID =
			"Ma_Sys.ma Inventory 1.0.0.0";

	public static final String COPYRIGHT =
		ID + '\n' + "Copyright (c) 2016, 2017 Ma_Sys.ma.\n" +
		"JFX Autocomplete, Copyright (c) 2016 Narayan " +
			"G. Maharjan, http://ngopal.com.np/\n" +
		"TornadoFX Controls' DateTimePicker " +
			"https://github.com/edvin/tornadofx-controls\n" +
		"For further info send an e-mail to Ma_Sys.ma@web.de.";

	public static final Path PROPERTY_FILE =
					Paths.get("ma_inventory.properties");

	public static final int FD_BOOK_TITLE = 1;

	/** Field definition (names need to be unique) */
	public static final String[][] FD = {
		// RowType.BOOK
		{ "Author", "Title", "Year", "Publisher", "Pub.Loc.", "Pages" },
		// RowType.OTHER
		{
			// IT/PC/Tower, IT/PC/Laptop, IT/USB, etc.
			"Class",

			// HP Z400, MEDION ..., 
			"Thing",

			// Ma_Sys.ma 3, etc.
			"Name",

			"Location",

			// YYYY/MM/DD HH:ii:ss
			"T0",

			// e.g. Conrad, Reichelt, etc.
			"Origin",

			// 0: essential
			// 1: highly relevant
			// 2: commonly needed 
			// 3
			// 4: normal relevant
			// 5: rarely used
			// 6
			// 7: unimportant
			// 9: could be thrown away
			"Importance",

			// X_TBD
			"Comments"
		},
	};

	public static final String COL_ID       = "ID or ISBN";
	public static final String COL_PREV_QTY = "prev.Qty.";
	public static final String COL_QTY      = "Quantity";
	public static final String COL_TYPE     = "Type";

	public static final String[] EMPTY_OTHER = { "", "", "", "", "", "",
								"", "" };

}
