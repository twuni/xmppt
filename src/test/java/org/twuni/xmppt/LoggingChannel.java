package org.twuni.xmppt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoggingChannel extends Channel {

	private final String local;
	private final String remote;

	public LoggingChannel( String local, OutputStream out, String remote, InputStream in ) {
		super( in, out );
		this.local = local;
		this.remote = remote;
	}

	@Override
	public int read() throws IOException {
		int size = super.read();
		System.out.println( String.format( "%s: %s", remote, new String( buffer, 0, size ) ) );
		return size;
	}

	@Override
	public void write( Object in ) throws IOException {
		super.write( in );
		System.out.println( String.format( "%s: %s", local, String.valueOf( in ) ) );
	}

}