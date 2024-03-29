package org.twuni;

public class Logger {

	public static final String FORMAT_LONG = "%1$s T/%2$d [%3$s] %4$s";

	public static final String TAG_VERBOSE = "V";
	public static final String TAG_DEBUG = "D";
	public static final String TAG_INFO = "I";
	public static final String TAG_WARNING = "W";
	public static final String TAG_ERROR = "E";

	private final String tag;

	public Logger( String tag ) {
		this.tag = tag;
	}

	private String createLogMessage( String level, String format, Object... args ) {
		return String.format( FORMAT_LONG, level, Long.valueOf( System.currentTimeMillis() ), getTag(), String.format( format, args ) );
	}

	public void debug( String format, Object... args ) {
		log( TAG_DEBUG, format, args );
	}

	public void error( String format, Object... args ) {
		log( TAG_ERROR, format, args );
	}

	public String getTag() {
		return tag;
	}

	public void info( String format, Object... args ) {
		log( TAG_INFO, format, args );
	}

	protected void log( String message ) {
		System.out.println( message );
	}

	private void log( String level, String format, Object... args ) {
		log( createLogMessage( level, format, args ) );
	}

	public void verbose( String format, Object... args ) {
		log( TAG_VERBOSE, format, args );
	}

	public void warning( String format, Object... args ) {
		log( TAG_WARNING, format, args );
	}

}
