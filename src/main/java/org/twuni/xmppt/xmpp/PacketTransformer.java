package org.twuni.xmppt.xmpp;

import java.util.Collection;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLElementFilter;

public abstract class PacketTransformer implements XMLElementFilter {

	public abstract Object transform( XMLElement element );

	public Object [] transform( Collection<? extends Object> objects ) {
		return transform( objects != null ? objects.toArray() : null );
	}

	public Object [] transform( Object... objects ) {
		int count = objects.length;
		Object [] packets = new Object [count];
		for( int i = 0; i < count; i++ ) {
			Object object = objects[i];
			Object packet = object;
			if( object instanceof XMLElement ) {
				XMLElement item = (XMLElement) object;
				if( matches( item ) ) {
					packet = transform( item );
				}
			}
			packets[i] = packet;
		}
		return packets;
	}

}
