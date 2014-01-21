package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Success {

	public static final String ELEMENT_NAME = "success";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Success from( XMLElement element ) {
		return new Success( element.attributes.get( XMLElement.ATTRIBUTE_NAMESPACE ) );
	}

	private final String namespace;

	public Success( String namespace ) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, namespace ).close();
	}

}
