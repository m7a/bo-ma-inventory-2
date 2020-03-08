package ma.inventory.plugin.csv.import2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CSVAutomaton {

	private static enum State { CSV_CNT, CSV_STR, POT_ESC }

	private final String quot;
	private final String sep;

	CSVAutomaton(String quotChar, String separator) {
		super();
		quot = quotChar;
		sep = separator;
	}

	/** May be invoked concurrently */
	String[] run(String line) {
		String pl = line;
		StringBuilder buf = new StringBuilder();
		List<String> rs = new ArrayList<String>();
		State s = State.CSV_CNT;

		while(pl.length() != 0) {
			switch(s) {
			case CSV_CNT:
				if(pl.startsWith(sep))  {
					pop(buf, rs);
					pl = discard(sep, pl);
				} else if(pl.startsWith(quot)) {
					s = State.CSV_STR;
					pl = discard(quot, pl);
				} else {
					pl = push(buf, pl);
				}
				break;
			case CSV_STR:
				if(pl.startsWith(quot)) {
					s = State.POT_ESC;
					pl = discard(quot, pl);
				} else {
					pl = push(buf, pl);
				}
			case POT_ESC:
				if(pl.startsWith(sep)) {
					pop(buf, rs);
					s = State.CSV_CNT;
					pl = discard(sep, pl);
				} else if(pl.startsWith(quot)) {
					s = State.CSV_STR;
					pl = push(buf, pl, quot);
				} else {
					pl = push(buf, pl);
					throw new RuntimeException(
						"CSV parsing failed: " +
						"unexpected symbol (buf=" +
						buf.toString() + ",s=" + s +
						",lineProc=" +
						Arrays.toString(rs.toArray()) +
						",lineIn=" + line + ",pl=" +
						pl + ")"
					);
				}
			}
		}

		if(buf.length() > 0)
			pop(buf, rs);

		return rs.toArray(new String[rs.size()]);
	}

	private static void pop(StringBuilder buf, List<String> rs) {
		rs.add(buf.toString());
		buf.delete(0, buf.length());
	}

	private static String push(StringBuilder buf, String pl) {
		return push(buf, pl, pl.substring(0, 1));
	}

	private static String push(StringBuilder buf, String pl, String val) {
		buf.append(val);
		return discard(val, pl);
	}

	private static String discard(String val, String pl) {
		return pl.substring(val.length() + 1);
	}

}
