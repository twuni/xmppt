package org.twuni.nio.client;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.net.ssl.SSLContext;

public class CLI implements Runnable {

	public static Socket createSecureSocket( String host, int port ) throws IOException {

		try {

			SSLContext tls = SSLContext.getInstance( "TLSv1.2" );

			try {
				tls.getSocketFactory();
			} catch( IllegalStateException exception ) {
				tls.init( null, null, new SecureRandom() );
			}

			return tls.getSocketFactory().createSocket( host, port );

		} catch( Throwable exception ) {
			throw new IOException( exception );
		}

	}

	public static void main( String [] args ) throws IOException {

		String host = "localhost";
		int port = 5222;
		boolean secure = false;

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

		Socket socket = secure ? createSecureSocket( host, port ) : new Socket( host, port );
		new Thread( new CLI( socket ) ).start();
		byte [] buffer = new byte[32*1024];
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
