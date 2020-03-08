package ma.inventory.plugin.isbndb;

import java.util.ArrayList;
import java.sql.*;
import ma.inventory.m.*;

public class PluginISBNDB implements Plugin, Suggester {

	private Verteiler v;
	private Connection psql;

	@Override
	public void init(String conf, Verteiler v) {
		this.v = v;
		psql = connect(conf);
		v.suggesters.add(this);
	}

	static Connection connect(String conf) {
		try {
			// initialize driver
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(conf);
			conn.setAutoCommit(false);
			return conn;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getDefaultConfiguration() {
		return getDefaultConfigurationS();
	}

	static String getDefaultConfigurationS() {
		return "jdbc:postgresql://127.0.0.1/ma_inventory?" +
					"user=linux-fan&password=testwort";
	}

	@Override
	public TableRow[] suggest(TableRow incomplete) {
		if(incomplete.getType() == RowType.BOOK) {
			try {
				return procBook(incomplete);
			} catch(SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
		return null;
	}

	private StaticTableRow[] procBook(TableRow incomplete)
							throws SQLException {
		String uidS = incomplete.getUserSuppliedID();
		if(hasField(uidS) && uidS.matches("^[0-9X]+$")) {
			long uid = uidS.indexOf('X') == -1? Long.parseLong(
					incomplete.getUserSuppliedID()): -1;
			StaticTableRow[] ret = querySuggestions(
				incomplete,
				"WHERE isbn13 = ? OR isbn10 = ? LIMIT 9",
				uid, uidS
			);
			if(ret.length != 0)
				return ret;
		}
		if(hasField(Constants.FD_BOOK_TITLE, incomplete)) {
			String match = incomplete.getField(RowType.BOOK,
						Constants.FD_BOOK_TITLE);
			StaticTableRow[] ret = querySuggestions(
				incomplete,
				"WHERE title LIKE '%' || ? || '%' LIMIT 5",
				match
			);
			if(ret.length != 0)
				return ret;
		}
		return null;
	}

	private boolean hasField(int fid, TableRow r) {
		return hasField(r.getField(r.getType(), fid));
	}

	private boolean hasField(String val) {
		return val != null && val.length() > 4;
	}

	private StaticTableRow[] querySuggestions(TableRow incomplete,
				String where, Object... a) throws SQLException {
		try(PreparedStatement stmt = psql.prepareStatement(
				"SELECT isbn13, isbn10, author, title, year, " +
				"publisher, location, pages FROM books " +
				where)) {
			for(int i = 0; i < a.length; i++) {
				if(a[i] instanceof Long)
					stmt.setLong(i + 1, (Long)a[i]);
				else if(a[i] instanceof String)
					stmt.setString(i + 1, (String)a[i]);
				else
					throw new RuntimeException(
						"Unexpected type (element " +
						a[i] + ")."
					);
			}
			try(ResultSet rs = stmt.executeQuery()) {
				ArrayList<StaticTableRow> r =
						new ArrayList<StaticTableRow>();
				while(rs.next())
					r.add(new StaticTableRow(
						incomplete.getDBID(),
						rs.getString("isbn13"),
						-1,
						1,
						RowType.BOOK,
						new String[][] {
							bookEntry(rs),
							Constants.EMPTY_OTHER,
						}
					));
				return r.toArray(new StaticTableRow[r.size()]);
			}
		}
	}

	private static String[] bookEntry(ResultSet rs) throws SQLException {
		return new String[] {
			f(rs.getString("author")),f(rs.getString("title")),
			f(rs.getString("year")), f(rs.getString("publisher")),
			f(rs.getString("location")), f(rs.getString("pages"))
		};
	}

	private static String f(String s) {
		return s == null? "": s;
	}

	@Override
	public void close() {
		if(v != null)
			v.suggesters.remove(this);
		try {
			if(psql != null)
				psql.close();
		} catch(SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
