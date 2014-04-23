package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public abstract class XMPPClientIntegrationTestBase extends XMPPClientTestFixture {

	@Ignore( "This is currently known to fail." )
	@Test
	public void server_shouldResendDroppedMessage() throws IOException {
		invokeDroppedMessage();
		goOnline( "expect-resend-dropped-message" );
		xmpp.next();
		goOffline();
	}

	@Test
	public void server_shouldReportNoPacketsReceived_whenNoPacketsHaveBeenSent() throws IOException {
		goOnline( "send-nothing-expect-ack-0" );
		assertStreamManagement();
		assertPacketsReceived( 0 );
		goOffline();
	}

	protected void invokeDroppedMessage() throws IOException {
		goOnline( "drop-message-on-purpose" );
		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>This message was dropped.</body>" ) );
		goOffline();
	}

	@Test
	public void server_shouldReportSinglePacketReceived_whenOnePacketHasBeenSent() throws IOException {

		goOnline( "send-message-expect-ack-1" );
		assertStreamManagement();

		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );
		xmpp.nextPacket();// Ignore this packet we have sent to ourselves.

		assertPacketsReceived( 1 );

		goOffline();

	}

	@Test( expected = IOException.class )
	public void connect_shouldThrowException_whenNetworkErrorOccurs() throws IOException {
		connect( "void.example.com", 4295, false, "localhost" );
	}

	@Test( expected = IOException.class )
	public void connect_shouldProduceError_ifServiceNameUnknown() throws IOException {
		connect( getHost(), getPort(), isSecure(), String.format( "%s-unknown", getServiceName() ) );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifUsernameUnknown() throws IOException {
		connect();
		login( "idonotexist", getPassword() );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifPasswordNotAccepted() throws IOException {
		connect();
		login( getUsername(), "iamnotvalid" );
	}

	protected void assertStreamManagement() {
		assertTrue( "XEP-0198 support should have been included in the stream features provided by the server.", getFeatures().hasFeature( StreamManagement.class ) );
	}

	@Test
	public void server_shouldIgnoreAcknowledgmentWithExpectedValue() throws IOException {
		goOnline( "send-unsolicited-ack-0" );
		assertStreamManagement();
		xmpp.write( new Acknowledgment( 0 ) );
		goOffline();
	}

}
