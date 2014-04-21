package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Acknowledgment {

	public static final String ELEMENT_NAME = "a";
	public static final String ATTRIBUTE_H = "h";

	public static boolean is( XMLElement element ) {
		return StreamManagement.is( element ) && ELEMENT_NAME.equals( element.name );
	}

	public static Acknowledgment from( XMLElement element ) {
		return new Acknowledgment( Integer.parseInt( element.attribute( ATTRIBUTE_H ) ) );
	}

	private final int h;

	public Acknowledgment( int h ) {
		this.h = h;
	}

	public int getH() {
		return h;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE ).attribute( ATTRIBUTE_H, Integer.toString( h ) ).close();
	}

}
