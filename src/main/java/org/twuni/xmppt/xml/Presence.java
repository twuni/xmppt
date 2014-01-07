package org.twuni.xmppt.xml;

public class Presence {

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TO = "to";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ELEMENT_NAME = "presence";

	private final String id;
	private final String to;
	private final String from;

	public Presence( String id ) {
		this( id, null, null );
	}

	public Presence( String id, String to, String from ) {
		this.id = id;
		this.to = to;
		this.from = from;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( ATTRIBUTE_FROM, from );
		xml.attribute( ATTRIBUTE_TO, to );
		xml.attribute( ATTRIBUTE_ID, id );
		return xml.close();
	}

}
