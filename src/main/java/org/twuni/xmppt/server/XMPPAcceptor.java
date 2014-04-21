package org.twuni.xmppt.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;

public class XMPPAcceptor extends Acceptor {

	private static final ConnectionFactory CONNECTION_FACTORY = new XMPPConnectionFactory();

	private static EventHandler createEventHandler( String serviceName ) {
		return new XMPPEventHandler( serviceName );
	}

	public XMPPAcceptor( int port, String serviceName ) throws IOException {
		super( port, createEventHandler( serviceName ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( int port, String serviceName, int dispatcherCount ) throws IOException {
		super( port, createEventHandler( serviceName ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( int port, Selector selector, String serviceName, int dispatcherCount ) throws IOException {
		super( port, selector, createEventHandler( serviceName ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( InetAddress address, int port, Selector selector, String serviceName, int dispatcherCount ) throws IOException {
		super( address, port, selector, createEventHandler( serviceName ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( InetAddress address, int port, DispatcherProvider dispatcherProvider, String serviceName ) throws IOException {
		super( address, port, dispatcherProvider, createEventHandler( serviceName ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( int port, DispatcherProvider dispatcherProvider, String serviceName ) throws IOException {
		super( port, dispatcherProvider, createEventHandler( serviceName ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( SocketAddress localServerEndpoint, Selector selector, String serviceName, int dispatcherCount ) throws IOException {
		super( localServerEndpoint, selector, createEventHandler( serviceName ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( SocketAddress localServerEndpoint, DispatcherProvider dispatcherProvider, String serviceName ) throws IOException {
		super( localServerEndpoint, dispatcherProvider, createEventHandler( serviceName ), CONNECTION_FACTORY );
	}

}
