package org.twuni.xmppt.xml;

public class SASLAuthentication {

	public static final String ATTRIBUTE_MECHANISM = "mechanism";
	public static final String ELEMENT_NAME = "auth";

	private final String mechanism;
	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-sasl";

	public SASLAuthentication( String mechanism ) {
		this.mechanism = mechanism;
	}

	protected Object getContent() {
		return null;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, SASLAuthentication.NAMESPACE ).attribute( ATTRIBUTE_MECHANISM, mechanism ).content( getContent() );
	}

}
