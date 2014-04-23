package org.twuni;

public class Logger {

	public static final String TAG_VERBOSE = "V";
	public static final String TAG_DEBUG = "D";
	public static final String TAG_INFO = "I";
	public static final String TAG_WARNING = "W";
	public static final String TAG_ERROR = "E";

	private final String tag;

	public Logger( String tag ) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void verbose( String format, Object... args ) {
		log( TAG_VERBOSE, format, args );
	}

	public void debug( String format, Object... args ) {
		log( TAG_DEBUG, format, args );
	}

	public void info( String format, Object... args ) {
		log( TAG_INFO, format, args );
	}

	public void warning( String format, Object... args ) {
		log( TAG_WARNING, format, args );
	}

	public void error( String format, Object... args ) {
		log( TAG_ERROR, format, args );
	}

	private void log( String level, String format, Object... args ) {
		log( createLogMessage( level, format, args ) );
	}

	protected void log( String message ) {
		System.out.println( message );
	}

	private String createLogMessage( String level, String format, Object... args ) {
		return String.format( "%s T/%d [%s] %s", level, Long.valueOf( System.currentTimeMillis() ), getTag(), String.format( format, args ) );
	}

}
