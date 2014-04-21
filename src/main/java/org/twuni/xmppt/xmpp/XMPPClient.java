package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.ping.Ping;
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
	private InputStream input;
	private OutputStream output;
	private PacketTransformer packetTransformer;
	private final List<PacketListener> packetListeners = new ArrayList<PacketListener>();
	private Thread reader;
	private int openStreams = 0;

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
		this.input = in;
		this.output = out;
		this.packetTransformer = packetTransformer;
		this.writer = new XMPPStreamWriter( out );
		this.serviceName = serviceName;
		this.username = username;
		this.password = password;
		this.resource = resource;
		newStream();
		state.connected = true;
	}

	public void addPacketListener( PacketListener packetListener ) {
		packetListeners.add( packetListener );
	}

	public void removePacketListener( PacketListener packetListener ) {
		packetListeners.remove( packetListener );
	}

	public void clearPacketListeners() {
		packetListeners.clear();
	}

	private void newStream() throws IOException {
		Thread oldReader = reader;
		reader = new Thread( new XMPPStreamReader( input, packetTransformer, this ), "XMPP Reader" );
		reader.start();
		send( new Stream( serviceName ) );
		openStreams++;
		if( oldReader != null ) {
			oldReader.interrupt();
			throw new XMLStreamResetException();
		}
	}

	@Override
	public void onPacketReceived( Object packet ) {
		try {
			respondTo( packet );
		} catch( IOException exception ) {
			onPacketException( exception );
		}
		for( PacketListener packetListener : packetListeners ) {
			packetListener.onPacketReceived( packet );
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
		} else if( packet instanceof Stream ) {
			onStream( (Stream) packet );
		} else if( packet instanceof Message ) {
			onMessage( (Message) packet );
		} else {
			throw new IOException( String.format( "Unknown packet received: [%1$s] %2$s", packet.getClass().getName(), packet ) );
		}

	}

	protected void onException( Throwable exception ) {
		// By default, do nothing.
	}

	public void quit() throws IOException {
		send( new Presence( id(), Presence.Type.UNAVAILABLE ) );
		while( openStreams > 0 ) {
			send( new Stream().close() );
			openStreams--;
		}
		state.reset();
		if( reader != null ) {
			reader.interrupt();
			reader = null;
		}
	}

	protected void onPresence( Presence presence ) {
		// Blah.
	}

	protected void onStream( Stream stream ) {
		// Blah.
	}

	protected void onMessage( Message message ) {
		// Blah.
	}

	protected void onIQ( IQ iq ) throws IOException {
		Object content = iq.getContent();
		if( content instanceof Bind ) {
			state.bound = true;
			state.jid = ( (Bind) content ).jid();
			send( new Presence( id() ) );
		}
		if( content instanceof Ping ) {
			if( iq.expectsResult() ) {
				send( IQ.result( iq.id(), state.jid, serviceName, null ) );
			}
		}
	}

	protected void onSASLSuccess( SASLSuccess success ) throws IOException {
		state.authenticated = true;
		newStream();
	}

	public void send( Object packet ) throws IOException {
		writer.write( packet );
		for( PacketListener packetListener : packetListeners ) {
			packetListener.onPacketSent( packet );
		}
	}

	protected void onFeatures( Features features ) throws IOException {

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

	protected static String id() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void onPacketException( Throwable exception ) {
		for( PacketListener packetListener : packetListeners ) {
			packetListener.onPacketException( exception );
		}
		onException( exception );
	}

	@Override
	public void onPacketSent( Object packet ) {
		// By default, do nothing.
	}

}
