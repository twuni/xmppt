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

	protected Object getContent() {
		return null;
	}

	public String getMechanism() {
		return mechanism;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, SASLAuthentication.NAMESPACE );
		xml.attribute( ATTRIBUTE_MECHANISM, mechanism );

		return xml.content( getContent() );

	}

}
