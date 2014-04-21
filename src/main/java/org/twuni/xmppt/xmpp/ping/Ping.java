package org.twuni.xmppt.xmpp.ping;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Ping {

	public static final String ELEMENT_NAME = "ping";
	public static final String NAMESPACE = "urn:xmpp:ping";

	public static boolean is( XMLElement element ) {
		return element.belongsTo( NAMESPACE ) && ELEMENT_NAME.equals( element.name );
	}

	public static Ping from( XMLElement element ) {
		return new Ping();
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE ).close();
	}

}
