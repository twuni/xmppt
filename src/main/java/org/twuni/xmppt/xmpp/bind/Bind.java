package org.twuni.xmppt.xmpp.bind;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLEntity;

public class Bind {

	public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-bind";
	public static final String ELEMENT_NAME = "bind";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Bind from( XMLElement element ) {
		for( XMLEntity entity : element.children ) {
			if( entity instanceof XMLElement ) {
				XMLElement child = (XMLElement) entity;
				if( JID.is( child ) ) {
					return new Bind( JID.from( child ) );
				}
				if( Resource.is( child ) ) {
					return new Bind( Resource.from( child ) );
				}
			}
		}
		return new Bind( element.content() );
	}

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

	public String jid() {
		if( content instanceof JID ) {
			return ( (JID) content ).getValue();
		}
		return null;
	}

	public String resource() {
		if( content instanceof Resource ) {
			return ( (Resource) content ).getValue();
		}
		return null;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE ).content( content );
	}

}
