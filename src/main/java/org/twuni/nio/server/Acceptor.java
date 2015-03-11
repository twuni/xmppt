package org.twuni.nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.twuni.Logger;

/**
 * An Acceptor listens for incoming socket connections on a given
 * {@link ServerSocketChannel}. It then creates a {@link Connection} between the
 * newly accepted socket and the provided dispatcher, listening for events being
 * emitted from the given {@link EventHandler}, then registers that connection
 * onto the dispatcher.
 */
public class Acceptor implements Runnable, Closeable {

	private static Logger defaultLogger() {
		return new Logger( Acceptor.class.getName() );
	}

	private final ServerSocketChannel channel;
	private final DispatcherProvider dispatcherProvider;
	private final EventHandler eventHandler;
	private final ConnectionFactory connectionFactory;
	private final Logger log;
	private boolean running;

	public Acceptor( InetAddress address, int port, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( new InetSocketAddress( address, port ), dispatcherProvider, eventHandler, connectionFactory );
	}

	public Acceptor( InetAddress address, int port, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( address, port, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	public Acceptor( int port, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( new InetSocketAddress( port ), dispatcherProvider, eventHandler, connectionFactory );
	}

	public Acceptor( int port, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( port, SelectorProvider.provider().openSelector(), eventHandler, connectionFactory, DispatcherPool.DEFAULT_SIZE );
	}

	public Acceptor( int port, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( port, SelectorProvider.provider().openSelector(), eventHandler, connectionFactory, dispatcherCount );
	}

	public Acceptor( int port, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( port, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	public Acceptor( SocketAddress localServerEndpoint, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( localServerEndpoint, dispatcherProvider, eventHandler, connectionFactory, defaultLogger() );
	}

	/**
	 * Opens a blocking server socket channel bound to the given
	 * {@code localServerEndpoint}.
	 *
	 * @param localServerEndpoint
	 *            the local endpoint on which to bind the newly opened
	 *            {@link ServerSocketChannel}.
	 * @param dispatcherProvider
	 *            the provider from which to obtain {@link Dispatcher} instances
	 *            each time a {@link Socket} is accepted by this object.
	 * @param eventHandler
	 *            the protocol-specific event interpreter and emitter.
	 * @param connectionFactory
	 *            the factory to use for creating connections each time a
	 *            {@link Socket} is accepted by this object.
	 * @param logger
	 *            the logger implementation to use for debugging.
	 * @throws IOException
	 *             if something goes wrong while attempting to open, configure,
	 *             or bind the internal {@link ServerSocketChannel} used by
	 *             this object.
	 */
	public Acceptor( SocketAddress localServerEndpoint, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory, Logger logger ) throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking( true );
		channel.socket().bind( localServerEndpoint );
		this.dispatcherProvider = dispatcherProvider;
		this.connectionFactory = connectionFactory;
		this.eventHandler = eventHandler;
		log = logger;
	}

	public Acceptor( SocketAddress localServerEndpoint, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( localServerEndpoint, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	@Override
	public void close() throws IOException {
		running = false;
		channel.close();
	}

	protected void onException( Throwable exception ) {
		log.info( "ERROR T/%s %s", exception.getClass().getName(), exception.getLocalizedMessage() );
	}

	@Override
	public void run() {
		running = true;
		log.info( "OPEN" );
		while( running && channel.isOpen() ) {
			if( Thread.interrupted() ) {
				running = false;
				break;
			}
			try {
				SocketChannel client = channel.accept();
				log.info( "ACCEPT" );
				client.configureBlocking( false );
				Dispatcher dispatcher = dispatcherProvider.provideDispatcher();
				Connection connection = connectionFactory.createConnection( client, dispatcher, eventHandler );
				dispatcher.register( connection );
			} catch( ClosedChannelException exception ) {
				log.info( "CLOSED" );
				break;
			} catch( IOException exception ) {
				onException( exception );
			}
		}
	}

}
