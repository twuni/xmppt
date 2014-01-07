package org.twuni.xmppt.xml;

import org.twuni.xmppt.xml.util.XMLUtils;

public class Bind {

	public static class JID {

		public static final String ELEMENT_NAME = "jid";

		private final String jid;

		public JID( String jid ) {
			this.jid = jid;
		}

		@Override
		public java.lang.String toString() {
			return new XMLBuilder( ELEMENT_NAME ).content( XMLUtils.encodeAttribute( jid ) );
		}

	}

	public static class Resource {

		public static final String ELEMENT_NAME = "resource";

		private final String resource;

		public Resource( String resource ) {
			this.resource = resource;
		}

		@Override
		public java.lang.String toString() {
			return new XMLBuilder( ELEMENT_NAME ).content( XMLUtils.encodeAttribute( resource ) );
		}

	}

	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-bind";
	public static final String ELEMENT_NAME = "bind";

	public static Bind jid( String jid ) {
		return new Bind( new JID( jid ) );
	}

	public static Bind resource( String resource ) {
		return new Bind( new Resource( resource ) );
	}

	private final Object content;

	public Bind() {
		this( null );
	}

	public Bind( Object content ) {
		this.content = content;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, NAMESPACE ).content( content );
	}

}
