package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Time {

	public static final String ELEMENT_NAME = "time";
	public static final String ATTRIBUTE_STAMP = "stamp";
	public static final String NAMESPACE = "http://silentcircle.com/timestamp";

	public static boolean is( XMLElement xml ) {
		return xml.belongsTo( NAMESPACE );
	}

	public static Time from( XMLElement xml ) {
		return new Time( xml.attribute( ATTRIBUTE_STAMP ) );
	}

	public final String stamp;

	public Time( String stamp ) {
		this.stamp = stamp;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_STAMP, stamp );
		return xml.close();
	}

}
