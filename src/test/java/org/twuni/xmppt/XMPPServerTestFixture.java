package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.twuni.xmppt.TestServer.Worker;
import org.twuni.xmppt.xml.Bind;
import org.twuni.xmppt.xml.CapabilitiesHash;
import org.twuni.xmppt.xml.Features;
import org.twuni.xmppt.xml.IQ;
import org.twuni.xmppt.xml.Presence;
import org.twuni.xmppt.xml.SASLAuthentication;
import org.twuni.xmppt.xml.SASLMechanisms;
import org.twuni.xmppt.xml.SASLPlainAuthentication;
import org.twuni.xmppt.xml.Success;
import org.twuni.xmppt.xml.XMPPSession;
import org.twuni.xmppt.xml.XMPPStream;

public class XMPPServerTestFixture implements TestingSocket {

	private String username = "alice";
	private String serviceName = "twuni.org";
	private String resource = "test";

	public String getJID() {
		return String.format( "%s@%s/%s", username, serviceName, resource );
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

	public void test( Channel channel ) throws IOException {

		String jid = getJID();
		CapabilitiesHash capabilities = new CapabilitiesHash( String.format( "https://%s/%s", serviceName, resource ), CapabilitiesHash.HASH_SHA1, "InwBitZINWvBDup88dDxf1C9HlY" );
		XMPPStream outerStream = new XMPPStream( null, serviceName, "outer-stream" );

		channel.read();
		channel.write( outerStream );
		channel.write( new Features( new SASLMechanisms( SASLPlainAuthentication.MECHANISM ), capabilities ) );

		channel.read();
		channel.write( new Success( SASLAuthentication.NAMESPACE ) );

		XMPPStream innerStream = new XMPPStream( null, serviceName, "inner-stream" );

		channel.read();
		channel.write( innerStream );
		channel.write( new Features( new Bind(), new XMPPSession(), capabilities ) );

		channel.read();
		channel.write( IQ.result( "1000-1", Bind.jid( jid ) ) );

		channel.read();
		channel.write( IQ.result( "1000-2", new XMPPSession() ) );

		channel.read();
		channel.write( new Presence( "1000-3", jid, jid ) );

		channel.read();
		channel.write( innerStream.close() );

		channel.read();
		channel.write( outerStream.close() );

	}

	public void test( InputStream in, OutputStream out ) throws IOException {
		test( new Channel( in, out ) );
	}

	@Override
	public void test( Socket socket ) throws IOException {
		test( socket.getInputStream(), socket.getOutputStream() );
	}

	public void test( TestingSocket client ) throws IOException {

		ServerSocket server = new ServerSocket( 0 );

		TestServer xmpp = new TestServer( server, new Worker() {

			@Override
			protected void onAttach( Socket socket ) throws IOException {
				test( socket );
			}

		} );

		xmpp.start();
		client.test( new Socket( server.getInetAddress(), server.getLocalPort() ) );
		xmpp.interrupt();
		server.close();

	}

}
