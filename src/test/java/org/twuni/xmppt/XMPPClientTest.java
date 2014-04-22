package org.twuni.xmppt;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;

public class XMPPClientTest extends XMPPClientTestFixture {

	@Ignore
	@Test
	public void sanityCheck() throws IOException, NoSuchAlgorithmException, KeyManagementException {
		connect( "localhost", 5222, false );
		service( "example.com" );
		login( "alice", "changeit" );
		bind( "xep0198-integration-test" );
		start();
	}

	@Override
	public void onPacketReceived( Object packet ) {

		System.out.println( String.format( "RECV [%s] %s", packet.getClass().getName(), packet ) );
		super.onPacketReceived( packet );

		if( packet instanceof Acknowledgment ) {
			stop();
		}

	}

	@Override
	public void onPacketSent( Object packet ) {
		System.out.println( String.format( "SEND [%s] %s", packet.getClass().getName(), packet ) );
		super.onPacketSent( packet );
	}

	@Override
	protected void onAvailable() {
		send( new Message( "abc123-12", Message.TYPE_CHAT, null, "alice@example.com", "<body>This is a good test.</body>" ) );
		send( new AcknowledgmentRequest() );
	}

}
