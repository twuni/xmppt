package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class SASLAuthentication {

	public static final String ATTRIBUTE_MECHANISM = "mechanism";
	public static final String ELEMENT_NAME = "auth";
	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-sasl";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	private final String mechanism;

	public SASLAuthentication( String mechanism ) {
		this.mechanism = mechanism;
	}

	public String getMechanism() {
		return mechanism;
	}

	protected Object getContent() {
		return null;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, SASLAuthentication.NAMESPACE ).attribute( ATTRIBUTE_MECHANISM, mechanism ).content( getContent() );
	}

}
