package org.twuni.xmppt;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.twuni.nio.server.auth.SimpleAuthenticator;
import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xmpp.XMPPClientConnection;
import org.twuni.xmppt.xmpp.XMPPClientConnection.PacketListener;
import org.twuni.xmppt.xmpp.core.Message;

public class XMPPClientConnectionTest {

	private XMPPTestServer server;

	@Test
	public void happyPath() throws IOException {

		XMPPClientConnection.Builder x = new XMPPClientConnection.Builder();

		x.log( true );
		x.host( "localhost" ).port( 5222 ).secure( false ).serviceName( "localhost" );
		x.userName( "alice" ).password( "p8ssw0rd." );
		x.resourceName( "xmpp-client-connection-test" );

		x.packetListener( new PacketListener() {

			@Override
			public void onException( XMPPClientConnection connection, Throwable exception ) {
				exception.printStackTrace();
			}

			@Override
			public void onPacketReceived( XMPPClientConnection connection, Object packet ) {
				System.out.println( String.format( "RECV [%s] %s", packet.getClass().getName(), packet ) );
			}

		} );

		XMPPClientConnection connection = x.build();

		connection.send( new Message( UUID.randomUUID().toString(), Message.TYPE_CHAT, "alice@localhost", "alice@localhost", new XMLBuilder( "body" ).content( "Hello, world!" ) ) );

		try {
			Thread.sleep( 2500 );
		} catch( InterruptedException ignore ) {
			// Expect to be interrupted, maybe.
		}

		connection.disconnect();

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