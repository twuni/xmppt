package org.twuni.xmppt.server;

public interface Authenticator {

	public void checkCredential( String identity, String secret );

}
