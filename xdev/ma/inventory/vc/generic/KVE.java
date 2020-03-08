package ma.inventory.vc.generic;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

// TODO MIGHT MOVE TO .m
public class KVE {

	public final StringProperty key;
	public final StringProperty value;

	public KVE(String k, String v) {
		super();
		key   = new SimpleStringProperty(k);
		value = new SimpleStringProperty(v);
	}

	public String         getKey()        { return key.getValue();   }
	public String         getValue()      { return value.getValue(); }
	public StringProperty keyProperty()   { return key;              }
	public StringProperty valueProperty() { return value;            }

}
