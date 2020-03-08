package ma.inventory.m;

import java.util.function.Consumer;

class CheckpointManager implements Consumer<Checkpoint> {

	CheckpointManager() {
		super();
	}

	void addCheckpoint() {
		// ...
	}

	/** Switch to given checkpoint */
	@Override
	public void accept(Checkpoint c) {
		System.err.println("TODO N_IMPL ACCEPT " + c);
	}

}
