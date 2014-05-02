package org.twuni.xmppt;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.After;
import org.junit.Before;
import org.twuni.nio.server.auth.SimpleAuthenticator;

public class XMPPClientIntegrationTest extends XMPPClientIntegrationTestBase {

	private XMPPTestServer server;
	private ResourceBundle properties;

	private Timer failsafe;

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

	public long getTimeout() {
		try {
			return Long.parseLong( properties.getString( "timeout" ) );
		} catch( Throwable exception ) {
			return 5000L;
		}
	}

	@Override
	protected String getUsername() {
		return properties.getString( "user_name" );
	}

	private boolean isLocal() {
		try {
			return InetAddress.getByName( getHost() ).isLoopbackAddress();
		} catch( UnknownHostException exception ) {
			return false;
		}
	}

	@Override
	protected boolean isSecure() {
		return Boolean.parseBoolean( properties.getString( "secure" ) );
	}

	@Before
	public void startTestServer() {
		properties = ResourceBundle.getBundle( getClass().getName() );
		if( isLocal() ) {
			SimpleAuthenticator authenticator = new SimpleAuthenticator();
			authenticator.setCredential( getUsername(), getPassword() );
			server = new XMPPTestServer( getServiceName(), authenticator, getPort(), isSecure() );
			server.startListening();
		}
		failsafe = new Timer( true );
		failsafe.schedule( new TimerTask() {

			@Override
			public void run() {
				fail();
			}

		}, getTimeout() );
	}

	@After
	public void stopTestServer() {
		failsafe.cancel();
		failsafe.purge();
		failsafe = null;
		if( isLocal() ) {
			server.stopListening();
			server = null;
		}
	}

}
