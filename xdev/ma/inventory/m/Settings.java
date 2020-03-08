package ma.inventory.m;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Properties;
import java.nio.file.Files;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Settings {

	private final Log log;
	public final Properties properties;

	public Settings(Log log) {
		super();
		this.log = log;
		properties = new Properties();
		putDefaults(properties);
		if(Files.exists(Constants.PROPERTY_FILE)) {
			try(BufferedReader r = Files.newBufferedReader(
					Constants.PROPERTY_FILE, UTF_8)) {
				properties.load(r);
			} catch(IOException ex) {
				log.error("Failed to read properties from " +
						Constants.PROPERTY_FILE, ex);
			}
		} else {
			log.info("No settings found. " +
						"Initialized with defaults.");
		}
		try {
			applyEnv();
		} catch(ClassNotFoundException ex) {
			log.error("Failed to initialize environment " +
				"(probably incorrect properties file " +
				"or missing DB driver from CLASSPATH.", ex);
		}
	}

	private static void putDefaults(Properties p) {
		p.put("db.jdbc",         "jdbc:h2:$1");
		p.put("db.fn_match",     ".(mv|trace).db$");
		p.put("db.fn_repl",      "");
		p.put("db.load_drivers", "org.h2.Driver");
		p.put("require_auto_complete_for_ok", "true");
		p.put(
			"plugins",
			"ma.inventory.m.RCLSuggester," +
			"ma.inventory.plugin.isbndb.PluginISBNDB," +
			"ma.inventory.plugin.barcode.PluginBarCode," +
			"ma.inventory.plugin.csv.CSVPlugin"
		);
	}

	public boolean isRequireAutoCompleteBeforeOK() {
		return properties.getProperty("require_auto_complete_for_ok").
								equals("true");
	}

	public String jdbcForPath(String fn) {
		return properties.getProperty("db.jdbc").replace("$1",
			fn.replaceAll(properties.getProperty("db.fn_match"),
					properties.getProperty("db.fn_repl")));
	}

	public void applyEnv() throws ClassNotFoundException {
		Class.forName(properties.getProperty("db.load_drivers"));
	}

	private String[] getPluginClasses() {
		return properties.getProperty("plugins").split(",");
	}

	public String[][] getPlugins() {
		String[] plugins = getPluginClasses();
		if(plugins[0].length() == 0)
			return new String[0][];
		String[][] ret = new String[plugins.length][];
		for(int i = 0; i < plugins.length; i++)
			ret[i] = new String[] {
				plugins[i],
				properties.getProperty("plugins." + plugins[i])
			};
		return ret;
	}

	public void putPluginConfig(String p, String v) {
		properties.put("plugins." + p, v);
	}

	public void save() {
		try(BufferedWriter w = Files.newBufferedWriter(
					Constants.PROPERTY_FILE, UTF_8)) {
			properties.store(w, Constants.COPYRIGHT);
		} catch(IOException ex) {
			log.error("Failed to save properties to " +
				Constants.PROPERTY_FILE + ". The current " +
				"settings are only valid for this session.",
				ex);
		}
	}

}
