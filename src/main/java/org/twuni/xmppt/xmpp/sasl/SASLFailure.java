package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.core.Failure;

public class SASLFailure extends Failure {

	public static final String REASON_NOT_AUTHORIZED = "not-authorized";

	public static boolean is( XMLElement xml ) {
		return Failure.is( xml ) && xml.belongsTo( SASLAuthentication.NAMESPACE );
	}

	public static SASLFailure from( XMLElement xml ) {
		return new SASLFailure( xml.content() );
	}

	public final String reason;

	public SASLFailure( String reason ) {
		super( SASLAuthentication.NAMESPACE );
		this.reason = reason;
	}

}
