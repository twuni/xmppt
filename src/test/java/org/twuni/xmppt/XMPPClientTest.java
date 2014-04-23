package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

@Ignore( "These are integration tests, and should not be run automatically." )
public class XMPPClientTest extends XMPPClientTestFixture {

	@Test
	public void server_shouldReportNoPacketsReceived_whenNoPacketsHaveBeenSent() throws IOException {

		connect( "localhost", 5222, false, "example.com" );
		login( "alice", "changeit" );
		bind( "test" );
		assertStreamManagement();

		assertPacketsReceived( 0 );

		logout();
		disconnect();

	}

	@Test
	public void server_shouldReportSinglePacketReceived_whenOnePacketHasBeenSent() throws IOException {

		connect( "localhost", 5222, false, "example.com" );
		login( "alice", "changeit" );
		bind( "test" );
		assertStreamManagement();

		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );
		xmpp.nextPacket();// Ignore this packet we have sent to ourselves.

		assertPacketsReceived( 1 );

		logout();
		disconnect();

	}

	@Test( expected = IOException.class )
	public void connect_shouldThrowException_whenNetworkErrorOccurs() throws IOException {
		connect( "void.example.com", 4295, false, "localhost" );
	}

	@Test( expected = IOException.class )
	public void connect_shouldProduceError_ifServiceNameUnknown() throws IOException {
		connect( "localhost", 5222, false, "void.example.com" );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifUsernameUnknown() throws IOException {
		connect( "localhost", 5222, false, "example.com" );
		login( "idonotexist", "changeit" );
	}

	@Test( expected = IOException.class )
	public void login_shouldProduceError_ifPasswordNotAccepted() throws IOException {
		connect( "localhost", 5222, false, "example.com" );
		login( "alice", "iamnotvalid" );
	}

	protected void assertStreamManagement() {
		assertTrue( "XEP-0198 support should have been included in the stream features provided by the server.", getFeatures().hasFeature( StreamManagement.class ) );
	}

	@Test
	public void server_shouldIgnoreAcknowledgmentWithExpectedValue() throws IOException {

		connect( "localhost", 5222, false, "example.com" );
		login( "alice", "changeit" );
		bind( "test" );
		assertStreamManagement();

		xmpp.write( new Acknowledgment( 0 ) );

		logout();
		disconnect();

	}

}
