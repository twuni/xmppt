package org.twuni.xmppt.xml;

public class Success {

	public static final String ELEMENT_NAME = "success";

	private final String namespace;

	public Success( String namespace ) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, namespace ).close();
	}

}
