package ma.inventory.m;

import java.util.Map;
import java.util.Iterator;
import java.sql.*;

public class RCLSuggester implements Suggester, Plugin {

	private boolean enabled;
	private Verteiler v;

	public RCLSuggester() {
		super();
	}

	@Override
	public void init(String conf, Verteiler v) {
		enabled = conf != null && conf.equals("enabled");
		this.v = v;
		v.suggesters.add(this);
	}

	public String getDefaultConfiguration() {
		return "enabled";
	}

	@Override
	public TableRow[] suggest(TableRow incomplete) {
		String uid = incomplete.getUserSuppliedID();
		if(uid != null && uid.length() > 4 &&
					uid.matches("^[0-9A-Za-z_-]+$")) {
			try {
				return query(incomplete);
			} catch(SQLException ex) {
				throw new RuntimeException(ex);
			}
		} else {
			return null;
		}
	}

	private TableRow[] query(TableRow incomplete) throws SQLException {
		Map<Long,MainTableRow> results = v.db.query1(" WHERE " +
					"userSuppliedID = '" +
					incomplete.getUserSuppliedID() + "'");
		if(results.size() >= 1) {
			TableRow[] ret = new TableRow[results.size()];
			Iterator<MainTableRow> mtr = results.values().
								iterator();
			int i = -1;
			while(mtr.hasNext()) {
				ret[++i] = mtr.next();
				v.db.assignExtendedFields(results, "AND iid = "
							+ ret[i].getDBID());
			}
			return ret;
		} else {
			return null;
		}
	}

	@Override
	public void close() {
		// not needed
	}

}
