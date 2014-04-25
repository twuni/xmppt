package org.twuni.xmppt.xmpp.core;

import java.util.Iterator;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLEntity;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class IQ {

	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_FROM = "from";
	public static final String ATTRIBUTE_TO = "to";
	public static final String TYPE_GET = "get";
	public static final String TYPE_SET = "set";
	public static final String TYPE_RESULT = "result";
	public static final String ELEMENT_NAME = "iq";

	public static IQ from( XMLElement element, PacketTransformer packetTransformer ) {

		String id = element.attribute( ATTRIBUTE_ID );
		String type = element.attribute( ATTRIBUTE_TYPE );
		String from = element.attribute( ATTRIBUTE_FROM );
		String to = element.attribute( ATTRIBUTE_TO );

		int count = element.children.size();
		Object [] content = new Object [count];

		Iterator<XMLEntity> it = element.children.iterator();
		for( int i = 0; i < count; i++ ) {
			XMLEntity entity = it.next();
			Object packet = entity;
			if( entity instanceof XMLElement ) {
				XMLElement child = (XMLElement) entity;
				if( packetTransformer != null ) {
					if( packetTransformer.matches( child ) ) {
						packet = packetTransformer.transform( child );
					}
				}
			}
			content[i] = packet;
		}

		return new IQ( id, type, from, to, content );

	}

	public static IQ get( String id, Object... content ) {
		return new IQ( id, TYPE_GET, null, null, content );
	}

	public static IQ get( String id, String to, Object... content ) {
		return new IQ( id, TYPE_GET, null, to, content );
	}

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static IQ result( String id, Object... content ) {
		return new IQ( id, TYPE_RESULT, null, null, content );
	}

	public static IQ result( String id, String from, String to, Object... content ) {
		return new IQ( id, TYPE_RESULT, from, to, content );
	}

	public static IQ set( String id, Object... content ) {
		return new IQ( id, TYPE_SET, null, null, content );
	}

	public static IQ set( String id, String to, Object... content ) {
		return new IQ( id, TYPE_SET, null, to, content );
	}

	private final String id;
	private final String type;
	private final String from;
	private final String to;
	private final Object [] content;

	public IQ( String id, String type, String from, String to, Object... content ) {
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		this.content = content;
	}

	public boolean expectsResult() {
		return type != null && !TYPE_RESULT.equals( type );
	}

	public Object [] getContent() {
		return content;
	}

	public <T> T getContent( Class<T> type ) {
		for( Object o : content ) {
			if( type.isInstance( o ) ) {
				return (T) o;
			}
		}
		return null;
	}

	public String id() {
		return id;
	}

	public IQ result() {
		return result( id, (Object []) null );
	}

	public IQ result( Object... content ) {
		return IQ.result( id, content );
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

	public String type() {
		return type;
	}

}
