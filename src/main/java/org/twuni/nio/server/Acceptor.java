package org.twuni.nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class Acceptor implements Runnable, Closeable {

	private static final Logger LOG = new Logger( Acceptor.class.getName() );

	private final ServerSocketChannel channel;
	private final DispatcherProvider dispatcherProvider;
	private final EventHandler eventHandler;
	private final ConnectionFactory connectionFactory;
	private boolean running;

	public Acceptor( int port, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( port, SelectorProvider.provider().openSelector(), eventHandler, connectionFactory, DispatcherPool.DEFAULT_SIZE );
	}

	public Acceptor( int port, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( port, SelectorProvider.provider().openSelector(), eventHandler, connectionFactory, dispatcherCount );
	}

	public Acceptor( int port, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( port, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	public Acceptor( InetAddress address, int port, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( address, port, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	public Acceptor( InetAddress address, int port, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( new InetSocketAddress( address, port ), dispatcherProvider, eventHandler, connectionFactory );
	}

	public Acceptor( int port, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		this( new InetSocketAddress( port ), dispatcherProvider, eventHandler, connectionFactory );
	}

	public Acceptor( SocketAddress localServerEndpoint, Selector selector, EventHandler eventHandler, ConnectionFactory connectionFactory, int dispatcherCount ) throws IOException {
		this( localServerEndpoint, new DispatcherPool( dispatcherCount, selector, eventHandler ), eventHandler, connectionFactory );
	}

	public Acceptor( SocketAddress localServerEndpoint, DispatcherProvider dispatcherProvider, EventHandler eventHandler, ConnectionFactory connectionFactory ) throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking( true );
		channel.socket().bind( localServerEndpoint );
		this.dispatcherProvider = dispatcherProvider;
		this.connectionFactory = connectionFactory;
		this.eventHandler = eventHandler;
	}

	@Override
	public void close() throws IOException {
		running = false;
		channel.close();
	}

	@Override
	public void run() {
		running = true;
		while( running && channel.isOpen() ) {
			try {
				SocketChannel client = channel.accept();
				LOG.info( "#%s %s", "run", "Client connected." );
				client.configureBlocking( false );
				Dispatcher dispatcher = dispatcherProvider.provideDispatcher();
				dispatcher.register( connectionFactory.createConnection( client, dispatcher, eventHandler ) );
			} catch( ClosedChannelException exception ) {
				LOG.info( "Stopped." );
				break;
			} catch( IOException exception ) {
				onException( exception );
			}
		}
	}

	protected void onException( Throwable exception ) {
		LOG.info( "#%s(%s) %s", "onException", exception.getClass().getSimpleName(), exception.getLocalizedMessage() );
	}

}
