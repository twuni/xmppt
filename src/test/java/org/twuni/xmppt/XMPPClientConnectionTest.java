package org.twuni.xmppt;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.twuni.Logger;
import org.twuni.nio.server.auth.SimpleAuthenticator;
import org.twuni.xmppt.client.SocketFactory;
import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xmpp.XMPPClientConnection;
import org.twuni.xmppt.xmpp.XMPPClientConnection.AcknowledgmentListener;
import org.twuni.xmppt.xmpp.XMPPClientConnection.PacketListener;
import org.twuni.xmppt.xmpp.XMPPClientConnectionManager;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;

public class XMPPClientConnectionTest {

	private static void send( XMPPClientConnectionManager connectionManager, String from, String to, String messageBody ) throws IOException {
		connectionManager.getConnection( from ).send( new Message( UUID.randomUUID().toString(), Message.TYPE_CHAT, from, to, new XMLBuilder( "body" ).content( messageBody ) ) );
	}

	private XMPPTestServer server;

	private XMPPClientConnection.Builder alice() {
		return local( "localhost", "alice", "p8ssw0rd." );
	}

	private XMPPClientConnection.Builder any( String serviceName, final String username, String password ) {

		XMPPClientConnection.Builder x = new XMPPClientConnection.Builder();

		x.log( true );
		x.socketFactory( SocketFactory.getInstance() );
		x.serviceName( serviceName ).resourceName( "xmpp-client-connection-test" );

		x.acknowledgmentListener( new AcknowledgmentListener() {

			private final Logger log = new Logger( AcknowledgmentListener.class.getName() );

			@Override
			public void onFailedAcknowledgment( int expected, int actual ) {
				log.info( "[%s] FAIL %s (expected:%d actual:%d)", username, Integer.valueOf( expected ), Integer.valueOf( actual ) );
			}

			@Override
			public void onSuccessfulAcknowledgment() {
				log.info( "[%s] PASS", username );
			}

		} );

		x.packetListener( new PacketListener() {

			private final Logger log = new Logger( PacketListener.class.getName() );

			@Override
			public void onException( XMPPClientConnection connection, Throwable exception ) {
				exception.printStackTrace();
			}

			@Override
			public void onPacketReceived( XMPPClientConnection connection, Object packet ) {
				log.info( "[%s] RECV %s", username, packet );
			}

		} );

		return x.userName( username ).password( password );

	}

	private XMPPClientConnection.Builder bob() {
		return local( "localhost", "bob", "p8ssw0rd!" );
	}

	@Test
	public void happyPath() throws IOException {

		XMPPClientConnection connection = alice().build();

		connection.send( new Message( UUID.randomUUID().toString(), Message.TYPE_CHAT, "alice@localhost", "alice@localhost", new XMLBuilder( "body" ).content( "Hello, world!" ) ) );

		try {
			Thread.sleep( 2500 );
		} catch( InterruptedException ignore ) {
			// Expect to be interrupted, maybe.
		}

		connection.disconnect();

	}

	@Test
	public void happyPath_forManagedConnection() throws IOException {

		String alice = "alice@localhost";
		String bob = "bob@localhost";

		XMPPClientConnectionManager connectionManager = new XMPPClientConnectionManager();

		connectionManager.startManaging( alice, alice() );

		connectionManager.connectAll();

		send( connectionManager, alice, bob, "Hello, world!" );
		connectionManager.getConnection( alice ).send( new AcknowledgmentRequest() );

		try {
			Thread.sleep( 2500 );
		} catch( InterruptedException ignore ) {
			// Expect to be interrupted, maybe.
		}

		connectionManager.stopManaging( alice );
		connectionManager.startManaging( bob, bob() );

		connectionManager.connectAll();

		try {
			Thread.sleep( 2500 );
		} catch( InterruptedException ignore ) {
			// Expect to be interrupted, maybe.
		}

		connectionManager.stopManagingAll();

	}

	public XMPPClientConnection.Builder local( String serviceName, final String username, String password ) {
		return any( serviceName, username, password ).host( "localhost" ).port( 5222 ).secure( false );
	}

	@Before
	public void startTestServer() {
		SimpleAuthenticator authenticator = new SimpleAuthenticator();
		authenticator.put( "alice", "p8ssw0rd." );
		authenticator.put( "bob", "p8ssw0rd!" );
		server = new XMPPTestServer( "localhost", authenticator, 5222, false );
		server.startListening();
	}

	@After
	public void stopTestServer() {
		server.stopListening();
		server = null;
	}

}
