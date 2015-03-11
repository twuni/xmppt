package org.twuni.xmppt.server;

import java.nio.channels.SocketChannel;

import org.twuni.nio.server.Connection;
import org.twuni.nio.server.Dispatcher;
import org.twuni.nio.server.EventHandler;

public class XMPPConnection extends Connection {

	public static class State {

		public int sent;
		public int received;
		public String username;
		public String resource;
		public String streamID;
		public String serviceName;
		public String sessionID;
		public boolean available;
		public String streamManagementID;
		public boolean streamManagementEnabled;

		public boolean hasSession() {
			return sessionID != null;
		}

		public boolean isAvailable() {
			return available;
		}

		public boolean isBound() {
			return resource != null;
		}

		public boolean isStreamManagementEnabled() {
			return streamManagementEnabled;
		}

		public String jid( String serviceName ) {
			return String.format( "%s@%s", username, serviceName );
		}

		public String jidWithResource( String serviceName ) {
			return String.format( "%s/%s", jid( serviceName ), resource );
		}

	}

	private final State state = new State();

	public XMPPConnection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler ) {
		super( client, dispatcher, eventHandler );
	}

	public XMPPConnection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler, int inputBufferSize, int outputBufferSize ) {
		super( client, dispatcher, eventHandler, inputBufferSize, outputBufferSize );
	}

	@Override
	public Object state() {
		return state;
	}

}
