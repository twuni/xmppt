package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Resumed {

	public static final String ELEMENT_NAME = "resumed";
	public static final String ATTRIBUTE_H = "h";
	public static final String ATTRIBUTE_PREVIOUS_ID = "previd";

	public static Resumed from( XMLElement element ) {
		String previousID = element.attribute( ATTRIBUTE_PREVIOUS_ID );
		String hString = element.attribute( ATTRIBUTE_H );
		int h = hString != null ? Integer.parseInt( hString ) : 0;
		return new Resumed( previousID, h );
	}

	public static boolean is( XMLElement element ) {
		return StreamManagement.is( element ) && ELEMENT_NAME.equals( element.name );
	}

	private final String previousID;
	private final int h;

	public Resumed( String previousID, int h ) {
		this.previousID = previousID;
		this.h = h;
	}

	public int getH() {
		return h;
	}

	public String getPreviousID() {
		return previousID;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE );
		xml.attribute( ATTRIBUTE_H, h );
		xml.attribute( ATTRIBUTE_PREVIOUS_ID, previousID );

		return xml.close();

	}

}
