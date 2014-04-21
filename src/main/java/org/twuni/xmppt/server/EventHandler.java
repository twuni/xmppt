package org.twuni.xmppt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class EventHandler {

	private static final Logger LOG = new Logger( EventHandler.class.getName() );

	public void onReadRequested( Connection connection ) {

		ByteBuffer buffer = connection.getInputBuffer();
		SocketChannel client = connection.getClient();

		try {
			buffer.clear();
			int bytesRead = client.read( buffer );
			if( bytesRead <= 0 ) {
				connection.getClient().close();
				onDisconnected( connection );
				return;
			}
			buffer.flip();
			byte [] data = new byte [buffer.limit()];
			buffer.get( data );
			onData( connection, data );
		} catch( IOException exception ) {
			onException( exception );
		}

	}

	protected void onData( Connection connection, byte [] data ) {
		// By default, do nothing.
	}

	public void onWriteRequested( Connection connection ) {
		try {
			connection.flush();
		} catch( IOException exception ) {
			onException( exception );
		}
	}

	public void onConnected( Connection connection ) {
		LOG.info( "CONNECT C/%s", Integer.toHexString( connection.hashCode() ) );
	}

	public void onDisconnected( Connection connection ) {
		LOG.info( "DISCONNECT C/%s", Integer.toHexString( connection.hashCode() ) );
		connection.cleanup();
	}

	public void onException( Throwable exception ) {
		LOG.info( "ERROR [%s] %s", "onException", exception.getClass().getSimpleName(), exception.getLocalizedMessage() );
	}

}
