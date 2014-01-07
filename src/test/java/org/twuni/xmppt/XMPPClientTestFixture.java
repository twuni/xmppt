package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.twuni.xmppt.xml.Bind;
import org.twuni.xmppt.xml.IQ;
import org.twuni.xmppt.xml.Presence;
import org.twuni.xmppt.xml.SASLPlainAuthentication;
import org.twuni.xmppt.xml.XMPPSession;
import org.twuni.xmppt.xml.XMPPStream;

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

	public void test( Channel channel ) throws IOException {

		XMPPStream outerStream = new XMPPStream( serviceName );

		channel.write( outerStream );
		channel.read();
		channel.read();

		channel.write( new SASLPlainAuthentication( username, password ) );
		channel.read();

		XMPPStream innerStream = new XMPPStream( serviceName );

		channel.write( innerStream );
		channel.read();
		channel.read();

		channel.write( IQ.set( "1000-1", Bind.resource( resource ) ) );
		channel.read();

		channel.write( IQ.set( "1000-2", new XMPPSession() ) );
		channel.read();

		channel.write( new Presence( "1000-3" ) );
		channel.read();

		channel.write( innerStream.close() );
		channel.write( outerStream.close() );

	}

	public void test( InputStream in, OutputStream out ) throws IOException {
		test( new LoggingChannel( "C", out, "S", in ) );
	}

	@Override
	public void test( Socket socket ) throws IOException {
		test( socket.getInputStream(), socket.getOutputStream() );
	}

}
