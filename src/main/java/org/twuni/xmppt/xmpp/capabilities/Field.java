package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class Field {

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

			if( Value.is( element ) ) {
				return Value.from( element );
			}

			return element;

		}

	}

	public static Field from( XMLElement element ) {
		return new Field( element.attribute( ATTRIBUTE_NAME ), element.attribute( ATTRIBUTE_TYPE ), CHILD_TRANSFORMER.transform( element.children ) );
	}

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static final String ELEMENT_NAME = "field";
	public static final String ATTRIBUTE_NAME = "var";
	public static final String ATTRIBUTE_TYPE = "type";

	public static final String TYPE_HIDDEN = "hidden";

	private static final PacketTransformer CHILD_TRANSFORMER = new ChildTransformer();

	private final String name;
	private final String type;
	private final Object [] content;

	public Field( String name, String type, Object... content ) {
		this.name = name;
		this.type = type;
		this.content = content;
	}

	public Object [] getContent() {
		return content;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( ATTRIBUTE_NAME, name );
		xml.attribute( ATTRIBUTE_TYPE, type );
		return xml.content( content );
	}

}
