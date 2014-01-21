package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class CapabilitiesFeature extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( CapabilitiesHash.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		return CapabilitiesHash.from( element );
	}

}
