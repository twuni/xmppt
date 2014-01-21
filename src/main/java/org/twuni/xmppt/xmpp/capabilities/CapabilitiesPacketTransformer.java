package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class CapabilitiesPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( CapabilitiesHash.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( CapabilitiesHash.is( element ) ) {
			return CapabilitiesHash.from( element );
		}

		return null;

	}

}
