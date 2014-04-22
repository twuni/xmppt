package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.ping.Ping;
import org.twuni.xmppt.xmpp.sasl.SASLAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLMechanisms;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;
import org.twuni.xmppt.xmpp.session.Session;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public class XMPPClientConnection implements PacketListener, Runnable {

	private final String serviceName;
	private final String localAddress;
	private final String remoteAddress;
	private final XMPPStreamWriter w;
	private final XMPPStreamReader r;

	private String username;
	private String jid;
	private String jidWithResource;
	private int ch = 0;
	private int sh = 0;

	public XMPPClientConnection( String serviceName, Socket socket ) throws IOException {
		this( serviceName, String.format( "%s:%d", socket.getLocalAddress().getHostAddress(), Integer.valueOf( socket.getLocalPort() ) ), String.format( "%s:%d", socket.getInetAddress().getHostAddress(), Integer.valueOf( socket.getPort() ) ), socket.getInputStream(), socket.getOutputStream() );
	}

	public XMPPClientConnection( String serviceName, String localAddress, String remoteAddress, InputStream in, OutputStream out ) {
		this.serviceName = serviceName;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		r = new XMPPStreamReader( in, XMPPPacketConfiguration.getDefault(), this );
		w = new XMPPStreamWriter( out );
		onConnected();
	}

	public boolean matchesJID( String jid ) {
		return this.jid != null && jid != null && this.jid.equals( jid );
	}

	public boolean matchesUsername( String username ) {
		return this.username != null && username != null && this.username.equals( username );
	}

	@Override
	public void onPacketReceived( Object packet ) {

		try {

			if( packet instanceof Stream ) {
				onStream( (Stream) packet );
			}

			if( packet instanceof SASLAuthentication ) {
				onSASLAuthentication( (SASLAuthentication) packet );
			}

			if( packet instanceof IQ ) {
				onIQ( (IQ) packet );
			}

			if( packet instanceof Presence ) {
				onPresence( (Presence) packet );
			}

			if( packet instanceof Message ) {
				onMessage( (Message) packet );
			}

			if( packet instanceof AcknowledgmentRequest ) {
				onAcknowledgmentRequest( (AcknowledgmentRequest) packet );
			}

			if( packet instanceof Acknowledgment ) {
				onAcknowledgment( (Acknowledgment) packet );
			}

		} catch( IOException exception ) {
			onPacketException( exception );
		}

	}

	protected void onAcknowledgment( Acknowledgment acknowledgment ) throws IOException {
		if( acknowledgment.getH() != sh ) {
			send( new org.twuni.xmppt.xmpp.core.Error( StreamManagement.NAMESPACE ) );
		}
	}

	protected void onAcknowledgmentRequest( AcknowledgmentRequest request ) throws IOException {
		send( new Acknowledgment( ch ) );
	}

	protected void onMessage( Message message ) throws IOException {
		ch++;
		send( new Acknowledgment( ch ) );
	}

	public boolean isAuthenticated() {
		return username != null;
	}

	protected void onStream( Stream stream ) throws IOException {
		if( !serviceName.equals( stream.to() ) ) {
			send( new org.twuni.xmppt.xmpp.core.Error( Stream.NAMESPACE ) );
			return;
		}
		if( !isAuthenticated() ) {
			send( new Stream( null, serviceName, "1234567890", "stream", "1.0" ) );
			send( new Features( "stream", new SASLMechanisms( "PLAIN" ) ) );
		} else {
			send( new Stream( null, serviceName, "9876543210", "stream", "1.0" ) );
			send( new Features( "stream", new Bind(), new Session() ) );
		}
	}

	protected void onSASLAuthentication( SASLAuthentication auth ) throws IOException {
		if( auth instanceof SASLPlainAuthentication ) {
			username = ( (SASLPlainAuthentication) auth ).getAuthenticationString();
			jid = String.format( "%s@%s", username, serviceName );
		}
		send( new SASLSuccess() );
	}

	protected void onBind( IQ iq, Bind bind ) throws IOException {
		jidWithResource = String.format( "%s/%s", jid, bind.resource() );
		if( iq.expectsResult() ) {
			send( IQ.result( iq.id(), Bind.jid( jidWithResource ) ) );
		}
	}

	protected void onSession( IQ iq, Session session ) throws IOException {
		ch = 0;
		sh = 0;
		if( iq.expectsResult() ) {
			send( IQ.result( iq.id(), new Session() ) );
		}
	}

	protected void onIQ( IQ iq ) throws IOException {

		Object content = iq.getContent();

		if( content instanceof Bind ) {
			onBind( iq, (Bind) content );
		}

		if( content instanceof Session ) {
			onSession( iq, (Session) content );
		}

		if( content instanceof Ping ) {
			onPing( iq, (Ping) content );
		}

	}

	protected void onPing( IQ iq, Ping ping ) throws IOException {
		if( iq.expectsResult() ) {
			send( IQ.result( iq.id(), jid, jidWithResource, null ) );
		}
	}

	protected void onPresence( Presence presence ) throws IOException {
		if( presence.type() == null ) {
			send( new Presence( presence.id(), jid, jid ) );
		}
	}

	@Override
	public void onPacketSent( Object packet ) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPacketException( Throwable exception ) {
		// TODO Auto-generated method stub
	}

	public void onConnected() {
		// By default, do nothing.
	}

	public void send( Object packet ) throws IOException {
		w.write( packet );
		onPacketSent( packet );
	}

	public boolean isConnected() {
		return !r.isClosed();
	}

	protected String getLocalAddress() {
		return localAddress;
	}

	protected String getRemoteAddress() {
		return remoteAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getJID() {
		return jid;
	}

	@Override
	public void run() {
		r.run();
	}

}
