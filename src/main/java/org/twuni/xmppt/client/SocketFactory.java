package org.twuni.xmppt.client;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;

public class SocketFactory {

	private static SocketFactory instance;

	public static SocketFactory getInstance() {
		if( instance == null ) {
			instance = new SocketFactory();
		}
		return instance;
	}

	public Socket createSecureSocket( String host, int port ) throws IOException {

		try {

			SSLContext tls = SSLContext.getInstance( "TLSv1.2" );

			try {
				tls.getSocketFactory();
			} catch( IllegalStateException exception ) {
				tls.init( null, null, new SecureRandom() );
			}

			return tls.getSocketFactory().createSocket( host, port );

		} catch( Throwable exception ) {
			throw new IOException( exception );
		}

	}

	public Socket createSocket( String host, int port ) throws IOException {
		return new Socket( host, port );
	}

	public Socket createSocket( String host, int port, boolean secure ) throws IOException {
		return secure ? createSecureSocket( host, port ) : createSocket( host, port );
	}

}
