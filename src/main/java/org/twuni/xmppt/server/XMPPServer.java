package org.twuni.xmppt.server;

import java.io.IOException;

public class XMPPServer {

	private static final int DEFAULT_PORT = 5222;

	public static void main( String [] args ) {

		int port = DEFAULT_PORT;
		String serviceName = null;

		for( int i = 0; i < args.length; i++ ) {
			String arg = args[i];
			if( "-p".equals( arg ) ) {
				i++;
				port = Integer.parseInt( args[i] );
			} else if( i == args.length - 1 ) {
				serviceName = arg;
			} else {
				System.err.println( String.format( "Unknown argument: %s", arg ) );
			}
		}

		if( serviceName == null ) {
			System.err.println( String.format( "Usage: java %s [-p <port>] <service_name>", XMPPAcceptor.class.getName() ) );
			return;
		}

		try {
			Acceptor acceptor = new XMPPAcceptor( port, serviceName, new AutomaticAuthenticator() );
			acceptor.run();
			acceptor.close();
		} catch( IOException exception ) {
			exception.printStackTrace();
		}

	}

}
