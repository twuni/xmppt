package org.twuni.xmppt.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;

import org.twuni.nio.server.Acceptor;
import org.twuni.nio.server.ConnectionFactory;
import org.twuni.nio.server.DispatcherProvider;
import org.twuni.nio.server.EventHandler;
import org.twuni.nio.server.auth.Authenticator;
import org.twuni.nio.server.auth.AutomaticAuthenticator;

public class XMPPAcceptor extends Acceptor {

	private static EventHandler createEventHandler( String serviceName, Authenticator authenticator ) {
		return new XMPPEventHandler( serviceName, authenticator );
	}

	private static Authenticator defaultAuthenticator() {
		return new AutomaticAuthenticator();
	}

	private static final ConnectionFactory CONNECTION_FACTORY = new XMPPConnectionFactory();

	public XMPPAcceptor( InetAddress address, int port, DispatcherProvider dispatcherProvider, String serviceName, Authenticator authenticator ) throws IOException {
		super( address, port, dispatcherProvider, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( InetAddress address, int port, Selector selector, String serviceName, Authenticator authenticator, int dispatcherCount ) throws IOException {
		super( address, port, selector, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( int port, DispatcherProvider dispatcherProvider, String serviceName, Authenticator authenticator ) throws IOException {
		super( port, dispatcherProvider, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( int port, Selector selector, String serviceName, Authenticator authenticator, int dispatcherCount ) throws IOException {
		super( port, selector, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( int port, String serviceName ) throws IOException {
		this( port, serviceName, defaultAuthenticator() );
	}

	public XMPPAcceptor( int port, String serviceName, Authenticator authenticator ) throws IOException {
		super( port, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( int port, String serviceName, Authenticator authenticator, int dispatcherCount ) throws IOException {
		super( port, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY, dispatcherCount );
	}

	public XMPPAcceptor( SocketAddress localServerEndpoint, DispatcherProvider dispatcherProvider, String serviceName, Authenticator authenticator ) throws IOException {
		super( localServerEndpoint, dispatcherProvider, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY );
	}

	public XMPPAcceptor( SocketAddress localServerEndpoint, Selector selector, String serviceName, Authenticator authenticator, int dispatcherCount ) throws IOException {
		super( localServerEndpoint, selector, createEventHandler( serviceName, authenticator ), CONNECTION_FACTORY, dispatcherCount );
	}

}
