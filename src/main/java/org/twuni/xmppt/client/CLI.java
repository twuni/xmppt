package org.twuni.xmppt.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CLI implements Runnable {

	public static void main( String [] args ) throws IOException {

		String host = "localhost";
		int port = 5222;
		boolean secure = false;

		if( args.length <= 0 ) {
			System.out.println( "" );
			System.out.println( String.format( "Usage: java %s [-p port] [-h host] [-s]", CLI.class.getName() ) );
			System.out.println( String.format( "    -p <port>   The remote port (default: %d)", Integer.valueOf( port ) ) );
			System.out.println( String.format( "    -h <host>   The remote host (default: %s)", host ) );
			System.out.println( String.format( "    -s          Use an SSL/TLS connection (default: %s)", secure ? "yes" : "no" ) );
			System.out.println( "" );
			return;
		}

		for( int i = 0; i < args.length; i++ ) {

			if( "-p".equals( args[i] ) ) {
				i++;
				port = Integer.parseInt( args[i] );
				continue;
			}

			if( "-h".equals( args[i] ) ) {
				i++;
				host = args[i];
				continue;
			}

			if( "-s".equals( args[i] ) ) {
				secure = true;
				continue;
			}

		}

		Socket socket = SocketFactory.createSocket( host, port, secure );
		new Thread( new CLI( socket ) ).start();
		byte [] buffer = new byte [32 * 1024];
		while( socket.isConnected() && !socket.isInputShutdown() ) {
			int count = socket.getInputStream().read( buffer, 0, buffer.length );
			if( count < 0 ) {
				break;
			}
			System.out.println( String.format( "RECV %s", new String( buffer, 0, count ) ) );
		}
		socket.close();

	}

	private Socket socket;

	public CLI( Socket socket ) {
		this.socket = socket;
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner( System.in );
		while( scanner.hasNextLine() && socket.isConnected() && !socket.isOutputShutdown() ) {
			String line = scanner.nextLine();
			byte [] input = line.getBytes();
			try {
				socket.getOutputStream().write( input, 0, input.length );
			} catch( IOException exception ) {
				exception.printStackTrace();
				break;
			}
		}
		scanner.close();
	}

}
