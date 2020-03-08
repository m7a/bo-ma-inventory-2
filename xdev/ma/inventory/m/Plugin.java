package ma.inventory.m;

public interface Plugin {

	public void init(String conf, Verteiler v);

	public String getDefaultConfiguration();

	public void close();

}
