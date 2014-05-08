package org.twuni.nio.server.auth;

import org.twuni.Logger;

public class AutomaticAuthenticator extends SimpleAuthenticator {

	private static final Logger LOG = new Logger( AutomaticAuthenticator.class.getName() );

	@Override
	public void checkCredential( String identity, String secret ) {
		try {
			super.checkCredential( identity, secret );
		} catch( UnknownIdentityException exception ) {
			LOG.info( "CREATE USER %s", identity );
			put( identity, secret );
		}
	}

}
