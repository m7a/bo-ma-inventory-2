package ma.inventory.m;

public enum RowType {

	BOOK("Book"),
	OTHER("Other");

	private final String visual;

	private RowType(String visual) {
		this.visual = visual;
	}

	/** @return visual */
	@Override
	public String toString() {
		return visual;
	}

}
