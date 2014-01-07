package org.twuni.xmppt.xml;

public class XMPPSession {

	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-session";
	public static final String ELEMENT_NAME = "session";

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, NAMESPACE ).close();
	}

}