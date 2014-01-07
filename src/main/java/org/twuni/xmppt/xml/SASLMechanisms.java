package org.twuni.xmppt.xml;

public class SASLMechanisms {

	public static final String ELEMENT_NAME = "mechanisms";

	private final Object [] mechanisms;

	public SASLMechanisms( String... mechanisms ) {
		int count = mechanisms.length;
		this.mechanisms = new Object [count];
		for( int i = 0; i < count; i++ ) {
			this.mechanisms[i] = new SASLMechanism( mechanisms[i] );
		}
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, SASLAuthentication.NAMESPACE );
		return xml.content( mechanisms );
	}

}
