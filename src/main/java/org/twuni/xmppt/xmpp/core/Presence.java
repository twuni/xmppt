package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Presence {

	public static enum Type {

		ERROR( "error" ),
		PROBE( "probe" ),
		SUBSCRIBE( "subscribe" ),
		SUBSCRIBED( "subscribed" ),
		UNAVAILABLE( "unavailable" ),
		UNSUBSCRIBE( "unsubscribe" ),
		UNSUBSCRIBED( "unsubscribed" );

		public final String name;

		Type( String name ) {
			this.name = name;
		}

		public static Type forName( String name ) {
			if( name != null ) {
				for( Type type : values() ) {
					if( type.name.equals( name ) ) {
						return type;
					}
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TO = "to";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ELEMENT_NAME = "presence";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Presence from( XMLElement element ) {
		return new Presence( element.attribute( ATTRIBUTE_ID ), element.attribute( ATTRIBUTE_TO ), element.attribute( ATTRIBUTE_FROM ), Type.forName( element.attribute( ATTRIBUTE_TYPE ) ) );
	}

	private final String id;
	private final String to;
	private final String from;
	private final Type type;

	public Presence( String id ) {
		this( id, null, null );
	}

	public Presence( String id, Type type ) {
		this( id, null, null, type );
	}

	public Presence( String id, String to, String from ) {
		this( id, to, from, null );
	}

	public Presence( String id, String to, String from, Type type ) {
		this.id = id;
		this.to = to;
		this.from = from;
		this.type = type;
	}

	public String id() {
		return id;
	}

	public Type type() {
		return type;
	}

	public String from() {
		return from;
	}

	public String to() {
		return to;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( ATTRIBUTE_FROM, from );
		xml.attribute( ATTRIBUTE_TO, to );
		xml.attribute( ATTRIBUTE_ID, id );
		xml.attribute( ATTRIBUTE_TYPE, type );
		return xml.close();
	}

}
