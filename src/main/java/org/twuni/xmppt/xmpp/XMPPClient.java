package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.sasl.SASLMechanisms;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;
import org.twuni.xmppt.xmpp.session.Session;
import org.twuni.xmppt.xmpp.stream.Stream;

public class XMPPClient implements PacketListener {

	static class State {

		public boolean connected;
		public boolean authenticated;
		public boolean bound;
		public String jid;

		public State() {
			// Blah.
		}

		public void reset() {
			jid = null;
			connected = false;
			authenticated = false;
			bound = false;
		}

	}

	private final XMPPStreamWriter writer;
	private final String serviceName;
	private final String username;
	private final String password;
	private final String resource;
	private final State state = new State();

	public XMPPClient( Socket socket, String serviceName, String username, String password, String resource ) throws IOException {
		this( socket.getInputStream(), socket.getOutputStream(), XMPPPacketConfiguration.getDefault(), serviceName, username, password, resource );
	}

	public XMPPClient( Socket socket, PacketTransformer packetTransformer, String serviceName, String username, String password, String resource ) throws IOException {
		this( socket.getInputStream(), socket.getOutputStream(), packetTransformer, serviceName, username, password, resource );
	}

	public XMPPClient( InputStream in, OutputStream out, String serviceName, String username, String password, String resource ) throws IOException {
		this( in, out, XMPPPacketConfiguration.getDefault(), serviceName, username, password, resource );
	}

	public XMPPClient( InputStream in, OutputStream out, PacketTransformer packetTransformer, String serviceName, String username, String password, String resource ) throws IOException {
		this.writer = new XMPPStreamWriter( out );
		this.serviceName = serviceName;
		this.username = username;
		this.password = password;
		this.resource = resource;
		new XMPPStreamReaderThread( in, packetTransformer, this ).start();
		send( new Stream( serviceName ) );
		state.connected = true;
	}

	@Override
	public void onPacketReceived( Object packet ) {
		try {
			respondTo( packet );
		} catch( Throwable exception ) {
			onException( exception );
		}
	}

	private void respondTo( Object packet ) throws IOException {

		if( packet instanceof Features ) {
			onFeatures( (Features) packet );
		} else if( packet instanceof SASLSuccess ) {
			onSASLSuccess( (SASLSuccess) packet );
		} else if( packet instanceof IQ ) {
			onIQ( (IQ) packet );
		} else if( packet instanceof Presence ) {
			onPresence( (Presence) packet );
		} else {
			throw new IOException( String.format( "Unknown packet received: [%1$s] %2$s", packet.getClass().getName(), packet ) );
		}

	}

	protected void onException( Throwable exception ) {
		// By default, do nothing.
	}

	public void quit() throws IOException {
		send( new Presence( id(), Presence.Type.UNAVAILABLE ) );
		send( new Stream().close() );
		state.reset();
	}

	private void onPresence( Presence presence ) {
		// Blah.
	}

	private void onIQ( IQ iq ) throws IOException {
		Object content = iq.getContent();
		if( content instanceof Bind ) {
			state.bound = true;
			state.jid = ( (Bind) content ).jid();
			send( new Presence( id() ) );
		}
	}

	private void onSASLSuccess( SASLSuccess success ) throws IOException {
		state.authenticated = true;
		send( new Stream( serviceName ) );
	}

	public void send( Object packet ) throws IOException {
		writer.write( packet );
	}

	private void onFeatures( Features features ) throws IOException {

		if( features.hasFeature( SASLMechanisms.class ) ) {
			SASLMechanisms mechanisms = (SASLMechanisms) features.getFeature( SASLMechanisms.class );
			if( mechanisms.hasMechanism( SASLPlainAuthentication.MECHANISM ) ) {
				send( new SASLPlainAuthentication( username, password ) );
			}
		}

		if( features.hasFeature( Bind.class ) ) {
			send( IQ.set( id(), Bind.resource( resource ) ) );
		}

		if( features.hasFeature( Session.class ) ) {
			send( IQ.set( id(), new Session() ) );
		}

	}

	private static String id() {
		return UUID.randomUUID().toString();
	}

}
