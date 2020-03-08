package ma.inventory.m;

import java.sql.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;

public class DataTransmitter {

	private final Log log;
	private final MainTableData t;
	private final List<Checkpoint> checkpoints;

	private Connection db;

	// TODO CLOSE/FREE AT EXIT?
	private PreparedStatement add;
	private PreparedStatement addWithID;
	private PreparedStatement[] addFields;
	private PreparedStatement delete1;
	private PreparedStatement delete2;

	private long currentInventoryID;

	DataTransmitter(Log log, MainTableData t,
						List<Checkpoint> checkpoints) {
		super();
		this.log = log;
		this.t = t;
		this.checkpoints = checkpoints;
		currentInventoryID = 0;
		db = null;
		t.preListener = this::refReplaced;
	}

	void assignDB(Connection conn) {
		db = conn;
		try {
			db.setAutoCommit(false);
		} catch(SQLException ex) {
			log.error("Failed to disable auto-commit.", ex);
		}

		// To prevent loading data from causing it to be writtin to the
		// DB again.
		t.preListener = null;

		// TODO z what about potentially existing data

		try {
			loadInitial();
		} catch(SQLException ex) {
			log.error("Failed to load data from database.", ex);
		} finally {
			t.preListener = this::refReplaced;
		}

		try {
			prepareStatements();
		} catch(SQLException ex) {
			log.error("Failed to prepare statemnt.", ex);
		}
	}

	/** overwrites existing data */
	private void loadInitial() throws SQLException {
		loadInitialCheckpoints();
		loadInitialMainTable();
	}

	private void loadInitialCheckpoints() throws SQLException {
		checkpoints.clear();
		try(Statement st = db.createStatement()) {
			try(ResultSet rs = st.executeQuery("SELECT idid, t " +
						"FROM inventory_dates " +
						"ORDER BY t ASC;")) {
				long id = 0;
				while(rs.next()) {
					id = rs.getLong("idid");
					checkpoints.add(new Checkpoint(id,
							rs.getDate("t")));
				}
				currentInventoryID = id;
			}
		}
	}

	private void loadInitialMainTable() throws SQLException {
		Map<Long,MainTableRow> m1 = query1("");
		// minor race condition here
		// If elements are inserted while this function runs, it might
		// yield a NullPointerException because we will search
		// for an element for which we do not have an entry in the Map.
		assignExtendedFields(m1, "");
		t.setAll(m1.values());
	}

	Map<Long,MainTableRow> query1(String f, Object... d)
							throws SQLException {
		Map<Long,MainTableRow> newRows =
					new LinkedHashMap<Long,MainTableRow>();
		try(PreparedStatement st = db.prepareStatement("SELECT iid, " +
				"kind, qty, userSuppliedId FROM items " + f)) {
			assignParameters(st, d);
			try(ResultSet rs = st.executeQuery()) {
				RowType[] rt = RowType.values();
				while(rs.next()) {
					MainTableRow tr = new MainTableRow(
							rs.getLong("iid"));
					tr.type.setValue(rt[rs.getInt("kind")]);
					tr.qty.setValue(rs.getInt("qty"));
					tr.userSuppliedID.setValue(strval(
							rs.getString(
							"userSuppliedId")));
					newRows.put(tr.dbID, tr);
				}
			}
		}
		return newRows;
	}

	private static String strval(String s) {
		return s == null? "": s;
	}

	// TODO z ALSO USEFUL FOR PLUGINS
	private static void assignParameters(PreparedStatement st, Object... d)
							throws SQLException {
		for(int i = 0; i < d.length; i++) {
			if(d[i] instanceof Long)
				st.setLong(i + 1, (Long)d[i]);
			else if(d[i] instanceof String)
				st.setString(i + 1, (String)d[i]);
			else
				throw new RuntimeException("Unexpected type " +
							"(element " + d[i]);
		}
	}

	void assignExtendedFields(Map<Long,MainTableRow> rows, String f,
					Object... d) throws SQLException {
		try(PreparedStatement st = db.prepareStatement("SELECT iid, " +
				"k, v FROM item_fields WHERE idid = 0 " + f)) {
			assignParameters(st, d);
			try(ResultSet rs = st.executeQuery()) {
				while(rs.next()) {
					MainTableRow tr = rows.get(rs.getLong(
									"iid"));
					tr.fields[tr.type.getValue().ordinal()][
						rs.getInt("k")].setValue(strval(
						rs.getString("v")));
				}
			}
		}
	}

	private void prepareStatements() throws SQLException {
		add = db.prepareStatement(
			"INSERT INTO items (iid, kind, qty, userSuppliedId) " +
			"VALUES (NEXT VALUE FOR items_seq, ?, ?, ?);"
		);
		addWithID = db.prepareStatement(
			"INSERT INTO items (iid, kind, qty, userSuppliedId) " +
			"VALUES (?, ?, ?, ?);"
		);
		// TODO CSTAT ISSUE: WE NOW WANT TO SUPPORT MULTIPLE INVENTORY DATES. UNFORTUNATELY THIS WOULD REQUIRE US TO _SHARE_ QTY BETWEEN INVENTORY DATES WHICH DOES NOT MAKE SENSE => ADD A TABLE item_fields_static WHICH HAS iid, idid and qty... -> need to do a schema migration. => PROBABLY ESTABLISH A CSV EXPORT AND USE THAT INSTEAD OF IMPLEMENTING A MIGRATION CSTAT SUBSTAT
		delete1 = db.prepareStatement(
				"DELETE FROM item_fields WHERE iid = ?;");
		delete2 = db.prepareStatement(
				"DELETE FROM items WHERE iid = ?;");
		addFields = new PreparedStatement[Constants.FD.length];
		for(int i = 0; i < addFields.length; i++) {
			StringBuilder queryString = new StringBuilder(
						"INSERT INTO item_fields " +
						"(iid, idid, k, v) VALUES ");
			for(int j = 0; j < Constants.FD[i].length; j++) {
				queryString.append("(?,?,?,?)");
				queryString.append((j ==
					Constants.FD[i].length - 1)? ';': ',');
			}
			addFields[i] = db.prepareStatement(queryString.
								toString());
		}
	}

	private void refReplaced(MainTableRow o, MainTableRow n) {
		try {
			if(o != null)
				applyDeleted(o);
			if(n != null)
				applyAdded(n);
		} catch(SQLException ex) {
			try {
				db.rollback();
			} catch(SQLException ex2) {
				log.error("Failed to rollback failed " +
					"transaction: " + ex2.toString(), ex2);
			}
			throw new RuntimeException("Transaction aborted due " +
							"to SQLException: " +
							ex.toString(), ex);
		}
	}

	private void applyDeleted(MainTableRow r) throws SQLException {
		delete1.setLong(1, r.dbID);
		delete1.execute();
		delete2.setLong(1, r.dbID);
		delete2.execute();
		db.commit();
	}

	private void applyAdded(TableRow r) throws SQLException {
		// TODO z deal with added items which just increase qty
		boolean hasDBID = r.getDBID() != -1;
		PreparedStatement useStmt = hasDBID? addWithID: add;
		RowType type = r.getType();
		int typeI = type.ordinal();
		int idx = 1;
		if(hasDBID)
			useStmt.setLong(idx++, r.getDBID());
		useStmt.setInt(idx++, typeI);
		useStmt.setInt(idx++, r.getQTY());
		useStmt.setString(idx++, r.getUserSuppliedID());
		useStmt.execute();
		long iid;
		if(hasDBID) {
			iid = r.getDBID();
		} else {
			ResultSet rs = add.getGeneratedKeys();
			if(!rs.next())
				throw new RuntimeException("No ID returned " +
							"ID required for " +
							"consecutive insert.");
			iid = rs.getLong(1);
			r.setDBID(iid);
			rs.close();
		}

		int cpos = 1;
		for(int i = 0; i < Constants.FD[typeI].length; i++) {
			addFields[typeI].setLong(cpos++, iid);
			addFields[typeI].setInt(cpos++, 0);
			addFields[typeI].setInt(cpos++, i);
			addFields[typeI].setString(cpos++, r.getField(type, i));
		}

		addFields[typeI].execute();
		db.commit();
	}

	public void generateIDs(int n) {
		try {
			StringBuilder q = new StringBuilder(
					"INSERT INTO items (iid, " +
					"kind, qty, userSuppliedID) VALUES ");
			for(int i = 0; i < n; i++) {
				if(i != 0)
					q.append(',');
				q.append("(NEXT VALUE FOR items_seq, 1, 0, " +
						"(NEXT VALUE FOR " +
						"masysma_id_seq)::text)");
			}
			db.createStatement().execute(q.toString());
			db.commit();
		} catch(SQLException ex) {
			RuntimeException e2 = new RuntimeException(ex);
			try {
				db.rollback();
			} catch(SQLException exS) {
				e2.addSuppressed(exS);
			}
			throw e2;
		}
		// TODO very inefficient but anoter implementation seemed to be tricky (TODO DEVISE SOMETHING BETTER)
		assignDB(db);
	}

}
