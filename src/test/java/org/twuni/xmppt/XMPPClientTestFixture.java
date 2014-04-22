package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.twuni.xmppt.xmpp.XMPPClient;

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

		XMPPClient xmpp = new XMPPClient( in, out, serviceName, username, password, resource ) {

			@Override
			public void send( Object packet ) throws IOException {
				System.out.println( String.format( "SEND %s", packet ) );
				super.send( packet );
			}

			@Override
			public void onPacketReceived( Object packet ) {
				System.out.println( String.format( "RECV %s", packet ) );
				super.onPacketReceived( packet );
			}

			@Override
			protected void onException( Throwable exception ) {
				System.out.println( String.format( "ERROR [%s] %s", exception.getClass().getName(), exception.getLocalizedMessage() ) );
				super.onException( exception );
				try {
					quit();
				} catch( IOException ignore ) {
					// Ignore.
				}
			}

		};

		try {
			Thread.sleep( 10000 );
		} catch( InterruptedException exception ) {
			// Ignore.
		}

		xmpp.quit();

	}

	@Override
	public void test( Socket socket ) throws IOException {
		test( socket.getInputStream(), socket.getOutputStream() );
	}

}
