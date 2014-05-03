package org.twuni.xmppt.xmpp;

import java.io.IOException;

import org.twuni.Retry;

public class XMPPClientConnector extends Retry {

	private final XMPPClientConnectionManager connectionManager;
	private final String id;

	public XMPPClientConnector( XMPPClientConnectionManager connectionManager, String id ) {
		this.connectionManager = connectionManager;
		this.id = id;
	}

	@Override
	protected boolean isFinished() {
		return !connectionManager.isManaging( id ) || connectionManager.isConnected( id );
	}

	public void start() {
		thread().start();
	}

	private Thread thread() {
		return new Thread( this, String.format( "%s(%s)", getClass().getName(), id ) );
	}

	@Override
	protected void tryAgain() {
		try {
			connectionManager.connect( id );
		} catch( IOException ignore ) {
			// Ignore.
		}
	}

}
