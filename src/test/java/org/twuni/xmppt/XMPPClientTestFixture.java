package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Assert;
import org.twuni.xmppt.xmpp.XMPPSocket;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.sasl.SASLMechanisms;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;
import org.twuni.xmppt.xmpp.session.Session;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamManagementFeature;

public class XMPPClientTestFixture extends Assert {

	protected String getServiceName() {
		return "example.com";
	}

	protected String getUsername() {
		return "alice";
	}

	protected String getPassword() {
		return "changeit";
	}

	protected String getResourceName() {
		return "test";
	}

	protected String getHost() {
		return "localhost";
	}

	protected int getPort() {
		return 5222;
	}

	protected boolean isSecure() {
		return false;
	}

	protected Features getFeatures() {
		return features;
	}

	protected String getFullJID() {
		return fullJID;
	}

	protected String getSimpleJID() {
		return String.format( "%s@%s", getUsername(), getServiceName() );
	}

	protected String generatePacketID() {
		sequence++;
		if( streamID == null ) {
			streamID = Long.toHexString( System.currentTimeMillis() );
		}
		return String.format( "%s-%d", streamID, Integer.valueOf( sequence ) );
	}

	private String streamID;
	private int sequence;
	private Features features;
	private String fullJID;
	protected XMPPSocket xmpp;

	protected void connect() throws IOException {
		connect( getHost(), getPort(), isSecure() );
	}

	protected void connect( String host, int port, boolean secure ) throws IOException {

		xmpp = new XMPPSocket( host, port, secure );

		xmpp.write( new Stream( getServiceName() ) );

		Stream stream = xmpp.nextPacket();

		assertEquals( getServiceName(), stream.from() );
		streamID = stream.id();

		features = xmpp.nextPacket();

		if( features.hasFeature( SASLMechanisms.class ) ) {

			SASLMechanisms mechanisms = (SASLMechanisms) features.getFeature( SASLMechanisms.class );

			if( mechanisms.hasMechanism( SASLPlainAuthentication.MECHANISM ) ) {

				xmpp.write( new SASLPlainAuthentication( getUsername(), getPassword() ) );

				SASLSuccess success = xmpp.nextPacket();

				xmpp.write( new Stream( getServiceName() ) );

				stream = xmpp.nextPacket();
				streamID = stream.id();

				assertEquals( getServiceName(), stream.from() );

				features = xmpp.nextPacket();

			}

		}

		String id = null;

		if( features.hasFeature( Bind.class ) ) {

			id = generatePacketID();
			xmpp.write( new IQ( id, IQ.TYPE_SET, null, null, Bind.resource( getResourceName() ) ) );

			IQ bindIQ = xmpp.nextPacket();

			assertEquals( id, bindIQ.id() );
			assertEquals( IQ.TYPE_RESULT, bindIQ.type() );

			Bind bind = (Bind) bindIQ.getContent();

			fullJID = bind.jid();
			assertNotNull( "Server failed to provide a bound JID.", fullJID );

			if( features.hasFeature( Session.class ) ) {

				id = generatePacketID();
				xmpp.write( new IQ( id, IQ.TYPE_SET, null, null, new Session() ) );

				IQ sessionIQ = xmpp.nextPacket();

				assertEquals( id, sessionIQ.id() );
				assertEquals( IQ.TYPE_RESULT, sessionIQ.type() );

			}

			id = generatePacketID();
			xmpp.write( new Presence( id ) );

			Presence presence = xmpp.nextPacket();

			assertEquals( id, presence.id() );
			assertEquals( bind.jid(), presence.from() );
			assertEquals( bind.jid(), presence.to() );

		}

		assertTrue( "XEP-0198 support should have been included in the stream features provided by the server.", features.hasFeature( StreamManagementFeature.class ) );

	}

	protected void assertPacketsReceived( int h ) throws IOException {
		xmpp.write( new AcknowledgmentRequest() );
		Acknowledgment acknowledgment = xmpp.nextPacket();
		assertEquals( h, acknowledgment.getH() );
	}

	protected void disconnect() throws IOException {

		xmpp.write( new Presence( generatePacketID(), Presence.Type.UNAVAILABLE ) );

		xmpp.write( "</stream:stream>" );
		xmpp.write( "</stream:stream>" );

		xmpp.flush();

		xmpp.close();

		streamID = null;
		sequence = 0;
		fullJID = null;
		features = null;
		xmpp = null;

	}

}
