package org.twuni.xmppt;

import java.io.IOException;
import java.util.Stack;

import org.junit.Assert;
import org.twuni.xmppt.xmpp.XMPPSocket;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.sasl.SASLFailure;
import org.twuni.xmppt.xmpp.sasl.SASLMechanisms;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;
import org.twuni.xmppt.xmpp.session.Session;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamError;

public class XMPPClientTestFixture extends Assert {

	private static class Context {

		public Stream stream;
		public Features features;
		public int sequence;

		public String nextID() {
			sequence++;
			return String.format( "%s-%d", stream.id(), Integer.valueOf( sequence ) );
		}

	}

	private final Stack<Context> contexts = new Stack<Context>();
	private String fullJID;
	protected XMPPSocket xmpp;

	protected void assertFeatureAvailable( Class<?> feature ) {
		assertTrue( String.format( "Feature not available: %s", feature.getName() ), isFeatureAvailable( feature ) );
	}

	protected void assertPacketsReceived( int h ) throws IOException {
		xmpp.write( new AcknowledgmentRequest() );
		Acknowledgment acknowledgment = xmpp.nextPacket();
		assertEquals( h, acknowledgment.getH() );
	}

	protected void bind() throws IOException {
		bind( getResourceName() );
	}

	protected void bind( String resourceName ) throws IOException {

		String id = null;

		if( isFeatureAvailable( Bind.class ) ) {

			id = generatePacketID();
			xmpp.write( new IQ( id, IQ.TYPE_SET, null, null, Bind.resource( resourceName ) ) );

			IQ bindIQ = xmpp.nextPacket();

			assertEquals( id, bindIQ.id() );
			assertEquals( IQ.TYPE_RESULT, bindIQ.type() );

			Bind bind = (Bind) bindIQ.getContent();

			fullJID = bind.jid();
			assertNotNull( "Server failed to provide a bound JID.", fullJID );

			if( isFeatureAvailable( Session.class ) ) {

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
			assertEquals( fullJID, presence.from() );
			assertEquals( fullJID, presence.to() );

		}

	}

	protected void connect() throws IOException {
		connect( getHost(), getPort(), isSecure(), getServiceName() );
	}

	protected void connect( String host, int port, boolean secure, String serviceName ) throws IOException {

		prepareConnect();

		xmpp = new XMPPSocket( host, port, secure );

		xmpp.write( new Stream( serviceName ) );

		Context context = new Context();

		context.stream = xmpp.nextPacket();

		Object packet = xmpp.next();

		if( packet instanceof StreamError ) {
			throw new IOException( ( (StreamError) packet ).content.toString() );
		}

		if( packet instanceof Features ) {
			context.features = (Features) packet;
		}

		contexts.push( context );

		assertEquals( serviceName, context.stream.from() );

	}

	protected void disconnect() throws IOException {

		if( isAuthenticated() ) {
			logout();
		}

		while( !contexts.isEmpty() ) {
			if( xmpp != null ) {
				xmpp.write( "</stream:stream>" );
			}
			contexts.pop();
		}

		if( xmpp != null ) {
			xmpp.flush();
			xmpp.close();
			xmpp = null;
		}

	}

	protected String generatePacketID() {
		Context context = getContext();
		return context != null ? context.nextID() : Long.toHexString( System.currentTimeMillis() );
	}

	private Context getContext() {
		return contexts.isEmpty() ? null : contexts.peek();
	}

	protected Features getFeatures() {
		Context context = getContext();
		return context != null ? context.features : null;
	}

	protected String getFullJID() {
		return fullJID;
	}

	protected String getHost() {
		return "localhost";
	}

	protected String getPassword() {
		return "changeit";
	}

	protected int getPort() {
		return 5222;
	}

	protected String getResourceName() {
		return "test";
	}

	protected String getServiceName() {
		return "example.com";
	}

	protected String getSimpleJID() {
		return getFullJID().replaceAll( "/.+$", "" );
	}

	protected Stream getStream() {
		Context context = getContext();
		return context != null ? context.stream : null;
	}

	protected String getUsername() {
		return "alice";
	}

	protected void goOffline() throws IOException {
		logout();
		disconnect();
	}

	protected void goOnline() throws IOException {
		goOnline( getResourceName() );
	}

	protected void goOnline( String resourceName ) throws IOException {
		connect();
		login();
		bind( resourceName );
	}

	protected boolean isAuthenticated() {
		return fullJID != null;
	}

	protected boolean isConnected() {
		return xmpp != null;
	}

	protected boolean isFeatureAvailable( Class<?> feature ) {
		return getFeatures().hasFeature( feature );
	}

	protected boolean isSecure() {
		return false;
	}

	protected void login() throws IOException {
		login( getUsername(), getPassword() );
	}

	protected void login( String username, String password ) throws IOException {

		if( isFeatureAvailable( SASLMechanisms.class ) ) {

			SASLMechanisms mechanisms = getFeatures().getFeature( SASLMechanisms.class );

			if( mechanisms.hasMechanism( SASLPlainAuthentication.MECHANISM ) ) {

				xmpp.write( new SASLPlainAuthentication( username, password ) );

				Object result = xmpp.next();

				if( result instanceof SASLFailure ) {
					throw new IOException( ( (SASLFailure) result ).reason );
				}

				if( result instanceof SASLSuccess ) {

					String serviceName = getStream().from();
					xmpp.write( new Stream( serviceName ) );

					Context context = new Context();

					context.stream = xmpp.nextPacket();
					context.features = xmpp.nextPacket();

					contexts.push( context );

					assertEquals( serviceName, context.stream.from() );

				}

			}

		}

	}

	protected void logout() throws IOException {
		if( !contexts.isEmpty() ) {
			xmpp.write( new Presence( generatePacketID(), Presence.Type.UNAVAILABLE ) );
			xmpp.write( "</stream:stream>" );
			contexts.pop();
		}
		fullJID = null;
	}

	private void prepareConnect() throws IOException {
		if( isConnected() ) {
			if( isAuthenticated() ) {
				logout();
			}
			disconnect();
		}
	}

}
