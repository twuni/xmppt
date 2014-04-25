package org.twuni.xmppt;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

import org.twuni.nio.server.Transporter;
import org.twuni.nio.server.auth.AuthenticationException;
import org.twuni.nio.server.auth.Authenticator;
import org.twuni.nio.server.auth.AutomaticAuthenticator;
import org.twuni.xmppt.xmpp.XMPPSocket;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
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

		public String jid() {
			return String.format( "%s@%s", username, serviceName );
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

	public boolean isRunning() {
		return running && thread != null && thread.isAlive();
	}

	protected void processPacket( XMPPClientState xmpp, Object packet ) throws IOException {

		if( packet instanceof Presence ) {

			Presence presence = (Presence) packet;

			if( presence.id() != null ) {

				if( xmpp.isAvailable() ) {
					xmpp.received++;
				}

				if( presence.type() == null ) {
					xmpp.socket.write( new Presence( presence.id(), xmpp.fullJID(), xmpp.fullJID() ) );
					xmpp.available = true;
					transporter.available( xmpp.socket, xmpp.jid() );
					processPacket( xmpp, new Acknowledgment( xmpp.sent ) );
				}

			}

			if( Presence.Type.UNAVAILABLE.equals( presence.type() ) ) {
				xmpp.available = false;
				transporter.unavailable( xmpp.jid() );
				xmpp.socket.close();
				throw new EOFException();
			}

			return;

		}

		if( packet instanceof IQ ) {

			IQ iq = (IQ) packet;

			if( xmpp.isAvailable() ) {

				if( iq.id() != null ) {
					xmpp.received++;
				}

				if( iq.expectsResult() ) {
					xmpp.socket.write( iq.result( iq.getContent() ) );
					xmpp.sent++;
				}

			} else {

				Bind bind = iq.getContent( Bind.class );
				Session session = iq.getContent( Session.class );

				if( bind != null ) {
					xmpp.resource = bind.resource();
					xmpp.socket.write( iq.result( Bind.jid( xmpp.fullJID() ) ) );
				}

				if( session != null ) {
					xmpp.sessionID = generateStreamID();
					xmpp.socket.write( iq.result( iq.getContent() ) );
				}

			}

		}

		if( packet instanceof Message ) {
			Message message = (Message) packet;
			if( message.id() != null ) {
				xmpp.received++;
			}
			transporter.transport( new Message( message.id(), message.type(), xmpp.jid(), xmpp.jid(), message.getContent() ) );
		}

		if( packet instanceof AcknowledgmentRequest ) {
			xmpp.socket.write( new Acknowledgment( xmpp.received ) );
		}

		if( packet instanceof Acknowledgment ) {
			Acknowledgment acknowledgment = (Acknowledgment) packet;
			transporter.acknowledge( xmpp.jid(), acknowledgment.getH() );
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
		thread.interrupt();
		stop();
		try {
			thread.join();
		} catch( InterruptedException exception ) {
			// Ignore.
		}
		thread = null;
	}

}
