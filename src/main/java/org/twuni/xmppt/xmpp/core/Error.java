package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Error {

	public static final String ELEMENT_NAME = "error";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Error from( XMLElement element ) {
		return new Error( element.attributes.get( XMLElement.ATTRIBUTE_NAMESPACE ) );
	}

	private final String namespace;

	public Error( String namespace ) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, namespace ).close();
	}

}
