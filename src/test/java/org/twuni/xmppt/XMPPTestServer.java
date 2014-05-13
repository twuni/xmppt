package org.twuni.xmppt;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLServerSocketFactory;

import org.twuni.nio.server.Queue;
import org.twuni.nio.server.Transporter;
import org.twuni.nio.server.auth.AuthenticationException;
import org.twuni.nio.server.auth.Authenticator;
import org.twuni.nio.server.auth.AutomaticAuthenticator;
import org.twuni.xmppt.xmpp.XMPPSocket;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.Failure;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
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

public class XMPPTestServer implements Runnable {

	public static class XMPPClientState {

		public XMPPSocket socket;
		public String username;
		public String resource;
		public String streamID;
		public String serviceName;
		public String sessionID;
		public boolean available;
		public String streamManagementID;
		public boolean streamManagementEnabled;
		public int received;
		public int sent;

		public String fullJID() {
			return String.format( "%s/%s", jid(), resource );
		}

		public boolean hasSession() {
			return sessionID != null;
		}

		public boolean isAvailable() {
			return available;
		}

		public boolean isBound() {
			return resource != null;
		}

		public boolean isStreamManagementEnabled() {
			return streamManagementEnabled;
		}

		public String jid() {
			return String.format( "%s@%s", username, serviceName );
		}

		public void send( Object packet ) throws IOException {
			if( isStreamManagementEnabled() ) {
				if( !StreamManagement.is( packet ) ) {
					sent++;
				}
			}
			socket.write( packet );
		}

	}

	private boolean running;
	private Thread thread;
	private final String serviceName;
	private final Authenticator authenticator;
	private final Transporter transporter;
	private final int port;
	private final boolean secure;
	private ServerSocket acceptor;

	private final Map<String, Queue> unacknowledged = new HashMap<String, Queue>();

	public XMPPTestServer( String serviceName ) {
		this( serviceName, new AutomaticAuthenticator() );
	}

	public XMPPTestServer( String serviceName, Authenticator authenticator ) {
		this( serviceName, authenticator, 5222, false );
	}

	public XMPPTestServer( String serviceName, Authenticator authenticator, int port ) {
		this( serviceName, authenticator, port, false );
	}

	public XMPPTestServer( String serviceName, Authenticator authenticator, int port, boolean secure ) {
		this.serviceName = serviceName;
		this.authenticator = authenticator;
		transporter = new Transporter();
		this.port = port;
		this.secure = secure;
	}

	public String generateStreamID() {
		return Long.toHexString( System.currentTimeMillis() );
	}

	public String generateStreamManagementID() {
		return Long.toHexString( System.currentTimeMillis() );
	}

	private Queue getOrCreateUnacknowledgedPacketQueue( String jid ) {
		Queue q = unacknowledged.get( jid );
		if( q == null ) {
			q = new Queue( jid );
			unacknowledged.put( q.id(), q );
		}
		return q;
	}

	public boolean isRunning() {
		return running && thread != null && thread.isAlive();
	}

	protected void processPacket( XMPPClientState xmpp, Object packet ) throws IOException {

		if( xmpp.isStreamManagementEnabled() ) {
			if( !StreamManagement.is( packet ) ) {
				xmpp.received++;
			}
		}

		String fullJID = xmpp.fullJID();
		String jid = xmpp.jid();

		if( packet instanceof Presence ) {

			Presence presence = (Presence) packet;

			if( presence.id() != null ) {

				if( presence.type() == null ) {
					xmpp.available = true;
					xmpp.send( new Presence( presence.id(), fullJID, fullJID ) );
					transporter.available( xmpp.socket, jid, unacknowledged.get( jid ) );
					sendUnacknowledgedMessages( jid );
				}

			}

			if( Presence.Type.UNAVAILABLE.equals( presence.type() ) ) {
				xmpp.available = false;
				transporter.unavailable( jid );
				xmpp.socket.close();
				throw new EOFException();
			}

			return;

		}

		if( packet instanceof IQ ) {

			IQ iq = (IQ) packet;

			if( xmpp.isAvailable() ) {

				if( iq.expectsResult() ) {
					transporter.transport( iq.result( iq.getContent() ), jid, unacknowledged.get( jid ) );
				}

			} else {

				Bind bind = iq.getContent( Bind.class );
				Session session = iq.getContent( Session.class );

				if( bind != null ) {
					xmpp.resource = bind.resource();
					fullJID = xmpp.fullJID();
					xmpp.send( iq.result( Bind.jid( fullJID ) ) );
				}

				if( session != null ) {
					xmpp.sessionID = generateStreamID();
					xmpp.send( iq.result( iq.getContent() ) );
				}

			}

		}

		if( packet instanceof Message ) {
			Message message = (Message) packet;
			transporter.transport( message.from( jid ), message.to(), unacknowledged.get( jid ) );
		}

		if( packet instanceof Resume ) {
			Resume resume = (Resume) packet;
			if( xmpp.isBound() ) {
				xmpp.send( new Failure( StreamManagement.NAMESPACE ) );
			} else {
				xmpp.streamManagementID = resume.getPreviousID();
				xmpp.send( new Resumed( xmpp.streamManagementID, xmpp.received ) );
				if( xmpp.sent != resume.getH() ) {
					// TODO: Retransmit unacknowledged packets.
				}
			}
		}

		if( packet instanceof Resumed ) {
			Resumed resumed = (Resumed) packet;
			if( xmpp.isBound() ) {
				xmpp.send( new Failure( StreamManagement.NAMESPACE ) );
			} else {
				xmpp.streamManagementID = resumed.getPreviousID();
				if( xmpp.sent != resumed.getH() ) {
					// TODO: Retransmit unacknowledged packets.
				}
			}
		}

		if( packet instanceof Enable ) {
			Enable enable = (Enable) packet;
			if( xmpp.isBound() ) {
				Enabled enabled = null;
				if( enable.supportsSessionResumption() ) {
					xmpp.streamManagementID = generateStreamManagementID();
					enabled = new Enabled( xmpp.streamManagementID, null, enable.getMaximumResumptionTime(), enable.supportsSessionResumption() );
				} else {
					enabled = new Enabled();
				}
				xmpp.send( enabled );
				xmpp.streamManagementEnabled = true;
				getOrCreateUnacknowledgedPacketQueue( jid );
			} else {
				xmpp.send( new Failure( StreamManagement.NAMESPACE ) );
			}
		}

		if( packet instanceof Enabled ) {
			xmpp.streamManagementID = ( (Enabled) packet ).id();
			xmpp.streamManagementEnabled = true;
			getOrCreateUnacknowledgedPacketQueue( jid );
		}

		if( packet instanceof AcknowledgmentRequest ) {
			xmpp.send( new Acknowledgment( xmpp.received ) );
		}

		if( packet instanceof Acknowledgment ) {
			Acknowledgment acknowledgment = (Acknowledgment) packet;
			if( acknowledgment.getH() == getOrCreateUnacknowledgedPacketQueue( jid ).getOffset() ) {
				unacknowledged.clear();
			} else {
				sendUnacknowledgedMessages( jid );
			}
		}

	}

	@Override
	public void run() {

		try {
			acceptor = secure ? SSLServerSocketFactory.getDefault().createServerSocket( port ) : new ServerSocket( port );
		} catch( IOException exception ) {
			exception.printStackTrace();
			return;
		}

		// TODO: Let's find out what our capabilities are, and let's compute the hash for real.
		CapabilitiesHash capabilities = new CapabilitiesHash( serviceName, CapabilitiesHash.HASH_SHA1, "yKzHls8GRkBRR5a35o/IZmOtpBU=" );

		while( running ) {

			try {

				XMPPClientState xmpp = new XMPPClientState();

				xmpp.serviceName = serviceName;

				xmpp.socket = new XMPPSocket( acceptor.accept() );
				xmpp.socket.setLogger( null );

				Stream stream = null;

				stream = xmpp.socket.nextPacket();

				xmpp.streamID = generateStreamID();

				xmpp.socket.write( new Stream( null, serviceName, xmpp.streamID ) );

				if( !serviceName.equals( stream.to() ) ) {
					xmpp.socket.write( new StreamError( "<host-unknown/>" ) );
					xmpp.socket.close();
					continue;
				}

				xmpp.socket.write( new Features( new SASLMechanisms( SASLPlainAuthentication.MECHANISM ), capabilities ) );

				SASLPlainAuthentication credentials = xmpp.socket.nextPacket();

				xmpp.username = credentials.getAuthenticationString();

				try {
					authenticator.checkCredential( credentials.getAuthorizationString(), credentials.getPassword() );
				} catch( AuthenticationException exception ) {
					xmpp.socket.write( new SASLFailure( SASLFailure.REASON_NOT_AUTHORIZED ) );
					xmpp.socket.close();
					continue;
				}

				xmpp.socket.write( new SASLSuccess() );

				stream = xmpp.socket.nextPacket();
				xmpp.streamID = generateStreamID();

				xmpp.socket.write( new Stream( null, serviceName, xmpp.streamID ) );

				if( !serviceName.equals( stream.to() ) ) {
					xmpp.socket.write( new StreamError( "<host-unknown/>" ) );
					xmpp.socket.close();
					continue;
				}

				xmpp.socket.write( new Features( new Bind(), new Session(), new StreamManagement(), capabilities ) );

				while( running ) {
					processPacket( xmpp, xmpp.socket.next() );
				}

			} catch( IOException exception ) {
				// Ignore.
			}

		}

		stop();

	}

	private void sendUnacknowledgedMessages( String jid ) {
		Queue q = unacknowledged.get( jid );
		if( q != null ) {
			Iterator<Object> it = q.iterator();
			while( it.hasNext() ) {
				transporter.transport( it.next(), jid );
			}
		}
	}

	public void startListening() {
		if( thread != null && thread.isAlive() ) {
			return;
		}
		running = true;
		thread = new Thread( this, String.format( "%s [%s]", getClass().getName(), serviceName ) );
		thread.start();
	}

	private void stop() {
		try {
			if( acceptor != null && acceptor.isBound() && !acceptor.isClosed() ) {
				acceptor.close();
			}
		} catch( IOException exception ) {
			// Ignore.
		}
	}

	public void stopListening() {
		running = false;
		stop();
		if( thread != null ) {
			thread.interrupt();
			try {
				thread.join();
			} catch( InterruptedException exception ) {
				// Ignore.
			}
			thread = null;
		}
	}

}
