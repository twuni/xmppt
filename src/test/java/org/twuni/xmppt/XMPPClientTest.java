package org.twuni.xmppt;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.StreamManagementFeature;

@Ignore( "These are integration tests, and should not be run automatically." )
public class XMPPClientTest extends XMPPClientTestFixture {

	@Test
	public void server_shouldReportNoPacketsReceived_whenNoPacketsHaveBeenSent() throws IOException {
		assertPacketsReceived( 0 );
	}

	@Test
	public void server_shouldReportSinglePacketReceived_whenOnePacketHasBeenSent() throws IOException {

		xmpp.write( new Message( generatePacketID(), Message.TYPE_CHAT, null, getSimpleJID(), "<body>Hello, world.</body>" ) );
		xmpp.nextPacket();// Ignore this packet we have sent to ourselves.

		assertPacketsReceived( 1 );

	}

	@Test
	public void server_shouldIgnoreAcknowledgmentWithExpectedValue() throws IOException {
		xmpp.write( new Acknowledgment( 0 ) );
	}

	@Before
	public void setUp() throws IOException {

		connect( "localhost", 5222, false, "example.com" );
		login( "alice", "changeit" );
		bind( "test" );

		assertTrue( "XEP-0198 support should have been included in the stream features provided by the server.", getFeatures().hasFeature( StreamManagementFeature.class ) );

	}

	@After
	public void tearDown() throws IOException {
		logout();
		disconnect();
	}

}
