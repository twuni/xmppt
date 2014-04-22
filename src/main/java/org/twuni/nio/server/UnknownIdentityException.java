package org.twuni.nio.server;

public class UnknownIdentityException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public UnknownIdentityException() {
		// Default constructor.
	}

	public UnknownIdentityException( String message ) {
		super( message );
	}

	public UnknownIdentityException( Throwable cause ) {
		super( cause );
	}

	public UnknownIdentityException( String message, Throwable cause ) {
		super( message, cause );
	}

}
