package org.twuni.xmppt.xmpp.iq.session;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Session {

	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-session";
	public static final String ELEMENT_NAME = "session";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Session from( XMLElement element ) {
		return new Session();
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE ).close();
	}

}
