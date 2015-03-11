package org.twuni.xmppt;

import java.io.IOException;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.util.Base64;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.ping.Ping;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamError;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public abstract class XMPPClientIntegrationTestBase extends XMPPClientTestFixture {

	@Test
	public void connect_shouldProduceError_ifServiceNameUnknown() throws IOException {
		StreamError error = connectWithError( getHost(), getPort(), isSecure(), String.format( "%s-unknown", getServiceName() ) );
		assertNotNull( error );
	}

	@Test( expected = IOException.class )
	public void connect_shouldThrowException_whenNetworkErrorOccurs() throws IOException {
		connect( "localhost", 4295, false, "localhost" );
	}

	protected void invokeDroppedMessage() throws IOException {
		goOnline( "drop-message-on-purpose" );
		send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>This message was dropped.</body>" ) );
		invokeConnectionLoss();
	}

	@Test
	public void iq_shouldIncrementReceivedPacketCountOnServer() throws IOException {

		goOnline( "send-iq-expect-ack-1" );

		// assertFeatureAvailable( Ping.class );

		send( new IQ( generatePacketID(), IQ.TYPE_SET, null, getStream().from(), new Ping() ) );
		nextPacket( IQ.class );// Ignore the response.

		assertPacketsSentWereReceived();

		goOffline();

	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifPasswordNotAccepted() throws IOException {
		goOnline( "minimal-connection-flow" );
		goOffline();
		connect();
		login( getUsername(), "iamnotvalid" );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifUsernameUnknown() throws IOException {
		connect();
		login( "idonotexist", getPassword() );
	}

	@Test
	public void send_shouldAllowLargeMessages() throws IOException {
		goOnline( "send-large-message" );
		byte [] buffer = new byte [48 * 1024];
		new Random().nextBytes( buffer );
		send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>" + Base64.encodeBase64String( buffer ) + "</body>" ) );
		assertPacketsSentWereReceived();
		goOffline();
	}

	@Test
	public void sendEnable_shouldCauseFailureWhenSentBeforeBind() throws IOException {
		connect();
		login();
		try {
			enableStreamManagement();
			fail();
		} catch( ClassCastException exception ) {
			// This is what we want.
		}
		bind( "full-on-double-rainbow" );
		disconnect();
	}

	@Test
	public void sendPresence_shouldBeAcknowledged() throws IOException {
		goOnline( "presence-storm" );
		assertPacketsSentWereReceived();
		for( int i = 0; i < 10; i++ ) {
			send( new Presence( generatePacketID() ) );
			nextPacket( Presence.class );
		}
		assertPacketsSentWereReceived();
		goOffline();
	}

	@Test
	public void server_shouldAcknowledgeAllPacketsSent() throws IOException {

		goOnline( "send-n-messages-expect-ack-n" );

		int n = 10;

		for( int i = 0; i < n; i++ ) {
			send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), String.format( "<body>This message is at index %d.</body>", Integer.valueOf( i ) ) ) );
		}

		for( int i = 0; i < n; i++ ) {
			nextPacket();
		}

		assertPacketsSentWereReceived();

		goOffline();

	}

	@Ignore( "FIXME: This will not pass when run against the local, single-connection test server." )
	@Test
	public void server_shouldDeliverUnacknowledgedStanzas_whenResumingSession() throws IOException {

		goOnline( "previous-session-to-resume" );
		Context previousContext = getContext();

		invokeConnectionLoss();
		// FIXME: This triggers a half-open TCP socket on the server side, which
		// on a single-connection test server, prohibits further connections.

		goOnline( previousContext );
		goOffline();

	}

	@Test
	public void server_shouldIgnoreAcknowledgmentWithExpectedValue() throws IOException {
		goOnline( "send-unsolicited-ack-0" );
		assertFeatureAvailable( StreamManagement.class );
		send( new Acknowledgment( 0 ) );
		goOffline();
	}

	@Ignore( "This is just an exploratory test." )
	@Test
	public void server_shouldReportCapabilities() throws IOException {

		goOnline( "check-capabilities" );

		assertFeatureAvailable( CapabilitiesHash.class );

		CapabilitiesHash capabilities = getFeatures().getFeature( CapabilitiesHash.class );

		send( new IQ( generatePacketID(), IQ.TYPE_GET, null, getStream().from(), capabilities.query() ) );
		nextPacket();

		goOffline();

	}

	@Test
	public void server_shouldReportNoPacketsReceived_whenNoPacketsHaveBeenSent() throws IOException {
		goOnline( "send-nothing-expect-ack-0" );
		assertFeatureAvailable( StreamManagement.class );
		assertPacketsSentWereReceived();
		goOffline();
	}

	@Test
	public void server_shouldReportSinglePacketReceived_whenOnePacketHasBeenSent() throws IOException {

		goOnline( "send-message-expect-ack-1" );
		assertFeatureAvailable( StreamManagement.class );

		send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );
		nextPacket();// Ignore this packet we have sent to ourselves.

		assertPacketsSentWereReceived();

		goOffline();

	}

	@Test
	public void server_shouldResendPacket_whenClientDoesNotAcknowledgeIt() throws IOException {

		goOnline( "request-resend" );
		assertFeatureAvailable( StreamManagement.class );

		// Trigger a packet to get sent to us.
		send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );

		// The server will immediately echo this message back to us.
		nextPacket();

		int count = 1;// getReceivedPacketCount();

		// Tell the server we received nothing.
		send( new Acknowledgment( 0 ) );

		// Expect the server to send it all again.
		for( int i = 0; i < count; i++ ) {
			next();
		}

		// Place nicely, and acknowledge receipt this time.
		send( new Acknowledgment( count ) );

		goOffline();

	}

}
