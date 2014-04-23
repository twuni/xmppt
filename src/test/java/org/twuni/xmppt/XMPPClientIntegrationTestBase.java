package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public abstract class XMPPClientIntegrationTestBase extends XMPPClientTestFixture {

	@Test( expected = IOException.class )
	public void connect_shouldProduceError_ifServiceNameUnknown() throws IOException {
		connect( getHost(), getPort(), isSecure(), String.format( "%s-unknown", getServiceName() ) );
	}

	@Test( expected = IOException.class )
	public void connect_shouldThrowException_whenNetworkErrorOccurs() throws IOException {
		connect( "void.example.com", 4295, false, "localhost" );
	}

	protected void invokeDroppedMessage() throws IOException {
		goOnline( "drop-message-on-purpose" );
		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>This message was dropped.</body>" ) );
		goOffline();
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifPasswordNotAccepted() throws IOException {
		connect();
		login( getUsername(), "iamnotvalid" );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifUsernameUnknown() throws IOException {
		connect();
		login( "idonotexist", getPassword() );
	}

	@Test
	public void server_shouldIgnoreAcknowledgmentWithExpectedValue() throws IOException {
		goOnline( "send-unsolicited-ack-0" );
		assertFeatureAvailable( StreamManagement.class );
		xmpp.write( new Acknowledgment( 0 ) );
		goOffline();
	}

	@Ignore( "This, for now, is just an exploratory test." )
	@Test
	public void server_shouldReportCapabilities() throws IOException {

		goOnline( "check-capabilities" );

		assertFeatureAvailable( CapabilitiesHash.class );

		CapabilitiesHash capabilities = getFeatures().getFeature( CapabilitiesHash.class );

		xmpp.write( new IQ( generatePacketID(), IQ.TYPE_GET, null, getStream().from(), capabilities.query() ) );
		xmpp.nextPacket();

		goOffline();

	}

	@Test
	public void server_shouldReportNoPacketsReceived_whenNoPacketsHaveBeenSent() throws IOException {
		goOnline( "send-nothing-expect-ack-0" );
		assertFeatureAvailable( StreamManagement.class );
		assertPacketsReceived( 0 );
		goOffline();
	}

	@Test
	public void server_shouldAcknowledgeAllPacketsSent() throws IOException {

		goOnline( "send-n-messages-expect-ack-n" );
		assertFeatureAvailable( StreamManagement.class );

		int n = 10;
		for( int i = 0; i < n; i++ ) {
			xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), String.format( "<body>This message is at index %d.</body>", Integer.valueOf( i ) ) ) );
		}

		for( int i = 0; i < n; i++ ) {
			xmpp.nextPacket();// Ignore the messages we have sent.
		}

		assertPacketsReceived( n );

		goOffline();

	}

	@Test
	public void server_shouldReportSinglePacketReceived_whenOnePacketHasBeenSent() throws IOException {

		goOnline( "send-message-expect-ack-1" );
		assertFeatureAvailable( StreamManagement.class );

		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );
		xmpp.nextPacket();// Ignore this packet we have sent to ourselves.

		assertPacketsReceived( 1 );

		goOffline();

	}

	@Ignore( "This is currently known to fail." )
	@Test
	public void server_shouldResendPacket_whenClientDoesNotAcknowledgeIt() throws IOException {

		goOnline( "request-resend" );
		assertFeatureAvailable( StreamManagement.class );

		// Trigger a packet to get sent to us.
		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );

		// The server will immediately echo this message back to us.
		xmpp.nextPacket();

		// Tell the server we didn't receive the message.
		xmpp.write( new Acknowledgment( 0 ) );

		// Expect the server to send the message again.
		xmpp.nextPacket();

		// Place nicely, and acknowledge receipt this time.
		xmpp.write( new Acknowledgment( 1 ) );

		goOffline();

	}

	@Ignore( "This is currently known to fail." )
	@Test
	public void server_shouldResendDroppedMessage() throws IOException {
		invokeDroppedMessage();
		goOnline( "expect-resend-dropped-message" );
		xmpp.next();
		goOffline();
	}

}
