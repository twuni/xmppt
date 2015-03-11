package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class AcknowledgmentRequest {

	public static AcknowledgmentRequest from( XMLElement element ) {
		return element != null ? INSTANCE : null;
	}

	public static boolean is( XMLElement element ) {
		return StreamManagement.is( element ) && ELEMENT_NAME.equals( element.name );
	}

	public static final String ELEMENT_NAME = "r";

	private static final AcknowledgmentRequest INSTANCE = new AcknowledgmentRequest();

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE ).close();
	}

}
