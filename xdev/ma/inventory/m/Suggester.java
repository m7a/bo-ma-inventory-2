package ma.inventory.m;

public interface Suggester {

	/** @return array of suggestions or null if no suggestion */
	public TableRow[] suggest(TableRow incomplete);

}
