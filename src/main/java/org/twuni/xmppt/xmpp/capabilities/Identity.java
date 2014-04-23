package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Identity {

	public static final String ELEMENT_NAME = "identity";
	public static final String ATTRIBUTE_CATEGORY = "category";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_TYPE = "type";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Identity from( XMLElement element ) {
		return new Identity( element.attribute( ATTRIBUTE_CATEGORY ), element.attribute( ATTRIBUTE_NAME ), element.attribute( ATTRIBUTE_TYPE ) );
	}

	private final String category;
	private final String name;
	private final String type;

	public Identity( String category, String name, String type ) {
		this.category = category;
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( ATTRIBUTE_CATEGORY, category );
		xml.attribute( ATTRIBUTE_NAME, name );
		xml.attribute( ATTRIBUTE_TYPE, type );

		return xml.close();

	}

}
