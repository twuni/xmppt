package org.twuni;

public abstract class Retry implements Runnable {

	public static final long DEFAULT_INITIAL_DELAY = 5000;

	private long delay;

	public Retry() {
		this( DEFAULT_INITIAL_DELAY );
	}

	public Retry( long initialDelay ) {
		delay = initialDelay;
	}

	protected abstract boolean isFinished();

	@Override
	public void run() {
		while( !isFinished() ) {
			delay();
			tryAgain();
			delay *= 2;
		}
	}

	private void delay() {
		try {
			Thread.sleep( delay );
		} catch( InterruptedException ignore ) {
			// Ignore.
		}
	}

	protected abstract void tryAgain();

}
