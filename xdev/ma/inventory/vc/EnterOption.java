package ma.inventory.vc;

enum EnterOption {

	// TODO z IT MIGHT BE EFFICIENT TO HAVE DISAMBIGUATE_THEN_SAVE OR SUCH

	AUTO, DISAMBIGUATE, APPLY, NOP;

	@Override
	public String toString() {
		switch(this) {
		case AUTO:         return "Auto";
		case DISAMBIGUATE: return "Disambiguate";
		case APPLY:        return "Save directly to database";
		case NOP:          return "No operation";
		default: throw new RuntimeException("Unknown enum constant: " +
							super.toString());
		}
	}

}
