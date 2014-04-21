package org.twuni.xmppt.server;

public interface Writable {

	public int write( byte [] buffer );

	public int write( byte [] buffer, int offset, int length );

}
