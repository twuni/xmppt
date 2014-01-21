package org.twuni.xmppt.xmpp.session;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SessionFeature extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Session.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		return Session.from( element );
	}

}
