package org.twuni.nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class Dispatcher implements Runnable, Closeable {

	private static final Logger LOG = new Logger( Dispatcher.class.getName() );

	private final Object guard = new Object();
	private final Selector selector;
	private final EventHandler eventHandler;
	private boolean running;

	public Dispatcher( Selector selector, EventHandler eventHandler ) {
		this.selector = selector;
		this.eventHandler = eventHandler;
	}

	public void register( Connection connection ) throws ClosedChannelException {
		LOG.info( "#%s(%s)", "register", connection.getClass().getName() );
		synchronized( guard ) {
			selector.wakeup();
			connection.getClient().register( selector, SelectionKey.OP_READ, connection );
		}
		eventHandler.onConnected( connection );
	}

	protected void onException( Throwable exception ) {
		LOG.info( "#%s(%s) %s", "onException", exception.getClass().getSimpleName(), exception.getLocalizedMessage() );
	}

	public void requestWrite( Connection connection ) {
		SelectionKey key = connection.getClient().keyFor( selector );
		synchronized( guard ) {
			selector.wakeup();
			key.interestOps( SelectionKey.OP_READ | SelectionKey.OP_WRITE );
		}
	}

	@Override
	public void run() {

		running = true;

		while( running ) {

			synchronized( guard ) {
				// This suspends the thread until the guard becomes unlocked.
			}

			try {
				selector.select();
			} catch( IOException exception ) {
				onException( exception );
				continue;
			}

			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

			while( iterator.hasNext() ) {

				SelectionKey key = iterator.next();
				iterator.remove();

				Connection connection = (Connection) key.attachment();

				if( !connection.getClient().isConnected() || !connection.getClient().isOpen() ) {
					eventHandler.onDisconnected( connection );
					key.cancel();
					continue;
				}

				if( key.isValid() && key.isReadable() ) {
					eventHandler.onReadRequested( connection );
				}

				if( key.isValid() && key.isWritable() ) {
					eventHandler.onWriteRequested( connection );
				}

			}

		}

	}

	@Override
	public void close() throws IOException {
		LOG.info( "#%s", "close" );
		running = false;
	}

}