package org.twuni.xmppt.xml;

public class IQ {

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TO = "to";
	public static final String TYPE_SET = "set";
	public static final String TYPE_RESULT = "result";
	public static final String ELEMENT_NAME = "iq";

	public static IQ result( String id, Object content ) {
		return new IQ( id, TYPE_RESULT, null, null, content );
	}

	public static IQ result( String id, String from, String to, Object content ) {
		return new IQ( id, TYPE_RESULT, from, to, content );
	}

	public static IQ set( String id, Object content ) {
		return new IQ( id, TYPE_SET, null, null, content );
	}

	public static IQ set( String id, String to, Object content ) {
		return new IQ( id, TYPE_SET, null, to, content );
	}

	private final String id;
	private final String type;
	private final String from;
	private final String to;
	private final Object content;

	public IQ( String id, String type, String from, String to, Object content ) {
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		this.content = content;
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
