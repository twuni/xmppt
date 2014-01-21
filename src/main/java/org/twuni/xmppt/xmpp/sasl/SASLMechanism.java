package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class SASLMechanism {

	public static final String ELEMENT_NAME = "mechanism";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static SASLMechanism from( XMLElement element ) {
		return new SASLMechanism( element.children.iterator().next().toString() );
	}

	private final String mechanism;

	public SASLMechanism( String mechanism ) {
		this.mechanism = mechanism;
	}

	public String getMechanism() {
		return mechanism;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).content( mechanism );
	}

}
