package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Stream {

	public static final String ROOT_NAMESPACE = "jabber:client";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TO = "to";
	public static final String ATTRIBUTE_VERSION = "version";
	public static final String ELEMENT_NAME = "stream";
	public static final String NAMESPACE = "http://etherx.jabber.org/streams";
	public static final String DEFAULT_PREFIX = "stream";
	public static final String DEFAULT_VERSION = "1.0";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Stream from( XMLElement element ) {
		return new Stream( element.attributes.get( ATTRIBUTE_TO ), element.attributes.get( ATTRIBUTE_FROM ), element.attributes.get( ATTRIBUTE_ID ), DEFAULT_PREFIX, element.attributes.get( ATTRIBUTE_VERSION ) );
	}

	private final String prefix;
	private final String version;
	private final String id;
	private final String to;
	private final String from;

	public Stream() {
		this( null );
	}

	public Stream( String to ) {
		this( to, null );
	}

	public Stream( String to, String from ) {
		this( to, from, null );
	}

	public Stream( String to, String from, String id ) {
		this( to, from, id, DEFAULT_PREFIX, DEFAULT_VERSION );
	}

	public Stream( String to, String from, String id, String prefix, String version ) {
		this.to = to;
		this.from = from;
		this.id = id;
		this.prefix = prefix;
		this.version = version;
	}

	public String close() {
		return new XMLBuilder( prefix, ELEMENT_NAME ).end();
	}

	public String open() {

		XMLBuilder xml = new XMLBuilder( prefix, ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, ROOT_NAMESPACE );
		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, prefix, NAMESPACE );
		xml.attribute( ATTRIBUTE_VERSION, version );
		xml.attribute( ATTRIBUTE_ID, id );
		xml.attribute( ATTRIBUTE_TO, to );
		xml.attribute( ATTRIBUTE_FROM, from );

		return xml.leaveOpen();

	}

	@Override
	public String toString() {
		return open();
	}

}
