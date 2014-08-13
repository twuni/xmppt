package org.twuni.xmppt.xmpp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import org.twuni.Logger;
import org.twuni.xmppt.client.SocketFactory;
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

public class XMPPClientConnection {

	public static interface AcknowledgmentListener {

		public void onFailedAcknowledgment( XMPPClientConnection connection, int expected, int actual );

		public void onSuccessfulAcknowledgment( XMPPClientConnection connection );

	}

	public static class Builder {

		private SocketFactory socketFactory = SocketFactory.getInstance();
		private String host = "localhost";
		private int port = 5222;
		private int sessionResumptionTimeout = 300;
		private boolean streamManagementRequested = true;
		private boolean secure = false;
		private Logger logger;
		private boolean log = true;
		private String serviceName = "localhost";
		private String resourceName = "default";
		private String userName;
		private String password;
		private InputStream state;
		private AcknowledgmentListener acknowledgmentListener;
		private PacketListener packetListener;
		private ConnectionListener connectionListener;

		public Builder acknowledgmentListener( AcknowledgmentListener acknowledgmentListener ) {
			this.acknowledgmentListener = acknowledgmentListener;
			return this;
		}

		public XMPPClientConnection build() throws IOException {

			XMPPClientConnection connection = new XMPPClientConnection();

			connection.setStreamManagementRequested( streamManagementRequested );
			connection.setAcknowledgmentListener( acknowledgmentListener );
			connection.setConnectionListener( connectionListener );

			if( logger != null ) {
				connection.connect( socketFactory, host, port, secure, serviceName, logger );
			} else {
				connection.connect( socketFactory, host, port, secure, serviceName, log );
			}

			connection.login( userName, password );

			if( state != null ) {
				connection.restoreState( state );
			} else {
				connection.bind( resourceName, sessionResumptionTimeout );
			}

			connection.startListening( packetListener );

			return connection;

		}

		public Builder connectionListener( ConnectionListener connectionListener ) {
			this.connectionListener = connectionListener;
			return this;
		}

		public Builder host( String host ) {
			this.host = host;
			return this;
		}

		public Builder log( boolean log ) {
			this.log = log;
			return log ? this : logger( null );
		}

		public Builder logger( Logger logger ) {
			this.logger = logger;
			return log( logger != null );
		}

		public Builder packetListener( PacketListener packetListener ) {
			this.packetListener = packetListener;
			return this;
		}

		public Builder password( String password ) {
			this.password = password;
			return this;
		}

		public Builder port( int port ) {
			this.port = port;
			return this;
		}

		public Builder requestStreamManagement() {
			return requestStreamManagement( true );
		}

		public Builder requestStreamManagement( boolean streamManagementRequested ) {
			this.streamManagementRequested = streamManagementRequested;
			return this;
		}

		public Builder resourceName( String resourceName ) {
			this.resourceName = resourceName;
			return this;
		}

		public Builder secure( boolean secure ) {
			this.secure = secure;
			return this;
		}

		public Builder serviceName( String serviceName ) {
			this.serviceName = serviceName;
			return this;
		}

		public Builder sessionResumptionTimeout( int timeout ) {
			sessionResumptionTimeout = timeout;
			return this;
		}

		public Builder socketFactory( SocketFactory socketFactory ) {
			this.socketFactory = socketFactory;
			return this;
		}

		public Builder state( byte [] state ) throws IOException {
			return state != null ? state( state, 0, state.length ) : state( (InputStream) null );
		}

		public Builder state( byte [] state, int offset, int length ) throws IOException {
			return state( state != null ? new ByteArrayInputStream( state, offset, length ) : null );
		}

		public Builder state( InputStream state ) throws IOException {
			this.state = state;
			return this;
		}

		public Builder userName( String userName ) {
			this.userName = userName;
			return this;
		}

	}

	public static interface ConnectionListener {

		public void onConnected( XMPPClientConnection connection );

		public void onDisconnected( XMPPClientConnection connection );

	}

	public static class Context {

		private static final int VERSION = 1;

		private static String readUTF( DataInputStream in ) throws IOException {
			int length = in.readInt();
			return length > 0 ? in.readUTF() : null;
		}

		public static Context restore( InputStream in ) throws IOException {
			Context context = new Context();
			context.load( in );
			return context;
		}

		private static void writeUTF( DataOutputStream d, String in ) throws IOException {
			if( in == null ) {
				d.writeInt( 0 );
			} else {
				d.writeInt( in.length() );
				d.writeUTF( in );
			}
		}

		public Stream stream;
		public Features features;
		public String location;
		public int sequence;
		public int sent;
		public int received;
		public int sessionResumptionTimeout;
		public String streamManagementID;
		public boolean streamManagementEnabled;
		public String userName;
		public String serviceName;
		public String resourceName;
		public String fullJID;

		public boolean isResumable() {
			return streamManagementID != null;
		}

		public void load( Context c ) {
			location = c.location;
			sequence = c.sequence;
			sent = c.sent;
			received = c.received;
			sessionResumptionTimeout = c.sessionResumptionTimeout;
			streamManagementID = c.streamManagementID;
			streamManagementEnabled = c.streamManagementEnabled;
			userName = c.userName;
			serviceName = c.serviceName;
			resourceName = c.resourceName;
			fullJID = c.fullJID;
		}

		public void load( InputStream in ) throws IOException {

			DataInputStream d = new DataInputStream( in );

			int version = d.readInt();

			switch( version ) {

				case 1:

					sequence = d.readInt();
					sent = d.readInt();
					received = d.readInt();
					sessionResumptionTimeout = d.readInt();
					location = readUTF( d );
					streamManagementID = readUTF( d );
					resourceName = readUTF( d );
					fullJID = readUTF( d );

					break;

			}

		}

		public String nextID() {
			sequence++;
			return String.format( "%s-%d", stream != null ? stream.id() : "_____", Integer.valueOf( sequence ) );
		}

		public void save( OutputStream out ) throws IOException {

			DataOutputStream d = new DataOutputStream( out );

			d.writeInt( VERSION );

			d.writeInt( sequence );
			d.writeInt( sent );
			d.writeInt( received );
			d.writeInt( sessionResumptionTimeout );

			writeUTF( d, location );
			writeUTF( d, streamManagementID );
			writeUTF( d, resourceName );
			writeUTF( d, fullJID );

		}

	}

	public static interface PacketListener {

		public void onException( XMPPClientConnection connection, Throwable exception );

		public void onPacketReceived( XMPPClientConnection connection, Object packet );

	}

	private final Stack<Context> contexts = new Stack<Context>();
	private XMPPSocket socket;
	private Thread packetListenerThread;
	private AcknowledgmentListener acknowledgmentListener;
	private ConnectionListener connectionListener;
	private boolean streamManagementRequested = true;

	public void bind( String resourceName ) throws IOException {
		bind( resourceName, 0 );
	}

	public void bind( String resourceName, int sessionResumptionTimeout ) throws IOException {

		String id = null;

		if( isFeatureAvailable( Bind.class ) ) {

			id = generatePacketID();
			send( new IQ( id, IQ.TYPE_SET, null, null, Bind.resource( resourceName ) ) );

			IQ bindIQ = nextPacket();

			Bind bind = bindIQ.getContent( Bind.class );

			getContext().fullJID = bind.jid();

			enableStreamManagement( sessionResumptionTimeout );

			if( isFeatureAvailable( Session.class ) ) {

				id = generatePacketID();
				send( new IQ( id, IQ.TYPE_SET, null, null, new Session() ) );

				nextPacket( IQ.class );

			}

			id = generatePacketID();
			send( new Presence( id ) );

			getContext().resourceName = resourceName;

			dispatchOnConnected();

		}

	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure ) throws IOException {
		prepareConnect();
		socket = new XMPPSocket( socketFactory, host, port, secure );
	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure, String serviceName ) throws IOException {
		connect( socketFactory, host, port, secure, serviceName, true );
	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure, String serviceName, boolean loggingEnabled ) throws IOException {
		connect( socketFactory, host, port, secure );
		if( !loggingEnabled ) {
			socket.setLogger( null );
		}
		connect( serviceName );
	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure, String serviceName, Logger logger ) throws IOException {
		connect( socketFactory, host, port, secure );
		socket.setLogger( logger );
		connect( serviceName );
	}

	private void connect( String serviceName ) throws IOException {

		send( new Stream( serviceName ) );

		Context context = new Context();

		context.serviceName = serviceName;
		context.stream = nextPacket();

		Object packet = next();

		if( packet instanceof StreamError ) {
			throw new IOException( ( (StreamError) packet ).content.toString() );
		}

		if( packet instanceof Features ) {
			context.features = (Features) packet;
		}

		contexts.push( context );

	}

	public void connect( String host, int port, boolean secure ) throws IOException {
		prepareConnect();
		socket = new XMPPSocket( host, port, secure );
	}

	public void connect( String host, int port, boolean secure, String serviceName ) throws IOException {
		connect( host, port, secure, serviceName, false );
	}

	public void connect( String host, int port, boolean secure, String serviceName, boolean loggingEnabled ) throws IOException {
		connect( host, port, secure );
		if( !loggingEnabled ) {
			socket.setLogger( null );
		}
		connect( serviceName );
	}

	public void disconnect() throws IOException {

		stopListening();

		if( isAuthenticated() ) {
			logout();
		}

		synchronized( contexts ) {
			while( !contexts.isEmpty() ) {
				if( socket != null ) {
					try {
						send( getStream().close() );
					} catch( IOException exception ) {
						// Socket must be closed. That's fine.
					}
				}
				contexts.pop();
			}
		}

		terminate();

	}

	private void dispatchFailedAcknowledgment( int expected, int actual ) {
		if( acknowledgmentListener != null ) {
			acknowledgmentListener.onFailedAcknowledgment( this, expected, actual );
		}
	}

	protected void dispatchOnConnected() {
		if( connectionListener != null ) {
			connectionListener.onConnected( this );
		}
	}

	protected void dispatchOnDisconnected() {
		if( connectionListener != null ) {
			connectionListener.onDisconnected( this );
		}
	}

	private void dispatchSuccessfulAcknowledgment() {
		if( acknowledgmentListener != null ) {
			acknowledgmentListener.onSuccessfulAcknowledgment( this );
		}
	}

	protected void enableStreamManagement( int sessionResumptionTimeout ) throws IOException {

		if( streamManagementRequested && isFeatureAvailable( StreamManagement.class ) ) {

			send( new Enable( sessionResumptionTimeout, sessionResumptionTimeout != 0 ) );

			Enabled enabled = nextPacket();

			Context context = getContext();

			context.location = enabled.getLocation();
			context.streamManagementID = enabled.id();
			context.streamManagementEnabled = true;
			context.received = 0;
			context.sent = 0;

		}

	}

	protected String generatePacketID() {
		Context context = getContext();
		return context != null ? context.nextID() : Long.toHexString( System.currentTimeMillis() );
	}

	private Context getContext() {
		return contexts.isEmpty() ? null : contexts.peek();
	}

	protected <T> T getFeature( Class<T> featureType ) {
		return getFeatures().getFeature( featureType );
	}

	protected Features getFeatures() {
		Context context = getContext();
		return context != null ? context.features : null;
	}

	protected Stream getStream() {
		Context context = getContext();
		return context != null ? context.stream : null;
	}

	public boolean isAuthenticated() {
		Context context = getContext();
		return context != null && context.fullJID != null;
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	public boolean isFeatureAvailable( Class<?> feature ) {
		Features features = getFeatures();
		return features != null && features.hasFeature( feature );
	}

	private boolean isResumable() {
		Context context = getContext();
		return context != null && context.isResumable();
	}

	private boolean isStreamManagementEnabled() {
		Context context = getContext();
		return streamManagementRequested && isFeatureAvailable( StreamManagement.class ) && context != null && context.streamManagementEnabled;
	}

	public void login( String username, String password ) throws IOException {

		if( isFeatureAvailable( SASLMechanisms.class ) ) {

			SASLMechanisms mechanisms = getFeature( SASLMechanisms.class );

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

					context.serviceName = serviceName;
					context.stream = nextPacket();
					context.features = nextPacket();
					context.userName = username;

					contexts.push( context );

				}

			}

		}

	}

	public void logout() throws IOException {
		stopListening();
		if( !contexts.isEmpty() ) {
			send( new Presence( generatePacketID(), Presence.Type.UNAVAILABLE ) );
			send( getStream().close() );
			contexts.pop();
		}
	}

	public void markForRetransmission( int count ) {
		Context context = getContext();
		if( context != null ) {
			if( count <= context.sent ) {
				context.sent -= count;
			}
		}
	}

	protected Object next() throws IOException {
		return ok( socket.next() );
	}

	protected <T> T nextPacket() throws IOException {
		T packet = socket.nextPacket();
		return ok( packet );
	}

	protected <T> T nextPacket( Class<T> type ) throws IOException {
		return ok( socket.nextPacket( type ) );
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

	protected void processPacket( PacketListener packetListener, Object packet ) throws IOException {

		if( StreamManagement.is( packet ) && isStreamManagementEnabled() ) {
			if( packet instanceof AcknowledgmentRequest ) {
				send( new Acknowledgment( getContext().received ) );
			} else if( packet instanceof Acknowledgment ) {
				Acknowledgment ack = (Acknowledgment) packet;
				if( getContext().sent != ack.getH() ) {
					dispatchFailedAcknowledgment( getContext().sent, ack.getH() );
				} else {
					dispatchSuccessfulAcknowledgment();
				}
			}
			return;
		}

		packetListener.onPacketReceived( this, packet );

	}

	public void restoreState( byte [] state ) throws IOException {
		restoreState( state, 0, state.length );
	}

	public void restoreState( byte [] state, int offset, int length ) throws IOException {
		restoreState( new ByteArrayInputStream( state, offset, length ) );
	}

	public void restoreState( InputStream in ) throws IOException {

		Context previousContext = Context.restore( in );

		if( !isConnected() ) {
			// Connect.
		}

		if( !isAuthenticated() ) {
			// Authenticate.
		}

		Context context = getContext();

		if( context == null || context.userName == null ) {
			throw new IllegalStateException( "The stream must be authenticated before attempting to restore state." );
		}

		resume( previousContext );

	}

	private void resume( Context previousContext ) throws IOException {

		if( isFeatureAvailable( StreamManagement.class ) ) {

			if( previousContext != null ) {

				if( !previousContext.isResumable() ) {
					bind( previousContext.resourceName, previousContext.sessionResumptionTimeout );
					return;
				}

				send( new Resume( previousContext.streamManagementID, previousContext.received ) );

				Object response = nextPacket();

				if( !( response instanceof Resumed ) ) {
					bind( previousContext.resourceName, previousContext.sessionResumptionTimeout );
					return;
				}

				Context context = getContext();
				Resumed resumed = (Resumed) response;

				context.load( previousContext );

				context.streamManagementEnabled = true;
				context.streamManagementID = resumed.getPreviousID();

				dispatchOnConnected();

				if( context.sent != resumed.getH() ) {
					dispatchFailedAcknowledgment( context.sent, resumed.getH() );
				} else {
					dispatchSuccessfulAcknowledgment();
				}

			}

		}

	}

	public byte [] saveState() throws IOException {
		if( !isResumable() ) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		saveState( out );
		return out.toByteArray();
	}

	public void saveState( OutputStream out ) throws IOException {
		if( !isResumable() ) {
			return;
		}
		Context context = getContext();
		if( context == null ) {
			context = new Context();
		}
		context.save( out );
	}

	public void send( Object... packets ) throws IOException {
		for( Object packet : packets ) {
			socket.write( packet );
			if( isStreamManagementEnabled() ) {
				if( !StreamManagement.is( packet ) ) {
					getContext().sent++;
				}
			}
		}
	}

	public void sendAcknowledgment() throws IOException {
		if( isStreamManagementEnabled() ) {
			send( new Acknowledgment( getContext().received ) );
		}
	}

	public void setAcknowledgmentListener( AcknowledgmentListener acknowledgmentListener ) {
		this.acknowledgmentListener = acknowledgmentListener;
	}

	public void setConnectionListener( ConnectionListener connectionListener ) {
		this.connectionListener = connectionListener;
	}

	public void setStreamManagementRequested( boolean streamManagementRequested ) {
		this.streamManagementRequested = streamManagementRequested;
	}

	public void startListening( final PacketListener packetListener ) {

		if( !( isConnected() && isAuthenticated() ) ) {
			throw new IllegalStateException();
		}

		stopListening();

		packetListenerThread = new Thread( String.format( "%s:%s", packetListener.getClass().getName(), getContext().fullJID ) ) {

			@Override
			public void run() {

				while( !interrupted() ) {

					try {

						Object packet = next();

						if( packet == null ) {
							disconnect();
							dispatchOnDisconnected();
							break;
						}

						if( !isInterrupted() ) {
							processPacket( packetListener, packet );
						}

					} catch( IOException exception ) {

						if( !isInterrupted() ) {
							packetListener.onException( XMPPClientConnection.this, exception );
						}

						try {
							terminate();
						} catch( IOException ignore ) {
							// Ignore.
						}

						if( !isConnected() ) {
							dispatchOnDisconnected();
						}

						break;

					}

				}

			}

		};

		packetListenerThread.start();

	}

	public void stopListening() {
		if( packetListenerThread != null ) {
			packetListenerThread.interrupt();
			packetListenerThread = null;
		}
	}

	public void terminate() throws IOException {
		if( socket != null ) {
			socket.flush();
			socket.close();
			socket = null;
		}
	}

}
