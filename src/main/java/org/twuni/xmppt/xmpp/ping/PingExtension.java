package org.twuni.xmppt.xmpp.ping;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class PingExtension extends PacketTransformer implements Extension {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Ping.NAMESPACE );
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
	public Object transform( XMLElement element ) {

		if( Ping.is( element ) ) {
			return Ping.from( element );
		}

		return element;

	}

	@Override
	public PacketTransformer presence() {
		return null;
	}

}
