package org.twuni.xmppt.server;

import java.nio.channels.SocketChannel;

import org.twuni.nio.server.Connection;
import org.twuni.nio.server.ConnectionFactory;
import org.twuni.nio.server.Dispatcher;
import org.twuni.nio.server.EventHandler;

public class XMPPConnectionFactory implements ConnectionFactory {

	@Override
	public Connection createConnection( SocketChannel channel, Dispatcher dispatcher, EventHandler eventHandler ) {
		return new XMPPConnection( channel, dispatcher, eventHandler );
	}

}
