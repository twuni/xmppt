package org.twuni.xmppt.xmpp.iq.bind;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.util.XMLUtils;

public class Resource {

	public static final String ELEMENT_NAME = "resource";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Resource from( XMLElement element ) {
		return new Resource( element.content() );
	}

	private final String resource;

	public Resource( String resource ) {
		this.resource = resource;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).content( XMLUtils.encodeAttribute( resource ) );
	}

}