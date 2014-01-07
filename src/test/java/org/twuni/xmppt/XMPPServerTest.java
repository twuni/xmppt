package org.twuni.xmppt;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class XMPPServerTest {

	protected TestingSocket client;
	protected XMPPServerTestFixture server;

	@Test
	public void establishConnectionViaLoopback() throws IOException {
		server.test( client );
	}

	@Before
	public void setUp() {
		client = new XMPPClientTestFixture();
		server = new XMPPServerTestFixture();
	}

}
