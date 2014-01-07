package org.twuni.xmppt.xml;

public class SASLMechanism {

	public static final String ELEMENT_NAME = "mechanism";

	private final String mechanism;

	public SASLMechanism( String mechanism ) {
		this.mechanism = mechanism;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).content( mechanism );
	}

}
