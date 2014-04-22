package org.twuni.nio.server;

import java.io.IOException;
import java.net.Socket;

public class WritableSocket implements Writable {

	private final Socket socket;

	public WritableSocket( Socket socket ) {
		this.socket = socket;
	}

	@Override
	public int write( byte [] buffer ) {
		return write( buffer, 0, buffer.length );
	}

	@Override
	public int write( byte [] buffer, int offset, int length ) {
		try {
			socket.getOutputStream().write( buffer, offset, length );
		} catch( IOException exception ) {
			return -1;
		}
		return length;
	}

}
