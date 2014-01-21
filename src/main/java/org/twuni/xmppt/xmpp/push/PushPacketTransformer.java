package org.twuni.xmppt.xmpp.push;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class PushPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Push.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		if( Push.is( element ) ) {
			return Push.from( element );
		}
		return null;
	}

}
