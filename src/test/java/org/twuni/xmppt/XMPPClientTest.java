package org.twuni.xmppt;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.core.Message;

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

	@Before
	public void setUp() throws IOException {
		connect();
	}

	@After
	public void tearDown() throws IOException {
		disconnect();
	}

}
