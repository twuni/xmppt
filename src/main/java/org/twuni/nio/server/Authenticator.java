package org.twuni.nio.server;

public interface Authenticator {

	public void checkCredential( String identity, String secret );

}
