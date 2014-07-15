package org.twuni.xmppt;

import java.io.IOException;
import java.util.Stack;

import org.junit.Assert;
import org.twuni.xmppt.xmpp.PacketListener;
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
import org.twuni.xmppt.xmpp.stream.Enable;
import org.twuni.xmppt.xmpp.stream.Enabled;
import org.twuni.xmppt.xmpp.stream.Resume;
import org.twuni.xmppt.xmpp.stream.Resumed;
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamError;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public class XMPPClientTestFixture extends Assert {

	protected static class Context {

		public Stream stream;
		public Features features;
		public int sequence;
		public int sent;
		public int received;
		public String streamManagementID;
		public boolean streamManagementEnabled;

		public String nextID() {
			sequence++;
			return String.format( "%s-%d", stream.id(), Integer.valueOf( sequence ) );
		}

	}

	private final Stack<Context> contexts = new Stack<Context>();
	private String fullJID;
	private XMPPSocket xmpp;

	protected void assertFeatureAvailable( Class<?> feature ) {
		assertTrue( String.format( "Feature not available: %s", feature.getName() ), isFeatureAvailable( feature ) );
	}

	protected void assertPacketsReceived( int h ) throws IOException {

		send( new AcknowledgmentRequest() );

		Acknowledgment ack = nextPacket( Acknowledgment.class, new PacketListener() {

			@Override
			public void onPacketException( Throwable exception ) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPacketReceived( Object packet ) {
				if( packet instanceof AcknowledgmentRequest ) {
					try {
						sendAcknowledgment();
					} catch( IOException exception ) {
						// Ignore.
					}
				} else {
					ok( packet );
				}
			}

			@Override
			public void onPacketSent( Object packet ) {
				// TODO Auto-generated method stub
			}

		} );

		assertEquals( h, ack.getH() );

	}

	protected void assertPacketsSentWereReceived() throws IOException {
		assertPacketsReceived( getContext().sent );
	}

	protected void bind() throws IOException {
		bind( getResourceName() );
	}

	protected void bind( String resourceName ) throws IOException {

		String id = null;

		if( isFeatureAvailable( Bind.class ) ) {

			id = generatePacketID();
			send( new IQ( id, IQ.TYPE_SET, null, null, Bind.resource( resourceName ) ) );

			IQ bindIQ = nextPacket();

			assertEquals( id, bindIQ.id() );
			assertEquals( IQ.TYPE_RESULT, bindIQ.type() );

			Bind bind = bindIQ.getContent( Bind.class );

			fullJID = bind.jid();
			assertNotNull( "Server failed to provide a bound JID.", fullJID );

			enableStreamManagement();

			if( isFeatureAvailable( Session.class ) ) {

				id = generatePacketID();
				send( new IQ( id, IQ.TYPE_SET, null, null, new Session() ) );

				IQ sessionIQ = nextPacket();

				assertEquals( id, sessionIQ.id() );
				assertEquals( IQ.TYPE_RESULT, sessionIQ.type() );

			}

			id = generatePacketID();
			send( new Presence( id ) );

			assertPacketsSentWereReceived();

		}

	}

	protected void connect() throws IOException {
		connect( getHost(), getPort(), isSecure(), getServiceName() );
	}

	protected void connect( String host, int port, boolean secure, String serviceName ) throws IOException {

		prepareConnect();

		xmpp = new XMPPSocket( host, port, secure );

		send( new Stream( serviceName ) );

		Context context = new Context();

		context.stream = nextPacket();

		Object packet = next();

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
				try {
					send( getStream().close() );
				} catch( IOException exception ) {
					// Socket must be closed. That's fine.
				}
			}
			contexts.pop();
		}

		invokeConnectionLoss();

	}

	protected void enableStreamManagement() throws IOException {

		if( isFeatureAvailable( StreamManagement.class ) ) {

			send( new Enable( 30, true ) );

			Enabled enabled = nextPacket();

			getContext().streamManagementID = enabled.id();
			getContext().streamManagementEnabled = true;
			getContext().received = 0;
			getContext().sent = 0;

		}

	}

	protected String generatePacketID() {
		Context context = getContext();
		return context != null ? context.nextID() : Long.toHexString( System.currentTimeMillis() );
	}

	protected Context getContext() {
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

	protected int getReceivedPacketCount() {
		return getContext().received;
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

	protected void goOnline( Context previousContext ) throws IOException {
		connect();
		login();
		resume( previousContext );
	}

	protected void goOnline( String resourceName ) throws IOException {
		connect();
		login();
		bind( resourceName );
	}

	protected void invokeConnectionLoss() throws IOException {
		contexts.clear();
		if( xmpp != null ) {
			xmpp.flush();
			xmpp.close();
			xmpp = null;
		}
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

				send( new SASLPlainAuthentication( username, password ) );

				Object result = next();

				if( result instanceof SASLFailure ) {
					throw new IOException( ( (SASLFailure) result ).reason );
				}

				if( result instanceof SASLSuccess ) {

					String serviceName = getStream().from();
					send( new Stream( serviceName ) );

					Context context = new Context();

					context.stream = nextPacket();
					context.features = nextPacket();

					contexts.push( context );

					assertEquals( serviceName, context.stream.from() );

				}

			}

		}

	}

	protected void logout() throws IOException {
		if( !contexts.isEmpty() ) {
			send( new Presence( generatePacketID(), Presence.Type.UNAVAILABLE ) );
			send( getStream().close() );
			contexts.pop();
		}
		fullJID = null;
	}

	protected Object next() throws IOException {
		return ok( xmpp.next() );
	}

	protected <T> T nextPacket() throws IOException {
		T packet = xmpp.nextPacket();
		return ok( packet );
	}

	protected <T> T nextPacket( Class<T> type ) throws IOException {
		return ok( xmpp.nextPacket( type ) );
	}

	protected <T> T nextPacket( Class<T> type, PacketListener until ) throws IOException {
		return ok( xmpp.nextPacket( type, until ) );
	}

	private <T> T ok( T packet ) {
		Context context = getContext();
		if( context != null ) {
			if( context.streamManagementEnabled ) {
				if( isFeatureAvailable( StreamManagement.class ) ) {
					if( !StreamManagement.is( packet ) ) {
						context.received++;
					}
				}
			}
		}
		return packet;
	}

	private void prepareConnect() throws IOException {
		if( isConnected() ) {
			if( isAuthenticated() ) {
				logout();
			}
			disconnect();
		}
	}

	protected void resume( Context previousContext ) throws IOException {

		if( isFeatureAvailable( StreamManagement.class ) ) {

			if( previousContext != null ) {

				send( new Resume( previousContext.streamManagementID, previousContext.received ) );
				Resumed resumed = nextPacket();

				Context context = getContext();

				context.streamManagementEnabled = true;
				context.streamManagementID = resumed.getPreviousID();
				context.received = previousContext.received;
				context.sent = previousContext.sent;

				assertEquals( context.streamManagementID, resumed.getPreviousID() );
				assertEquals( context.sent, resumed.getH() );

			}

		}

	}

	protected void send( Object packet ) throws IOException {
		xmpp.write( packet );
		Context context = getContext();
		if( context != null ) {
			if( context.streamManagementEnabled ) {
				if( isFeatureAvailable( StreamManagement.class ) ) {
					if( !StreamManagement.is( packet ) ) {
						context.sent++;
					}
				}
			}
		}
	}

	protected void sendAcknowledgment() throws IOException {
		send( new Acknowledgment( getContext().received ) );
	}

}
