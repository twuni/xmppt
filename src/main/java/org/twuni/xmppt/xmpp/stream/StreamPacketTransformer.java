package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class StreamPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Stream.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( Stream.is( element ) ) {
			return Stream.from( element );
		}

		return null;

	}

}
