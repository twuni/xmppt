package org.twuni.xmppt.xmpp;

import java.io.IOException;

import org.twuni.Retry;

public class XMPPClientConnector extends Retry {

	private final XMPPClientConnectionManager connectionManager;
	private final String id;
	private final byte [] previousState;

	public XMPPClientConnector( XMPPClientConnectionManager connectionManager, String id, byte [] previousState ) {
		this.connectionManager = connectionManager;
		this.id = id;
		this.previousState = previousState;
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
			connectionManager.connect( id, previousState );
		} catch( IOException ignore ) {
			// Ignore.
		}
	}

}
