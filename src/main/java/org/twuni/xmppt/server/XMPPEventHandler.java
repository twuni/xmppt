package org.twuni.xmppt.server;

import java.nio.BufferOverflowException;

import org.twuni.Logger;
import org.twuni.nio.server.Connection;
import org.twuni.nio.server.Transporter;
import org.twuni.nio.server.auth.AuthenticationException;
import org.twuni.nio.server.auth.Authenticator;
import org.twuni.xmppt.server.XMPPConnection.State;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.bind.Bind;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesHash;
import org.twuni.xmppt.xmpp.core.Failure;
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
import org.twuni.xmppt.xmpp.stream.Enable;
import org.twuni.xmppt.xmpp.stream.Enabled;
import org.twuni.xmppt.xmpp.stream.Stream;
import org.twuni.xmppt.xmpp.stream.StreamError;
import org.twuni.xmppt.xmpp.stream.StreamManagement;

public class XMPPEventHandler extends XMLEventHandler {

	private static Logger defaultLogger() {
		return new Logger( XMPPEventHandler.class.getName() );
	}

	private static PacketTransformer defaultTransformer() {
		return XMPPPacketConfiguration.getDefault();
	}

	private static Transporter defaultTransporter() {
		return new Transporter();
	}

	private static boolean isAuthenticated( Connection connection ) {
		return state( connection ).username != null;
	}

	private static State state( Connection connection ) {
		return (State) connection.state();
	}

	private final Logger log;
	private final Transporter transporter;
	private final Authenticator authenticator;
	private final String serviceName;
	private final PacketTransformer transformer;

	public XMPPEventHandler( String serviceName, Authenticator authenticator ) {
		this( serviceName, authenticator, defaultTransporter(), defaultTransformer(), defaultLogger() );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, Logger logger ) {
		this( serviceName, authenticator, defaultTransporter(), defaultTransformer(), logger );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, PacketTransformer transformer ) {
		this( serviceName, authenticator, defaultTransporter(), transformer, defaultLogger() );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, PacketTransformer transformer, Logger logger ) {
		this( serviceName, authenticator, defaultTransporter(), transformer, logger );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, Transporter transporter ) {
		this( serviceName, authenticator, transporter, defaultTransformer(), defaultLogger() );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, Transporter transporter, Logger logger ) {
		this( serviceName, authenticator, transporter, defaultTransformer(), logger );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, Transporter transporter, PacketTransformer transformer ) {
		this( serviceName, authenticator, transporter, transformer, defaultLogger() );
	}

	public XMPPEventHandler( String serviceName, Authenticator authenticator, Transporter transporter, PacketTransformer transformer, Logger logger ) {
		super( logger );
		this.serviceName = serviceName;
		this.authenticator = authenticator;
		this.transporter = transporter;
		this.transformer = transformer;
		log = logger;
	}

	private String jid( Connection connection ) {
		return state( connection ).jid( serviceName );
	}

	private String jidWithResource( Connection connection ) {
		return state( connection ).jidWithResource( serviceName );
	}

	public void onAcknowledgment( Connection connection, Acknowledgment acknowledgment ) {
		if( acknowledgment.getH() != state( connection ).sent ) {
			send( connection, new org.twuni.xmppt.xmpp.core.Error( StreamManagement.NAMESPACE ) );
		}
	}

	public void onAcknowledgmentRequest( Connection connection ) {
		send( connection, new Acknowledgment( state( connection ).received ) );
	}

	private void onBind( Connection connection, IQ iq, Bind bind ) {
		if( iq.expectsResult() ) {
			String resource = bind.resource();
			if( resource != null ) {
				state( connection ).resource = resource;
				transporter.available( connection, jid( connection ) );
				state( connection ).available = true;
			}
			send( connection, IQ.result( iq.id(), Bind.jid( jidWithResource( connection ) ) ) );
		}
	}

	@Override
	public void onDisconnected( Connection connection ) {
		super.onDisconnected( connection );
		transporter.unavailable( jid( connection ) );
		State s = state( connection );
		s.available = false;
	}

	public void onEnable( Connection connection, Enable enable ) {
		State s = state( connection );
		if( !s.isBound() ) {
			send( connection, new Failure( StreamManagement.NAMESPACE ) );
			return;
		}
		s.sent = 0;
		s.received = 0;
		s.streamManagementID = connection.id();
		s.streamManagementEnabled = true;
		send( connection, new Enabled( connection.id(), serviceName, Math.min( 60, enable.getMaximumResumptionTime() ), enable.supportsSessionResumption() ) );
	}

	public void onIQ( Connection connection, IQ iq ) {

		Object [] contents = iq.getContent();

		for( Object content : contents ) {
			if( content instanceof Bind ) {
				onBind( connection, iq, (Bind) content );
			} else if( content instanceof Session ) {
				onSession( connection, iq, (Session) content );
			} else if( content instanceof Ping ) {
				onPing( connection, iq );
			}
		}

	}

	public void onMessage( Connection connection, Message message ) {
		transporter.transport( message.from( jid( connection ) ), message.to() );
	}

	public void onPacket( Connection connection, Object packet ) {
		State s = state( connection );
		if( s.isStreamManagementEnabled() ) {
			if( !StreamManagement.is( packet ) ) {
				s.received++;
			}
		}
		if( packet instanceof Stream ) {
			onStream( connection, (Stream) packet );
		} else if( packet instanceof SASLAuthentication ) {
			onSASLAuthentication( connection, (SASLAuthentication) packet );
		} else if( packet instanceof IQ ) {
			onIQ( connection, (IQ) packet );
		} else if( packet instanceof Presence ) {
			onPresence( connection, (Presence) packet );
		} else if( packet instanceof Message ) {
			onMessage( connection, (Message) packet );
		} else if( packet instanceof AcknowledgmentRequest ) {
			onAcknowledgmentRequest( connection );
		} else if( packet instanceof Acknowledgment ) {
			onAcknowledgment( connection, (Acknowledgment) packet );
		} else if( packet instanceof Enable ) {
			onEnable( connection, (Enable) packet );
		}
	}

	public void onPing( Connection connection, IQ iq ) {
		if( iq.expectsResult() ) {
			send( connection, IQ.result( iq.id(), jid( connection ), jidWithResource( connection ), (Object []) null ) );
		}
	}

	public void onPresence( Connection connection, Presence presence ) {
		if( presence.type() == null ) {
			String jid = jid( connection );
			send( connection, new Presence( presence.id(), jid, jid ) );
		}
	}

	public void onSASLAuthentication( Connection connection, SASLAuthentication auth ) {
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

	public void onSession( Connection connection, IQ iq, Session session ) {
		if( iq.expectsResult() ) {
			send( connection, IQ.result( iq.id(), session ) );
			state( connection ).sessionID = iq.id();
		}
	}

	public void onStream( Connection connection, Stream stream ) {
		if( !serviceName.equals( stream.to() ) ) {
			Stream s = new Stream( null, serviceName, null, "stream", "1.0" );
			send( connection, s );
			send( connection, new StreamError( new XMLElement( "host-unknown" ) ) );
			send( connection, s.close() );
			return;
		}
		State s = state( connection );
		s.serviceName = serviceName;
		if( !isAuthenticated( connection ) ) {
			s.streamID = connection.id();
			send( connection, new Stream( null, serviceName, s.streamID, "stream", "1.0" ) );
			send( connection, new Features( "stream", new SASLMechanisms( "PLAIN" ), new CapabilitiesHash( "http://example.com/test", CapabilitiesHash.HASH_SHA1, "InwBitZINWvBDup88dDxf1C9HlY" ) ) );
		} else {
			s.streamID = Integer.toHexString( connection.hashCode() - 1 );
			send( connection, new Stream( null, serviceName, s.streamID, "stream", "1.0" ) );
			send( connection, new Features( "stream", new StreamManagement(), new Bind(), new Session(), new CapabilitiesHash( "http://example.com/test", CapabilitiesHash.HASH_SHA1, "InwBitZINWvBDup88dDxf1C9HlY" ) ) );
		}
	}

	@Override
	public void onWriteRequested( Connection connection ) {
		super.onWriteRequested( connection );
		String jid = jid( connection );
		if( jid != null ) {
			transporter.flush( jid );
		}
	}

	@Override
	public void onXMLElement( Connection connection, XMLElement element ) {
		Object packet = transformer.transform( element );
		if( packet != null ) {
			onPacket( connection, packet );
		}
	}

	private void send( Connection connection, Object packet ) {
		byte [] b = packet.toString().getBytes();
		try {
			connection.write( b, 0, b.length );
		} catch( BufferOverflowException exception ) {
			log.info( "DELAY C/%s %s", connection.id(), new String( b, 0, b.length ) );
		}
	}

}
