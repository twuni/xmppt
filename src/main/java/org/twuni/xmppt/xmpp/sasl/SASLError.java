package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xmpp.core.Error;

public class SASLError extends Error {

	public SASLError() {
		super( SASLAuthentication.NAMESPACE );
	}

}
