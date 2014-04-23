package org.twuni.xmppt.server;

import java.nio.BufferOverflowException;
import java.util.List;

import org.twuni.Logger;
import org.twuni.nio.server.Connection;
import org.twuni.nio.server.EventHandler;
import org.twuni.nio.server.Transporter;
import org.twuni.nio.server.auth.AuthenticationException;
import org.twuni.nio.server.auth.Authenticator;
import org.twuni.xmppt.server.XMPPConnection.State;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLElementParser;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.ping.Ping;
import org.twuni.xmppt.xmpp.sasl.SASLAuthentication;
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

public class XMPPEventHandler extends EventHandler {

	private static final Logger LOG = new Logger( XMPPEventHandler.class.getName() );
	private static final XMLElementParser XML = new XMLElementParser();

	private final Transporter transporter = new Transporter();
	private final Authenticator authenticator;
	private final String serviceName;

	public XMPPEventHandler( String serviceName, Authenticator authenticator ) {
		this.serviceName = serviceName;
		this.authenticator = authenticator;
	}

	@Override
	public void onData( Connection connection, byte [] data ) {
		LOG.info( "RECV C/%s [%d bytes] %s", Integer.toHexString( connection.hashCode() ), Integer.valueOf( data.length ), new String( data, 0, data.length ) );
		List<XMLElement> xml = XML.parse( data );
		for( XMLElement element : xml ) {
			onXMLElement( connection, element );
		}
	}

	public void onXMLElement( Connection connection, XMLElement element ) {
		PacketTransformer xmpp = XMPPPacketConfiguration.getDefault();
		Object packet = Stream.is( element ) ? Stream.from( element ) : xmpp.transform( element );
		if( packet != null ) {
			onPacket( connection, packet );
		}
	}

	public void onPacket( Connection connection, Object packet ) {

		if( packet instanceof Stream ) {
			onStream( connection, (Stream) packet );
		}

		if( packet instanceof SASLAuthentication ) {
			onSASLAuthentication( connection, (SASLAuthentication) packet );
		}

		if( packet instanceof IQ ) {
			onIQ( connection, (IQ) packet );
		}

		if( packet instanceof Presence ) {
			onPresence( connection, (Presence) packet );
		}

		if( packet instanceof Message ) {
			onMessage( connection, (Message) packet );
		}

		if( packet instanceof AcknowledgmentRequest ) {
			onAcknowledgmentRequest( connection, (AcknowledgmentRequest) packet );
		}

		if( packet instanceof Acknowledgment ) {
			onAcknowledgment( connection, (Acknowledgment) packet );
		}

	}

	private void onAcknowledgment( Connection connection, Acknowledgment acknowledgment ) {
		if( acknowledgment.getH() != state( connection ).sent ) {
			send( connection, new org.twuni.xmppt.xmpp.core.Error( StreamManagement.NAMESPACE ) );
		}
	}

	private void send( Connection connection, Object packet ) {
		byte [] b = packet.toString().getBytes();
		try {
			connection.write( b, 0, b.length );
		} catch( BufferOverflowException exception ) {
			LOG.info( "DELAY C/%s %s", Integer.toHexString( connection.hashCode() ), new String( b, 0, b.length ) );
		}
	}

	private void onAcknowledgmentRequest( Connection connection, AcknowledgmentRequest request ) {
		send( connection, new Acknowledgment( state( connection ).received ) );
	}

	private void onMessage( Connection connection, Message message ) {
		state( connection ).received++;
		transporter.transport( message.from( jid( connection ) ) );
	}

	private void onPresence( Connection connection, Presence presence ) {
		if( presence.type() == null ) {
			String jid = jid( connection );
			send( connection, new Presence( presence.id(), jid, jid ) );
		}
	}

	private void onIQ( Connection connection, IQ iq ) {

		Object content = iq.getContent();

		if( content instanceof Bind ) {
			onBind( connection, iq, (Bind) content );
		}

		if( content instanceof Session ) {
			onSession( connection, iq, (Session) content );
		}

		if( content instanceof Ping ) {
			onPing( connection, iq, (Ping) content );
		}

	}

	private void onPing( Connection connection, IQ iq, Ping ping ) {
		if( iq.expectsResult() ) {
			send( connection, IQ.result( iq.id(), jid( connection ), jidWithResource( connection ), null ) );
		}
	}

	private void onSession( Connection connection, IQ iq, Session session ) {
		state( connection ).sent = 0;
		state( connection ).received = 0;
		if( iq.expectsResult() ) {
			send( connection, IQ.result( iq.id(), new Session() ) );
		}
	}

	private void onBind( Connection connection, IQ iq, Bind bind ) {
		if( iq.expectsResult() ) {
			String resource = bind.resource();
			if( resource != null ) {
				state( connection ).resource = resource;
				transporter.available( connection, jid( connection ) );
			}
			send( connection, IQ.result( iq.id(), Bind.jid( jidWithResource( connection ) ) ) );
		}
	}

	private void onSASLAuthentication( Connection connection, SASLAuthentication auth ) {
		if( auth instanceof SASLPlainAuthentication ) {
			SASLPlainAuthentication plain = (SASLPlainAuthentication) auth;
			try {
				authenticator.checkCredential( plain.getAuthenticationString(), plain.getPassword() );
				state( connection ).username = plain.getAuthenticationString();
				send( connection, new SASLSuccess() );
			} catch( AuthenticationException exception ) {
				send( connection, new SASLFailure( "<not-authorized/>" ) );
			}
		}
	}

	private boolean isAuthenticated( Connection connection ) {
		return state( connection ).username != null;
	}

	private void onStream( Connection connection, Stream stream ) {
		if( !serviceName.equals( stream.to() ) ) {
			send( connection, new StreamError() );
			return;
		}
		if( !isAuthenticated( connection ) ) {
			send( connection, new Stream( null, serviceName, Integer.toHexString( connection.hashCode() ), "stream", "1.0" ) );
			send( connection, new Features( "stream", new SASLMechanisms( "PLAIN" ), new CapabilitiesHash( "http://example.com/test", CapabilitiesHash.HASH_SHA1, "InwBitZINWvBDup88dDxf1C9HlY" ) ) );
		} else {
			send( connection, new Stream( null, serviceName, Integer.toHexString( connection.hashCode() - 1 ), "stream", "1.0" ) );
			send( connection, new Features( "stream", new Bind(), new Session(), new CapabilitiesHash( "http://example.com/test", CapabilitiesHash.HASH_SHA1, "InwBitZINWvBDup88dDxf1C9HlY" ) ) );
		}
	}

	@Override
	public void onDisconnected( Connection connection ) {
		super.onDisconnected( connection );
		transporter.unavailable( jid( connection ) );
	}

	@Override
	public void onWriteRequested( Connection connection ) {
		super.onWriteRequested( connection );
		String jid = jid( connection );
		if( jid != null ) {
			transporter.flush( jid );
		}
	}

	private String jidWithResource( Connection connection ) {
		return state( connection ).jidWithResource( serviceName );
	}

	private String jid( Connection connection ) {
		return state( connection ).jid( serviceName );
	}

	private static State state( Connection connection ) {
		return (State) connection.state();
	}

}
