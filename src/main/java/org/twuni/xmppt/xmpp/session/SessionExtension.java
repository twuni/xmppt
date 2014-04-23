package org.twuni.xmppt.xmpp.session;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SessionExtension extends PacketTransformer implements Extension {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Session.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		if( Session.is( element ) ) {
			return Session.from( element );
		}
		return element;
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
		return this;
	}

	@Override
	public PacketTransformer presence() {
		return null;
	}

}
