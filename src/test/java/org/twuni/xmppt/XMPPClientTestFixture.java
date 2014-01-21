package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.twuni.xmppt.xmpp.PacketListener;
import org.twuni.xmppt.xmpp.XMPPStreamReaderThread;
import org.twuni.xmppt.xmpp.XMPPStreamWriter;
import org.twuni.xmppt.xmpp.core.Features;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.iq.bind.Bind;
import org.twuni.xmppt.xmpp.iq.session.Session;
import org.twuni.xmppt.xmpp.sasl.SASLMechanisms;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;
import org.twuni.xmppt.xmpp.stream.Stream;

public class XMPPClientTestFixture implements TestingSocket {

	private String serviceName = "twuni.org";
	private String username = "alice";
	private String password = "changeit";
	private String resource = "test";

	public void setPassword( String password ) {
		this.password = password;
	}

	public void setResource( String resource ) {
		this.resource = resource;
	}

	public void setServiceName( String serviceName ) {
		this.serviceName = serviceName;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public void test( InputStream in, OutputStream out ) throws IOException {

		final XMPPStreamWriter writer = new XMPPStreamWriter( out ) {

			@Override
			public void write( Object packet ) throws IOException {
				System.out.println( String.format( "SEND %s", packet ) );
				super.write( packet );
			}

		};

		new XMPPStreamReaderThread( in, new PacketListener() {

			@Override
			public void onPacketReceived( Object packet ) {

				System.out.println( String.format( "RECV %s", packet ) );

				if( packet instanceof Features ) {
					onFeatures( (Features) packet );
				}

				if( packet instanceof SASLSuccess ) {
					onSASLSuccess( (SASLSuccess) packet );
				}

				if( packet instanceof IQ ) {
					onIQ( (IQ) packet );
				}

				if( packet instanceof Presence ) {
					onPresence( (Presence) packet );
				}

			}

			private void write( Object object ) {
				try {
					writer.write( object );
				} catch( IOException ignore ) {
					// Ignore.
				}
			}

			private void onPresence( Presence presence ) {
				write( new Presence( UUID.randomUUID().toString(), Presence.Type.UNAVAILABLE ) );
				write( new Stream().close() );
			}

			private void onIQ( IQ iq ) {
				Object content = iq.getContent();
				if( content instanceof Bind ) {
					write( new Presence( UUID.randomUUID().toString() ) );
				}
			}

			private void onSASLSuccess( SASLSuccess success ) {
				write( new Stream( serviceName ) );
			}

			private void onFeatures( Features features ) {

				if( features.hasFeature( SASLMechanisms.class ) ) {
					write( new SASLPlainAuthentication( username, password ) );
				}

				if( features.hasFeature( Bind.class ) ) {
					write( IQ.set( UUID.randomUUID().toString(), Bind.resource( resource ) ) );
				}

				if( features.hasFeature( Session.class ) ) {
					write( IQ.set( UUID.randomUUID().toString(), new Session() ) );
				}

			}

		} ).start();

		writer.write( new Stream( serviceName ) );

	}

	@Override
	public void test( Socket socket ) throws IOException {
		test( socket.getInputStream(), socket.getOutputStream() );
	}

}
