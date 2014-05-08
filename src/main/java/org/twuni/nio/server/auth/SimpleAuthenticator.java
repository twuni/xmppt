package org.twuni.nio.server.auth;

import java.util.HashMap;
import java.util.Map;

public class SimpleAuthenticator implements Authenticator {

	private final Map<String, String> credentials = new HashMap<String, String>();

	public void put( String identity, String secret ) {
		credentials.put( identity, secret );
	}

	@Override
	public void checkCredential( String identity, String secret ) {
		String p = credentials.get( identity );
		if( p == null ) {
			throw new UnknownIdentityException();
		}
		if( !p.equals( secret ) ) {
			throw new AuthenticationException();
		}
	}

}
