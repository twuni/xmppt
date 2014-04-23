package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Value {

	public static final String ELEMENT_NAME = "value";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Value from( XMLElement element ) {
		return new Value( element.content() );
	}

	private final String content;

	public Value( String content ) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).content( content );
	}

}
