package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Error {

	public static Error from( XMLElement element ) {
		String type = element.attribute( ATTRIBUTE_TYPE );
		String codeString = element.attribute( ATTRIBUTE_CODE );
		String namespace = element.attribute( XMLElement.ATTRIBUTE_NAMESPACE );
		return new Error( namespace, type, codeString != null ? Integer.parseInt( codeString ) : 0, element.content() );
	}

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static final String ELEMENT_NAME = "error";
	public static final String ATTRIBUTE_CODE = "code";

	public static final String ATTRIBUTE_TYPE = "type";

	public static final String TYPE_CANCEL = "cancel";

	public final String namespace;
	public final String prefix;
	public final String type;
	public final int code;
	public final Object content;

	public Error( String namespace ) {
		this( namespace, null, 0, null );
	}

	public Error( String type, int code ) {
		this( null, type, code, null );
	}

	public Error( String namespace, String type, int code, Object content ) {
		this( null, namespace, type, code, content );
	}

	public Error( String prefix, String namespace, String type, int code, Object content ) {
		this.prefix = prefix;
		this.namespace = namespace;
		this.type = type;
		this.code = code;
		this.content = content;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( prefix, ELEMENT_NAME );
		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, namespace );
		xml.attribute( ATTRIBUTE_TYPE, type );
		if( code != 0 ) {
			xml.attribute( ATTRIBUTE_CODE, Integer.valueOf( code ) );
		}
		return xml.content( content );
	}

}
