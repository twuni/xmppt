package org.twuni.xmppt.xmpp.session;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SessionPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Session.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		if( Session.is( element ) ) {
			return Session.from( element );
		}
		return null;
	}

}
