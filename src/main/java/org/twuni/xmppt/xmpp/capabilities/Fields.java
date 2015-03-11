package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class Fields {

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

			if( Field.is( element ) ) {
				return Field.from( element );
			}

			return element;

		}

	}

	public static Fields from( XMLElement element ) {
		return new Fields( element.attribute( ATTRIBUTE_TYPE ), CHILD_TRANSFORMER.transform( element.children ) );
	}

	public static boolean is( XMLElement element ) {
		return element.belongsTo( NAMESPACE ) && ELEMENT_NAME.equals( element.name );
	}

	public static final String NAMESPACE = "jabber:x:data";
	public static final String ELEMENT_NAME = "x";
	public static final String ATTRIBUTE_TYPE = "type";

	public static final String TYPE_RESULT = "result";

	private static final PacketTransformer CHILD_TRANSFORMER = new ChildTransformer();

	private final String type;
	private final Object [] content;

	public Fields( String type, Object... content ) {
		this.type = type;
		this.content = content;
	}

	public Object [] getContent() {
		return content;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_TYPE, type );

		return xml.content( content );

	}

}
