package org.twuni.xmppt.xml;

public class XMPPStream {

	public static final String ROOT_NAMESPACE = "jabber:client";

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TO = "to";
	public static final String ATTRIBUTE_VERSION = "version";
	public static final String ELEMENT_NAME = "stream";
	public static final String NAMESPACE = "http://etherx.jabber.org/streams";
	public static final String DEFAULT_PREFIX = "stream";
	public static final String DEFAULT_VERSION = "1.0";

	private final String prefix;
	private final String version;
	private final String id;
	private final String to;

	private final String from;

	public XMPPStream() {
		this( null );
	}

	public XMPPStream( String to ) {
		this( to, null );
	}

	public XMPPStream( String to, String from ) {
		this( to, from, null );
	}

	public XMPPStream( String to, String from, String id ) {
		this( to, from, id, DEFAULT_PREFIX, DEFAULT_VERSION );
	}

	public XMPPStream( String to, String from, String id, String prefix, String version ) {
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

		xml.attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, ROOT_NAMESPACE );
		xml.attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, prefix, NAMESPACE );
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
