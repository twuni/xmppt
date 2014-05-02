package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.ping.Ping;
import org.twuni.xmppt.xmpp.push.Push;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public abstract class XMPPClientIntegrationTestBase extends XMPPClientTestFixture {

	@Test( expected = IOException.class )
	public void connect_shouldProduceError_ifServiceNameUnknown() throws IOException {
		connect( getHost(), getPort(), isSecure(), String.format( "%s-unknown", getServiceName() ) );
	}

	@Test( expected = IOException.class )
	public void connect_shouldThrowException_whenNetworkErrorOccurs() throws IOException {
		connect( "localhost", 4295, false, "localhost" );
	}

	protected void invokeDroppedMessage() throws IOException {
		goOnline( "drop-message-on-purpose" );
		send( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>This message was dropped.</body>" ) );
		goOffline();
	}

	@Test
	public void iq_shouldIncrementReceivedPacketCountOnServer() throws IOException {

		goOnline( "send-iq-expect-ack-1" );

		// assertFeatureAvailable( Ping.class );

		send( new IQ( generatePacketID(), IQ.TYPE_SET, null, getStream().from(), new Ping() ) );
		IQ iq = nextPacket( IQ.class );// Ignore the response.

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
	public void registerPushNotification_shouldBeSuccessful() throws IOException {
		goOnline( "register-push" );
		IQ sent = new IQ( generatePacketID(), IQ.TYPE_SET, null, getStream().from(), Push.register( "xmppt", "IGNORE_THIS" ) );
		send( sent );
		IQ received = nextPacket();
		assertEquals( IQ.TYPE_RESULT, received.type() );
		assertEquals( sent.id(), received.id() );
		assertNotNull( received.getContent( Push.class ) );
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
			nextPacket();// Ignore the messages we have sent.
		}

		assertPacketsSentWereReceived();

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
	public void server_shouldResendDroppedMessage() throws IOException {
		invokeDroppedMessage();
		goOnline( "expect-resend-dropped-message" );
		nextPacket( Message.class );
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
