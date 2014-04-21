package org.twuni.xmppt.server;

import java.nio.channels.SocketChannel;

public class XMPPConnection extends Connection {

	public XMPPConnection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler, int inputBufferSize, int outputBufferSize ) {
		super( client, dispatcher, eventHandler, inputBufferSize, outputBufferSize );
	}

	public XMPPConnection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler ) {
		super( client, dispatcher, eventHandler );
	}

	public static class State {

		public int sent;
		public int received;
		public String username;
		public String resource;

		public String jid( String serviceName ) {
			return String.format( "%s@%s", username, serviceName );
		}

		public String jidWithResource( String serviceName ) {
			return String.format( "%s/%s", jid( serviceName ), resource );
		}

	}

	private final State state = new State();

	@Override
	public Object state() {
		return state;
	}

}
