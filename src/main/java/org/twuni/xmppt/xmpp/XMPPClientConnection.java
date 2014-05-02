package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.util.Stack;

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
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamError;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public class XMPPClientConnection {

	public static class Builder {

		private SocketFactory socketFactory = SocketFactory.getInstance();
		private String host = "localhost";
		private int port = 5222;
		private boolean secure = false;
		private boolean log = true;
		private String serviceName = "localhost";
		private String resourceName = "default";
		private String userName;
		private String password;
		private PacketListener packetListener;
		private ConnectionListener connectionListener;

		public XMPPClientConnection build() throws IOException {

			XMPPClientConnection connection = new XMPPClientConnection();

			connection.setConnectionListener( connectionListener );
			connection.connect( socketFactory, host, port, secure, serviceName, log );
			connection.login( userName, password );
			connection.bind( resourceName );
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
			return this;
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

		public Builder socketFactory( SocketFactory socketFactory ) {
			this.socketFactory = socketFactory;
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

		public Stream stream;
		public Features features;
		public int sequence;
		public int sent;
		public int received;
		public boolean streamManagementEnabled;
		public String userName;
		public String serviceName;
		public String resourceName;
		public String fullJID;

		public String nextID() {
			sequence++;
			return String.format( "%s-%d", stream.id(), Integer.valueOf( sequence ) );
		}

	}

	public static interface PacketListener {

		public void onException( XMPPClientConnection connection, Throwable exception );

		public void onPacketReceived( XMPPClientConnection connection, Object packet );

	}

	private final Stack<Context> contexts = new Stack<Context>();
	private XMPPSocket socket;
	private Thread packetListenerThread;
	private ConnectionListener connectionListener;

	public void bind( String resourceName ) throws IOException {

		String id = null;

		if( isFeatureAvailable( Bind.class ) ) {

			id = generatePacketID();
			send( new IQ( id, IQ.TYPE_SET, null, null, Bind.resource( resourceName ) ) );

			IQ bindIQ = nextPacket();

			Bind bind = bindIQ.getContent( Bind.class );

			getContext().fullJID = bind.jid();

			enableStreamManagement();

			if( isFeatureAvailable( Session.class ) ) {

				id = generatePacketID();
				send( new IQ( id, IQ.TYPE_SET, null, null, new Session() ) );

				nextPacket( IQ.class );

			}

			id = generatePacketID();
			send( new Presence( id ) );

			nextPacket( Presence.class );

			getContext().resourceName = resourceName;

			dispatchOnConnected();

		}

	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure ) throws IOException {
		prepareConnect();
		socket = new XMPPSocket( socketFactory, host, port, secure );
	}

	public void connect( SocketFactory socketFactory, String host, int port, boolean secure, String serviceName, boolean loggingEnabled ) throws IOException {
		connect( socketFactory, host, port, secure );
		if( !loggingEnabled ) {
			socket.setLogger( null );
		}
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

		if( socket != null ) {
			socket.flush();
			socket.close();
			socket = null;
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

	protected void enableStreamManagement() throws IOException {

		if( isFeatureAvailable( StreamManagement.class ) ) {

			send( new Enable() );

			Enabled enabled = nextPacket( Enabled.class );

			Context context = getContext();

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

	private boolean isStreamManagementEnabled() {
		Context context = getContext();
		return isFeatureAvailable( StreamManagement.class ) && context != null && context.streamManagementEnabled;
	}

	public void login( String username, String password ) throws IOException {

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
					// TODO: Resend unacknowledged messages.
				} else {
					// TODO: Acknowledge sent messages.
				}
			}
			return;
		}

		packetListener.onPacketReceived( this, packet );

	}

	public void send( Object packet ) throws IOException {
		socket.write( packet );
		if( isStreamManagementEnabled() ) {
			if( !StreamManagement.is( packet ) ) {
				getContext().sent++;
			}
		}
	}

	public void setConnectionListener( ConnectionListener connectionListener ) {
		this.connectionListener = connectionListener;
	}

	public void startListening( final PacketListener packetListener ) {

		if( !( isConnected() && isAuthenticated() ) ) {
			throw new IllegalStateException();
		}

		stopListening();

		packetListenerThread = new Thread() {

			@Override
			public void run() {
				while( !interrupted() ) {
					try {
						Object packet = next();
						if( !isInterrupted() ) {
							processPacket( packetListener, packet );
						}
					} catch( IOException exception ) {
						if( !isInterrupted() ) {
							packetListener.onException( XMPPClientConnection.this, exception );
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

}
