package ma.inventory.m;

import java.util.Date;

public class Checkpoint {

	/**
	 * Marks a newly created checkpoint which has not been synced to the
	 * database yet.
	 */
	static final long NOT_LINKED_TO_DB = -1;

	private final long id;
	private final Date time;

	/**
	 * Create a checkpoint which is detached from the database.
	 */
	public Checkpoint(Date time) {
		this(NOT_LINKED_TO_DB, time);
	}

	Checkpoint(long id, Date time) {
		super();
		this.id = id;
		this.time = time;
	}

	@Override
	public String toString() {
		return (id == 0)? "0": time.toString(); // TODO z FORMAT DATE PROPERLY
	}

}
