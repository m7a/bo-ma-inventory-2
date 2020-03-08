package ma.inventory.m;

import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import ma.tools2.util.BufferUtils;

public class VMGMT {

	static void unloadPlugins(List<Plugin> plugins, Verteiler v) {
		for(Plugin p: plugins) {
			try {
				p.close();
			} catch(Exception ex) {
				v.log.error("Failed to stop plugin: " + p, ex);
			}
		}
	}

	/** initialize defaults or load from configuration */
	public static void loadPlugins(List<Plugin> plugins, Verteiler v) {
		unloadPlugins(plugins, v);
		for(String[] p: v.set.getPlugins()) {
			Plugin pi = null;
			try {
				pi = (Plugin)(Class.forName(p[0]).
						getConstructor().newInstance());
				String conf;
				if(p[1] == null) {
					conf = pi.getDefaultConfiguration();
					v.set.putPluginConfig(pi.getClass().
						getCanonicalName(), conf);
				} else {
					conf = p[1];
				}
				pi.init(conf, v);
				plugins.add(pi);
			} catch(Exception ex) {
				v.log.error("Failed to load plugin: " +
								p[0], ex);
				try {
					if(pi != null)
						pi.close();
				} catch(Exception e2) {
					v.log.error("Failed to close plugin " +
						"(which falied to load): " +
						pi, e2);
				}
			}
		}
	}

	public static void setDB(String dbf, Verteiler v) {
		try {
			v.dbConn = DriverManager.getConnection(
							v.set.jdbcForPath(dbf));
			try {
				establishDBIfEmpty(dbf, v);
				v.db.assignDB(v.dbConn);
			} catch(SQLException ex) {
				v.dbConn.rollback();
				throw ex;
			}
		} catch(IOException | SQLException ex) {
			v.log.error("Failed to set database to \"" + dbf + "\"",
									ex);
		}
	}

	private static void establishDBIfEmpty(String dbf, Verteiler v)
					throws IOException, SQLException {
		boolean hasEntries = v.dbConn.getMetaData().getTables(null,
				null, null, new String[] {"TABLE"}).next();
		if(!hasEntries) {
			v.log.info("Initializing database at \"" + dbf + "\".");
			try(InputStream is = v.getClass().getResourceAsStream(
							"inventory.sql")) {	
				v.dbConn.createStatement().execute(
						BufferUtils.readfile(is));
			}
		}
	}
}
