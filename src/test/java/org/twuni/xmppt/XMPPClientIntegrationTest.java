package org.twuni.xmppt;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.twuni.nio.server.auth.SimpleAuthenticator;

public class XMPPClientIntegrationTest extends XMPPClientIntegrationTestBase {

	private XMPPTestServer server;
	private ResourceBundle properties;

	@Override
	protected String getHost() {
		return properties.getString( "host" );
	}

	@Override
	protected String getPassword() {
		return properties.getString( "password" );
	}

	@Override
	protected int getPort() {
		return Integer.parseInt( properties.getString( "port" ) );
	}

	@Override
	protected String getResourceName() {
		return properties.getString( "resource_name" );
	}

	@Override
	protected String getServiceName() {
		return properties.getString( "service_name" );
	}

	@Override
	protected String getUsername() {
		return properties.getString( "user_name" );
	}

	@Override
	protected boolean isSecure() {
		return Boolean.parseBoolean( properties.getString( "secure" ) );
	}

	@Before
	public void startTestServer() {
		properties = ResourceBundle.getBundle( getClass().getName() );
		SimpleAuthenticator authenticator = new SimpleAuthenticator();
		authenticator.setCredential( getUsername(), getPassword() );
		server = new XMPPTestServer( getServiceName(), authenticator, getPort(), isSecure() );
		server.startListening();
	}

	@After
	public void stopTestServer() {
		server.stopListening();
		server = null;
	}

}
