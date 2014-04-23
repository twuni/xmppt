package org.twuni.nio.server.auth;

public interface Authenticator {

	public void checkCredential( String identity, String secret );

}
