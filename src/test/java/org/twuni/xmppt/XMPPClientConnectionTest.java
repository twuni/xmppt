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
import org.twuni.xmppt.xmpp.XMPPClientConnection.PacketListener;
import org.twuni.xmppt.xmpp.XMPPClientConnectionManager;
import org.twuni.xmppt.xmpp.core.Message;

public class XMPPClientConnectionTest {

	private XMPPTestServer server;

	private XMPPClientConnection.Builder alice() {

		XMPPClientConnection.Builder x = new XMPPClientConnection.Builder();

		x.log( true );
		x.socketFactory( SocketFactory.getInstance() );
		x.host( "localhost" ).port( 5222 ).secure( false ).serviceName( "localhost" );
		x.userName( "alice" ).password( "p8ssw0rd." );
		x.resourceName( "xmpp-client-connection-test" );

		x.packetListener( new PacketListener() {

			private final Logger log = new Logger( PacketListener.class.getName() );

			@Override
			public void onException( XMPPClientConnection connection, Throwable exception ) {
				exception.printStackTrace();
			}

			@Override
			public void onPacketReceived( XMPPClientConnection connection, Object packet ) {
				log.info( "RECV %s", packet );
			}

		} );

		return x;

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

		XMPPClientConnectionManager connectionManager = new XMPPClientConnectionManager();

		connectionManager.startManaging( alice, alice() );

		connectionManager.connectAll();

		connectionManager.getConnection( alice ).send( new Message( UUID.randomUUID().toString(), Message.TYPE_CHAT, "alice@localhost", "alice@localhost", new XMLBuilder( "body" ).content( "Hello, world!" ) ) );

		try {
			Thread.sleep( 2500 );
		} catch( InterruptedException ignore ) {
			// Expect to be interrupted, maybe.
		}

		connectionManager.stopManagingAll();

	}

	@Before
	public void startTestServer() {
		SimpleAuthenticator authenticator = new SimpleAuthenticator();
		authenticator.setCredential( "alice", "p8ssw0rd." );
		server = new XMPPTestServer( "localhost", authenticator, 5222, false );
		server.startListening();
	}

	@After
	public void stopTestServer() {
		server.stopListening();
		server = null;
	}

}
