package org.twuni.xmppt;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.twuni.xmppt.xmpp.PacketListener;
import org.twuni.xmppt.xmpp.XMPPClient;
import org.twuni.xmppt.xmpp.core.Message;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.stream.Acknowledgment;
import org.twuni.xmppt.xmpp.stream.AcknowledgmentRequest;

public class XMPPClientTest extends Assert {

	private String username;
	private String password;
	private String serviceName;
	private String host;
	private int port;

	@Ignore
	@Test
	public void sanityCheck() throws IOException, NoSuchAlgorithmException, KeyManagementException {

		Socket socket = createSecureSocket( host, port );

		final XMPPClient xmpp = new XMPPClient( socket, serviceName, username, password, "xep0198-integration-test" );

		xmpp.addPacketListener( new PacketListener() {

			@Override
			public void onPacketReceived( Object packet ) {

				System.out.println( String.format( "RECV [%s] %s", packet.getClass().getName(), packet ) );

				if( packet instanceof Presence ) {
					send( new AcknowledgmentRequest() );
				}

				if( packet instanceof Acknowledgment ) {

					Acknowledgment acknowledgment = (Acknowledgment) packet;

					switch( acknowledgment.getH() ) {

						case 0:

							send( new Message( "abcd12-1", "chat", null, "bob@example.com", "<body>Test.</body>" ) );
							send( new AcknowledgmentRequest() );

							break;

						default:

							// Good.
							break;

					}

				}

			}

			private void send( Object packet ) {
				try {
					xmpp.send( packet );
				} catch( IOException exception ) {
					onPacketException( exception );
				}
			}

			@Override
			public void onPacketSent( Object packet ) {
				System.out.println( String.format( "SENT %s", packet ) );
			}

			@Override
			public void onPacketException( Throwable exception ) {
				System.out.println( String.format( "ERROR [%s] [%s]", exception.getClass().getName(), exception.getLocalizedMessage() ) );
			}

		} );

		try {
			Thread.sleep( 5000 );
		} catch( InterruptedException exception ) {
			// Ignore.
		}

		xmpp.quit();

	}

	public static Socket createSecureSocket( String host, int port ) throws NoSuchAlgorithmException, KeyManagementException, IOException, UnknownHostException {

		SSLContext tls = SSLContext.getInstance( "TLSv1.2" );

		try {
			tls.getSocketFactory();
		} catch( IllegalStateException exception ) {
			tls.init( null, null, new SecureRandom() );
		}

		return tls.getSocketFactory().createSocket( host, port );

	}

	@Before
	public void setUp() {
		username = "alice";
		password = "abcd...1234";
		serviceName = "example.com";
		host = "localhost";
		port = 5223;
	}
}
