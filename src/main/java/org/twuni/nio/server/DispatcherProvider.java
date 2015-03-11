package org.twuni.nio.server;

/**
 * A dispatcher provider provides a {@link Dispatcher}.
 */
public interface DispatcherProvider {

	/**
	 * Provide a dispatcher.
	 * 
	 * @return a dispatcher.
	 */
	public Dispatcher provideDispatcher();

}
