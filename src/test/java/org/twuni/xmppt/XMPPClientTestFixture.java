package org.twuni.xmppt;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;

import org.junit.Assert;
import org.twuni.xmppt.xmpp.PacketListener;
import org.twuni.xmppt.xmpp.XMPPClient;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.sasl.SASLSuccess;

public class XMPPClientTestFixture extends Assert implements PacketListener {

	private String serviceName = "twuni.org";
	private String username = "alice";
	private String password = "changeit";
	private String resource = "test";
	private XMPPClient xmpp;
	private Socket socket;

	public void connect( String host, int port ) throws IOException {
		try {
			connect( host, port, false );
		} catch( KeyManagementException impossible ) {
			// Impossible.
		} catch( NoSuchAlgorithmException impossible ) {
			// Impossible.
		}
	}

	public void connect( String host, int port, boolean secure ) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		this.socket = secure ? createSecureSocket( host, port ) : new Socket( host, port );
		onConnected();
	}

	public void service( String serviceName ) {
		this.serviceName = serviceName;
	}

	public void login( String username, String password ) {
		this.username = username;
		this.password = password;
	}

	public void bind( String resource ) {
		this.resource = resource;
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

	public void start() throws IOException, KeyManagementException, NoSuchAlgorithmException {
		xmpp = new XMPPClient( socket, serviceName, username, password, resource );
		xmpp.addPacketListener( this );
		xmpp.waitForConnectionToDie();
		xmpp.waitForConnectionToDie();
	}

	public void stop() {
		try {
			xmpp.quit();
		} catch( IOException exception ) {
			onPacketException( exception );
		}
		onDisconnected();
	}

	protected void send( Object packet ) {
		try {
			xmpp.send( packet );
		} catch( IOException exception ) {
			onPacketException( exception );
		}
	}

	@Override
	public void onPacketReceived( Object packet ) {

		if( packet instanceof Presence ) {
			onAvailable();
		}

		if( packet instanceof SASLSuccess ) {
			onAuthenticated();
		}

	}

	@Override
	public void onPacketSent( Object packet ) {

		if( packet instanceof Presence ) {
			Presence presence = (Presence) packet;
			if( Presence.Type.UNAVAILABLE.equals( presence.type() ) ) {
				onUnavailable();
			}
		}

	}

	@Override
	public void onPacketException( Throwable exception ) {
		exception.printStackTrace();
		fail( exception.getLocalizedMessage() );
	}

	protected void onConnected() {
		// Blah.
	}

	protected void onAuthenticated() {
		// Blah.
	}

	protected void onAvailable() {
		// Blah.
	}

	protected void onUnavailable() {
		// Blah.
	}

	protected void onDisconnected() {
		// Blah.
	}

}
