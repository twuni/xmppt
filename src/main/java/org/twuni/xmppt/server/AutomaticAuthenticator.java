package org.twuni.xmppt.server;

public class AutomaticAuthenticator extends SimpleAuthenticator {
	
	private static final Logger LOG = new Logger( AutomaticAuthenticator.class.getName() );

	@Override
	public void checkCredential( String identity, String secret ) {
		try {
			super.checkCredential( identity, secret );
		} catch( UnknownIdentityException exception ) {
			LOG.info( "CREATE USER %s", identity );
			setCredential( identity, secret );
		}
	}

}
