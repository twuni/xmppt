package org.twuni.xmppt.server;

public class Logger {

	private final String tag;

	public Logger( String tag ) {
		this.tag = tag;
	}

	public void info( String format, Object... args ) {
		System.out.println( String.format( "[%s] %s", tag, String.format( format, args ) ) );
	}

}
