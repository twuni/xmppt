package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Feature {

	public static final String ELEMENT_NAME = "feature";
	public static final String ATTRIBUTE_NAME = "var";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Feature from( XMLElement element ) {
		return new Feature( element.attribute( ATTRIBUTE_NAME ) );
	}

	private final String name;

	public Feature( String name ) {
		this.name = name;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( ATTRIBUTE_NAME, name );
		return xml.close();
	}

}
