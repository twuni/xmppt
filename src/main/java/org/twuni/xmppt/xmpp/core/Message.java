package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Message {

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TO = "to";
	public static final String ELEMENT_NAME = "message";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Message from( XMLElement element ) {

		String id = element.attribute( ATTRIBUTE_ID );
		String type = element.attribute( ATTRIBUTE_TYPE );
		String from = element.attribute( ATTRIBUTE_FROM );
		String to = element.attribute( ATTRIBUTE_TO );

		return new Message( id, type, from, to, element.content() );

	}

	private final String id;
	private final String type;
	private final String from;
	private final String to;
	private final Object content;

	public Message( String id, String type, String from, String to, Object content ) {
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		this.content = content;
	}

	public Message from( String from ) {
		return new Message( id, type, from, to, content );
	}

	public String to() {
		return to;
	}

	public String from() {
		return from;
	}

	public String id() {
		return id;
	}

	public String type() {
		return type;
	}

	public Object getContent() {
		return content;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( ATTRIBUTE_ID, id );
		xml.attribute( ATTRIBUTE_TYPE, type );
		xml.attribute( ATTRIBUTE_FROM, from );
		xml.attribute( ATTRIBUTE_TO, to );

		return xml.content( content );

	}

}
