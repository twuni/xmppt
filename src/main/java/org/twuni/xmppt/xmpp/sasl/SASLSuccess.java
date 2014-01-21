package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLElement;

public class SASLSuccess extends Success {

	public static boolean is( XMLElement element ) {
		return Success.is( element ) && element.belongsTo( SASLAuthentication.NAMESPACE );
	}

	public static SASLSuccess from( XMLElement element ) {
		return new SASLSuccess();
	}

	public SASLSuccess() {
		super( SASLAuthentication.NAMESPACE );
	}

}
