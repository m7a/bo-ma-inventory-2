package ma.inventory.vc.generic;

import java.util.Map;
import java.util.HashMap;

// TODO MIGHT MOVE TO .m
public class DescribedProperties {

	public final String description;
	public final Map<String,String> kv;

	public DescribedProperties(String description) {
		super();
		this.description = description;
		kv = new HashMap<String,String>();
	}

	@Override
	public String toString() {
		return description;
	}

}
