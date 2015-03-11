package org.twuni.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.twuni.Logger;
import org.twuni.xmppt.util.Base64;

public class EventHandler {

	private static Logger defaultLogger() {
		return new Logger( EventHandler.class.getName() );
	}

	private final Logger log;

	public EventHandler() {
		this( defaultLogger() );
	}

	public EventHandler( Logger logger ) {
		log = logger;
	}

	public void onConnected( Connection connection ) {
		log.info( "CONNECT C/%s", connection.id() );
	}

	protected void onData( Connection connection, byte [] data ) {
		log.info( "DATA C/%s %s", connection.id(), Base64.encodeBase64URLSafeString( data ) );
	}

	public void onDisconnected( Connection connection ) {
		log.info( "DISCONNECT C/%s", connection.id() );
		connection.cleanup();
	}

	public void onException( Throwable exception ) {
		log.info( "ERROR [%s] %s", "onException", exception.getClass().getSimpleName(), exception.getLocalizedMessage() );
	}

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

	public void onWriteRequested( Connection connection ) {
		try {
			connection.flush();
		} catch( IOException exception ) {
			onException( exception );
		}
	}

}
