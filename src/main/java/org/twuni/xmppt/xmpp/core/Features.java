package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class Features {

	public static final String DEFAULT_PREFIX = "stream";
	public static final String ELEMENT_NAME = "features";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Features from( XMLElement element, PacketTransformer packetTransformer ) {
		return new Features( packetTransformer.transform( element.children ) );
	}

	private final String prefix;
	private final Object [] content;

	public Features( Object... content ) {
		this( DEFAULT_PREFIX, content );
	}

	public Features( String prefix, Object... content ) {
		this.prefix = prefix;
		this.content = content;
	}

	public boolean hasFeature( Class<?> type ) {
		return getFeature( type ) != null;
	}

	public Object getFeature( Class<?> type ) {
		for( int i = 0, count = content.length; i < count; i++ ) {
			Object item = content[i];
			if( type.isInstance( item ) ) {
				return item;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new XMLBuilder( prefix, ELEMENT_NAME ).content( content );
	}

}
