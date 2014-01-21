package org.twuni.xmppt;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

public class SSLSocketTest {

	private TestingSocket client;

	@Ignore( "This is an integration test, and should only be run manually on an as-needed basis." )
	@Test
	public void establishConnectionViaSSLSocket() throws IOException, XmlPullParserException, InterruptedException {
		Socket socket = SSLSocketFactory.getDefault().createSocket( "twuni.org", 5223 );
		client.test( socket );
		socket.close();
	}

	@Before
	public void setUp() {
		client = new XMPPClientTestFixture();
	}

}
