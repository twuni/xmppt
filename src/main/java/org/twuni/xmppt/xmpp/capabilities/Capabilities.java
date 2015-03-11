package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class Capabilities {

	private static class ChildTransformer extends PacketTransformer {

		public ChildTransformer() {
			// Default constructor.
		}

		@Override
		public boolean matches( XMLElement element ) {
			return true;
		}

		@Override
		public Object transform( XMLElement element ) {

			if( Identity.is( element ) ) {
				return Identity.from( element );
			}

			if( Feature.is( element ) ) {
				return Feature.from( element );
			}

			if( Fields.is( element ) ) {
				return Fields.from( element );
			}

			return element;

		}

	}

	public static Capabilities from( XMLElement element ) {
		return new Capabilities( element.attribute( ATTRIBUTE_NODE ), CHILD_TRANSFORMER.transform( element.children ) );
	}

	public static boolean is( XMLElement element ) {
		return element.belongsTo( NAMESPACE ) && ELEMENT_NAME.equals( element.name );
	}

	public static final String ELEMENT_NAME = "query";
	public static final String NAMESPACE = "http://jabber.org/protocol/disco#info";

	public static final String ATTRIBUTE_NODE = "node";

	private static final PacketTransformer CHILD_TRANSFORMER = new ChildTransformer();

	private final String node;
	private final Object [] content;

	public Capabilities( String node, Object... content ) {
		this.node = node;
		this.content = content;
	}

	public Capabilities( String node, String hash, Object... content ) {
		this( String.format( "%s#%s", node, hash ), content );
	}

	public Object [] getContent() {
		return content;
	}

	public String getNode() {
		return node;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_NODE, node );

		return xml.content( content );

	}

}
