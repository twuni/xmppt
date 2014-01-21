package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Channel {

	protected final byte [] buffer = new byte [32 * 1024];
	private final InputStream in;
	private final OutputStream out;

	public Channel( InputStream in, OutputStream out ) {
		this.out = out;
		this.in = in;
	}

	public int read() throws IOException {
		return read( buffer, 0, buffer.length );
	}

	public int read( byte [] buffer, int offset, int length ) throws IOException {
		return in.read( buffer, offset, length );
	}

	public void write( Object in ) throws IOException {
		String s = String.valueOf( in );
		out.write( s.getBytes() );
	}

}
