package org.twuni.xmppt.xmpp.push;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class PushExtension extends PacketTransformer implements Extension {

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

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return this;
	}

	@Override
	public PacketTransformer feature() {
		return null;
	}

}
