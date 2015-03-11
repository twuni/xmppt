package org.twuni.xmppt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.After;
import org.junit.Before;
import org.twuni.nio.server.auth.SimpleAuthenticator;
import org.twuni.xmppt.server.XMPPAcceptor;

public class XMPPClientIntegrationTest extends XMPPClientIntegrationTestBase {

	private Thread asyncTestServer;
	private XMPPTestServer syncTestServer;
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

	protected boolean isAsync() {
		return Boolean.parseBoolean( properties.getString( "async" ) );
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

	private void startLocalAsyncTestServer( SimpleAuthenticator authenticator ) {
		try {
			asyncTestServer = new Thread( new XMPPAcceptor( getPort(), getServiceName(), authenticator ) );
		} catch( IOException ignore ) {
			// Ignore this.
		}
		asyncTestServer.start();
	}

	private void startLocalSyncTestServer( SimpleAuthenticator authenticator ) {
		syncTestServer = new XMPPTestServer( getServiceName(), authenticator, getPort(), isSecure() );
		syncTestServer.startListening();
	}

	@Before
	public void startTestServer() {
		properties = ResourceBundle.getBundle( getClass().getName() );
		if( isLocal() ) {
			SimpleAuthenticator authenticator = new SimpleAuthenticator();
			authenticator.put( getUsername(), getPassword() );
			if( isAsync() ) {
				startLocalAsyncTestServer( authenticator );
			} else {
				startLocalSyncTestServer( authenticator );
			}
		}
		failsafe = new Timer( true );
		failsafe.schedule( new TimerTask() {

			@Override
			public void run() {
				try {
					disconnect();
				} catch( IOException exception ) {
					// Ignore.
				}
			}

		}, getTimeout() );
	}

	private void stopLocalAsyncTestServer() {
		asyncTestServer.interrupt();
		try {
			asyncTestServer.join();
		} catch( InterruptedException ignore ) {
			// Ignore.
		}
		asyncTestServer = null;
	}

	private void stopLocalSyncTestServer() {
		syncTestServer.stopListening();
		syncTestServer = null;
	}

	@After
	public void stopTestServer() {
		failsafe.cancel();
		failsafe.purge();
		failsafe = null;
		if( isLocal() ) {
			if( isAsync() ) {
				stopLocalAsyncTestServer();
			} else {
				stopLocalSyncTestServer();
			}
		}
	}

}
