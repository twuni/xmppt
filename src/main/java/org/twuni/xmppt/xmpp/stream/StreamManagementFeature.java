package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class StreamManagementFeature {

	public static final String ELEMENT_NAME = "sm";

	public static boolean is( XMLElement element ) {
		return element.belongsTo( StreamManagement.NAMESPACE ) && ELEMENT_NAME.equals( element.name );
	}

	public static StreamManagementFeature from( XMLElement element ) {
		return new StreamManagementFeature();
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE ).close();
	}

}
