package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class CapabilitiesExtension extends PacketTransformer implements Extension {

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer feature() {
		return this;
	}

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
