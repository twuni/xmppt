package org.twuni.xmppt.server;

import java.nio.channels.SocketChannel;

public class XMPPConnectionFactory implements ConnectionFactory {

	@Override
	public Connection createConnection( SocketChannel channel, Dispatcher dispatcher, EventHandler eventHandler ) {
		return new XMPPConnection( channel, dispatcher, eventHandler );
	}

}
