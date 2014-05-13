package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.twuni.xmppt.xmpp.XMPPClientConnection.ConnectionListener;

public class XMPPClientConnectionManager implements ConnectionListener {

	private final Map<String, XMPPClientConnection.Builder> managedConnectionBuilders = new HashMap<String, XMPPClientConnection.Builder>();
	private final Map<XMPPClientConnection, String> connectionsToIDs = new HashMap<XMPPClientConnection, String>();
	private final Map<String, XMPPClientConnection> idsToConnections = new HashMap<String, XMPPClientConnection>();

	public void connect( String id ) throws IOException {
		connect( id, null );
	}

	public void connect( String id, byte [] previousState ) throws IOException {

		if( isConnected( id ) ) {
			return;
		}

		XMPPClientConnection.Builder builder = managedConnectionBuilders.get( id );
		if( builder != null ) {
			builder.state( previousState );
			builder.connectionListener( this );
			XMPPClientConnection connection = builder.build();
			if( connection != null ) {
				connectionsToIDs.put( connection, id );
				idsToConnections.put( id, connection );
			}
		}

	}

	public void connectAll() throws IOException {
		for( String id : managedConnectionBuilders.keySet() ) {
			connect( id );
		}
	}

	private void disconnect( String id ) {

		XMPPClientConnection connection = idsToConnections.get( id );

		if( connection != null && connection.isConnected() ) {
			try {
				connection.disconnect();
			} catch( IOException exception ) {
				// Ignore.
			}
			idsToConnections.remove( id );
			// Let the #onDisconnected method handle removal of the connection-to-id mapping.
		}

	}

	public XMPPClientConnection getConnection( String id ) {
		return idsToConnections.get( id );
	}

	public boolean isConnected( String id ) {
		XMPPClientConnection connection = idsToConnections.get( id );
		return connection != null && connection.isConnected();
	}

	public boolean isManaging( String id ) {
		return managedConnectionBuilders.get( id ) != null;
	}

	@Override
	public void onConnected( XMPPClientConnection connection ) {
		// TODO: Record this event for computing uptime metrics.
	}

	@Override
	public void onDisconnected( XMPPClientConnection connection ) {
		// TODO: Record this event for computing uptime metrics.
		String id = connectionsToIDs.get( connection );
		connectionsToIDs.remove( connection );
		idsToConnections.remove( id );
		if( isManaging( id ) ) {
			try {
				reconnect( id, connection.saveState() );
			} catch( IOException exception ) {
				throw new RuntimeException( exception );
			}
		}
	}

	private void reconnect( String id, byte [] previousState ) {
		if( isManaging( id ) && !isConnected( id ) ) {
			new XMPPClientConnector( this, id, previousState ).start();
		}
	}

	public void startManaging( String id, XMPPClientConnection.Builder builder ) {
		disconnect( id );
		managedConnectionBuilders.put( id, builder );
	}

	public void stopManaging( String id ) {
		managedConnectionBuilders.remove( id );
		disconnect( id );
	}

	public void stopManagingAll() {
		while( !managedConnectionBuilders.isEmpty() ) {
			stopManaging( managedConnectionBuilders.keySet().iterator().next() );
		}
	}

}
